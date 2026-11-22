package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.config.RegrasGuiaProperties;
import br.com.bughunters.fusexflow.dto.response.GuiaPrestadorDetalheResponse;
import br.com.bughunters.fusexflow.dto.response.GuiaPrestadorResumoResponse;
import br.com.bughunters.fusexflow.dto.response.GuiaProcedimentoResponse;
import br.com.bughunters.fusexflow.entity.GuiaOficial;
import br.com.bughunters.fusexflow.entity.PreGuia;
import br.com.bughunters.fusexflow.entity.PreGuiaItem;
import br.com.bughunters.fusexflow.entity.Prestador;
import br.com.bughunters.fusexflow.entity.Usuario;
import br.com.bughunters.fusexflow.enums.EtapaFluxo;
import br.com.bughunters.fusexflow.enums.StatusGuia;
import br.com.bughunters.fusexflow.repositories.GuiaAssinaturaCodigoRepository;
import br.com.bughunters.fusexflow.repositories.GuiaOficialRepository;
import br.com.bughunters.fusexflow.repositories.PreGuiaItemRepository;
import br.com.bughunters.fusexflow.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import br.com.bughunters.fusexflow.repositories.PreGuiaRepository;
import br.com.bughunters.fusexflow.enums.StatusPreGuia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Service
public class GuiaOficialService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final GuiaOficialRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final PreGuiaItemRepository preGuiaItemRepository;
    private final GuiaAssinaturaCodigoRepository codigoAssinaturaRepository;
    private final RegrasGuiaProperties regras;
    private final PreGuiaRepository preGuiaRepository;
    private final HistoricoStatusService historicoStatusService;

    public GuiaOficialService(
            GuiaOficialRepository repository,
            UsuarioRepository usuarioRepository,
            PreGuiaItemRepository preGuiaItemRepository,
            GuiaAssinaturaCodigoRepository codigoAssinaturaRepository,
            RegrasGuiaProperties regras,
            PreGuiaRepository preGuiaRepository,
            HistoricoStatusService historicoStatusService
    ) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.preGuiaItemRepository = preGuiaItemRepository;
        this.codigoAssinaturaRepository = codigoAssinaturaRepository;
        this.regras = regras;
        this.preGuiaRepository = preGuiaRepository;
        this.historicoStatusService = historicoStatusService;
    }



    @Transactional(rollbackFor = IOException.class)
    public GuiaOficial salvar(
            MultipartFile file,
            Long solicitacaoId
    ) throws IOException {

        // Identifica o colaborador autenticado
        var autenticacao = SecurityContextHolder
                .getContext()
                .getAuthentication();

        boolean autorizado = autenticacao != null
                && autenticacao.isAuthenticated()
                && autenticacao.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_FUSEX")
                                || a.getAuthority().equals("ROLE_ADMIN")
                );

        if (!autorizado) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não possui permissão para emitir guias."
            );
        }

        Usuario colaborador = usuarioRepository
                .findByDsEmail(autenticacao.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuário autenticado não encontrado."
                ));

        // Busca e bloqueia a pré-guia durante a emissão
        PreGuia preGuia = preGuiaRepository
                .buscarParaAnalise(solicitacaoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pré-guia não encontrada."
                ));

        // Impede a emissão da própria guia
        if (Objects.equals(
                preGuia.getUsuario().getIdUsuario(),
                colaborador.getIdUsuario()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não pode emitir sua própria guia."
            );
        }

        // Somente solicitações autorizadas
        if (preGuia.getStatus() != StatusPreGuia.AUTORIZADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A pré-guia precisa estar autorizada."
            );
        }

        // Impede emissão duplicada
        if (repository.existsBySolicitacaoId(solicitacaoId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta solicitação já possui uma guia oficial."
            );
        }

        // Busca os procedimentos da pré-guia
        List<PreGuiaItem> itens = preGuiaItemRepository
                .findByPreGuia_IdPreGuia(solicitacaoId);

        if (itens.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A solicitação não possui procedimentos."
            );
        }

        // Confirma que todos os procedimentos estão autorizados
        if (itens.stream().anyMatch(item ->
                item.getStatus() != StatusPreGuia.AUTORIZADA
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Existem procedimentos não autorizados."
            );
        }

        // O fluxo atual da OCS exige um único prestador por guia
        long quantidadePrestadores = itens.stream()
                .map(item -> item.getPrestador().getIdPrestador())
                .distinct()
                .count();

        if (quantidadePrestadores != 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A guia deve conter procedimentos de uma única OCS."
            );
        }

        // Validação do arquivo PDF
        if (file == null
                || file.isEmpty()
                || !"application/pdf".equals(file.getContentType())) {

            throw new IllegalArgumentException(
                    "Arquivo deve ser um PDF válido."
            );
        }

        // Limite máximo de 10 MB
        if (file.getSize() > 10L * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "O PDF deve ter no máximo 10 MB."
            );
        }

        // Verifica a assinatura inicial do PDF
        try (var entrada = file.getInputStream()) {

            byte[] assinatura = entrada.readNBytes(5);

            if (!java.util.Arrays.equals(
                    assinatura,
                    "%PDF-".getBytes(
                            java.nio.charset.StandardCharsets.US_ASCII
                    )
            )) {
                throw new IllegalArgumentException(
                        "O arquivo enviado não possui formato PDF."
                );
            }
        }

        // Define o diretório de armazenamento
        Path diretorio = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(diretorio);

        // Gera um nome único para armazenar o PDF
        String nomeUnico = UUID.randomUUID() + ".pdf";

        Path destino = diretorio.resolve(nomeUnico);

        // Gera automaticamente o código da guia oficial
        String codigoGuia = "GUIA-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        try {

            // Salva o arquivo PDF no servidor
            file.transferTo(destino);

            // Cria a entidade da guia oficial
            GuiaOficial guia = new GuiaOficial(
                    null,
                    solicitacaoId,
                    codigoGuia,
                    file.getOriginalFilename(),
                    destino.toString(),
                    LocalDateTime.now(),
                    StatusGuia.ATIVA
            );

            // Validade: data de emissão (upload) + prazo configurado em
            // application.properties (fusex.guia.validade-meses)
            guia.setDataValidade(regras.calcularValidade(guia.getDataUpload()));

            // Salva a guia no banco de dados
            GuiaOficial salva = repository.saveAndFlush(guia);

            // Registra a emissão da guia no histórico de status
            historicoStatusService.registrar(
                    EtapaFluxo.EMISSAO,
                    salva.getSolicitacaoId(),
                    salva.getId(),
                    null,
                    StatusGuia.ATIVA.name(),
                    "Guia " + salva.getCodigoGuia()
                            + " emitida pelo FUSEx."
            );

            // Retorna a guia após registrar o histórico
            return salva;

        } catch (IOException | RuntimeException erro) {

            // Remove o PDF caso ocorra algum erro
            // durante a emissão ou registro do histórico
            try {
                Files.deleteIfExists(destino);

            } catch (IOException limpeza) {
                erro.addSuppressed(limpeza);
            }

            throw erro;
        }
    }




    @Transactional
    public GuiaOficial finalizar(Long id) {

        Prestador prestador = prestadorLogado();

        GuiaOficial guia = repository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Guia não encontrada.")
                );

        List<PreGuiaItem> todosItens =
                preGuiaItemRepository.findByPreGuia_IdPreGuia(
                        guia.getSolicitacaoId()
                );

        if (todosItens.isEmpty()) {
            throw new IllegalStateException(
                    "Esta guia não possui procedimentos cadastrados."
            );
        }

        // A guia só pode ser utilizada por um prestador
        // se todos os seus procedimentos forem destinados a ele.
        for (PreGuiaItem item : todosItens) {
            validarPrestadorDoProcedimento(item, prestador);
        }

        // Guia fora do prazo de validade não pode mais ser utilizada
        if (guia.getStatus() == StatusGuia.VENCIDA
                || (guia.getStatus() == StatusGuia.ATIVA && guia.vencidaEm(LocalDate.now()))) {
            throw new IllegalStateException(
                    "Esta guia venceu" + (guia.getDataValidade() != null
                            ? " em " + guia.getDataValidade().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            : "")
                            + " e não pode mais ser utilizada."
            );
        }

        // Evita alteração de uma guia já utilizada ou encerrada
        if (guia.getStatus() != StatusGuia.ATIVA) {
            throw new IllegalStateException(
                    "A guia não está ativa e não pode ser marcada como utilizada."
            );
        }

        guia.setStatus(StatusGuia.UTILIZADA);

        GuiaOficial salva = repository.save(guia);

        // US17: o prestador confirmou a utilização — etapa de realização.
        // O prestador não é um usuário do FUSEx, então ele fica identificado
        // na observação e o evento é gravado sem ID_USUARIO.
        historicoStatusService.registrar(
                EtapaFluxo.REALIZACAO,
                salva.getSolicitacaoId(),
                salva.getId(),
                StatusGuia.ATIVA.name(),
                StatusGuia.UTILIZADA.name(),
                "Procedimentos realizados por " + prestador.getNomeExibicao() + "."
        );

        return salva;
    }




    @Transactional
    public GuiaPrestadorDetalheResponse finalizarProcedimento(
            Long idGuia,
            Long idItem
    ) {

        // Identifica o prestador autenticado
        Prestador prestador = prestadorLogado();

        // Busca a guia oficial
        GuiaOficial guia = repository.findById(idGuia)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Guia oficial não encontrada."
                        )
                );

        // Busca o procedimento
        PreGuiaItem item = preGuiaItemRepository.findById(idItem)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Procedimento não encontrado."
                        )
                );

        // Confirma que o procedimento pertence à guia
        if (!Objects.equals(
                item.getPreGuia().getIdPreGuia(),
                guia.getSolicitacaoId()
        )) {
            throw new IllegalStateException(
                    "O procedimento não pertence a esta guia."
            );
        }

        // Confirma que pertence ao prestador autenticado
        validarPrestadorDoProcedimento(item, prestador);

        // A guia deve estar utilizada
        if (guia.getStatus() != StatusGuia.UTILIZADA) {
            throw new IllegalStateException(
                    "A guia precisa estar utilizada antes da realização do exame."
            );
        }

        // Impede nova confirmação de um procedimento já realizado
        if (item.isRealizado()) {
            throw new IllegalStateException(
                    "Este procedimento já foi realizado."
            );
        }

        // Registra a realização
        item.marcarComoRealizado();

        preGuiaItemRepository.save(item);

        // Retorna o estado atualizado
        return buscarDetalhes(idGuia);
    }



    @Transactional(readOnly = true)
    public List<GuiaPrestadorResumoResponse> listarGuiasDoPrestador() {

        Prestador prestador = prestadorLogado();

        return repository.findAll()
                .stream()
                .map(guia -> {

                    List<PreGuiaItem> itens =
                            preGuiaItemRepository
                                    .findByPreGuia_IdPreGuiaAndPrestador_IdPrestador(
                                            guia.getSolicitacaoId(),
                                            prestador.getIdPrestador()
                                    );

                    if (itens.isEmpty()) {
                        return null;
                    }

                    PreGuia preGuia =
                            itens.get(0).getPreGuia();

                    int totalProcedimentos =
                            itens.size();

                    int procedimentosRealizados =
                            (int) itens.stream()
                                    .filter(PreGuiaItem::isRealizado)
                                    .count();

                    return new GuiaPrestadorResumoResponse(
                            guia.getId(),
                            preGuia.getIdPreGuia(),
                            guia.getCodigoGuia(),
                            preGuia.getUsuario().getNmUsuario(),
                            statusExibicao(guia),
                            guia.getDataUpload(),
                            procedimentosRealizados,
                            totalProcedimentos
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GuiaPrestadorResumoResponse> pesquisarGuias(
            String termo
    ) {

        List<GuiaPrestadorResumoResponse> guias =
                listarGuiasDoPrestador();

        if (termo == null || termo.isBlank()) {
            return guias;
        }

        String busca =
                termo.trim().toLowerCase();

        return guias.stream()
                .filter(guia ->

                        (
                                guia.codigoGuia() != null
                                        && guia.codigoGuia()
                                        .toLowerCase()
                                        .contains(busca)
                        )

                                ||

                                (
                                        guia.nomePaciente() != null
                                                && guia.nomePaciente()
                                                .toLowerCase()
                                                .contains(busca)
                                )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public GuiaPrestadorDetalheResponse buscarDetalhes(
            Long idGuia
    ) {

        Prestador prestador =
                prestadorLogado();

        GuiaOficial guia =
                repository.findById(idGuia)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Guia não encontrada."
                                )
                        );

        List<PreGuiaItem> itens =
                preGuiaItemRepository
                        .findByPreGuia_IdPreGuiaAndPrestador_IdPrestador(
                                guia.getSolicitacaoId(),
                                prestador.getIdPrestador()
                        );

        if (itens.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Esta guia não pertence ao prestador autenticado."
            );
        }

        PreGuia preGuia =
                itens.get(0).getPreGuia();

        List<GuiaProcedimentoResponse> procedimentos =
                itens.stream()
                        .map(item ->
                                new GuiaProcedimentoResponse(
                                        item.getIdPreGuiaItem(),

                                        item.getExame()
                                                .getCatalogoTuss()
                                                .getCodTuss(),

                                        item.getExame()
                                                .getDescExame(),

                                        item.getValor(),

                                        item.isRealizado(),

                                        item.getDataRealizacao()
                                )
                        )
                        .toList();

        int procedimentosRealizados =
                (int) itens.stream()
                        .filter(PreGuiaItem::isRealizado)
                        .count();

        return new GuiaPrestadorDetalheResponse(
                guia.getId(),
                preGuia.getIdPreGuia(),
                guia.getCodigoGuia(),
                preGuia.getUsuario().getNmUsuario(),
                statusExibicao(guia),
                guia.getStatus().foiUtilizada(),
                guia.getDataUpload(),
                guia.getDataValidade(),
                guia.getDataAssinatura(),
                codigoAssinaturaRepository
                        .findFirstByIdGuiaOficialAndInvalidadoEmIsNullAndExpiraEmAfterOrderByCriadoEmDesc(
                                guia.getId(), LocalDateTime.now())
                        .map(c -> Math.max(0L,
                                Duration.between(LocalDateTime.now(), c.getExpiraEm()).getSeconds()))
                        .orElse(null),
                procedimentosRealizados,
                itens.size(),
                procedimentos
        );
    }


/* =====================================================
   RECUPERAR ARQUIVO OFICIAL DA GUIA
===================================================== */


    @Transactional(readOnly = true)
    public Resource buscarArquivoGuia(Long idGuia) {

        GuiaOficial guia = repository.findById(idGuia)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Guia oficial não encontrada."
                ));

        var autenticacao = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (autenticacao == null ||
                !autenticacao.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuário não autenticado."
            );
        }

        Usuario usuario = usuarioRepository
                .findByDsEmail(autenticacao.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuário autenticado não encontrado."
                ));

        // Funcionários FUSEx e administradores autorizados
        boolean colaborador = autenticacao.getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_FUSEX")
                                || a.getAuthority().equals("ROLE_ADMIN")
                );

        List<PreGuiaItem> itens = preGuiaItemRepository
                .findByPreGuia_IdPreGuia(
                        guia.getSolicitacaoId()
                );

        if (itens.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Não foi possível validar o acesso à guia."
            );
        }

        // Beneficiário titular da solicitação
        boolean beneficiarioTitular = itens.stream()
                .anyMatch(item ->
                        Objects.equals(
                                item.getPreGuia()
                                        .getUsuario()
                                        .getIdUsuario(),
                                usuario.getIdUsuario()
                        )
                );

        // Prestador responsável por algum procedimento
        boolean prestadorVinculado =
                usuario.getPrestador() != null
                        && itens.stream().anyMatch(item ->
                        item.getPrestador() != null
                                && Objects.equals(
                                item.getPrestador()
                                        .getIdPrestador(),
                                usuario.getPrestador()
                                        .getIdPrestador()
                        )
                );

        if (!colaborador
                && !beneficiarioTitular
                && !prestadorVinculado) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não possui permissão para visualizar esta guia."
            );
        }

        try {
            Path caminho = Paths.get(
                    guia.getCaminhoArquivo()
            ).toAbsolutePath().normalize();

            Path diretorioPermitido = Paths.get(
                    uploadDir
            ).toAbsolutePath().normalize();

            if (!caminho.startsWith(diretorioPermitido)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Caminho de arquivo não permitido."
                );
            }

            if (!Files.isRegularFile(caminho)
                    || !Files.isReadable(caminho)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Arquivo da guia não encontrado no servidor."
                );
            }

            return new UrlResource(caminho.toUri());

        } catch (ResponseStatusException erro) {
            throw erro;

        } catch (Exception erro) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao recuperar o arquivo da guia.",
                    erro
            );
        }
    }


    /**
     * Situação mostrada nas telas. Uma guia ATIVA fora do prazo já aparece como
     * VENCIDA mesmo que o job diário ainda não tenha gravado isso no banco.
     */
    private String statusExibicao(GuiaOficial guia) {
        if (guia.getStatus() == StatusGuia.ATIVA && guia.vencidaEm(LocalDate.now())) {
            return StatusGuia.VENCIDA.name();
        }

        return guia.getStatus().name();
    }

    private Prestador prestadorLogado() {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        Usuario usuario =
                usuarioRepository
                        .findByDsEmail(email)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Usuário autenticado não encontrado."
                                )
                        );

        System.out.println("EMAIL AUTENTICADO: " + email);
        System.out.println("ID USUARIO: " + usuario.getIdUsuario());
        System.out.println("EMAIL BANCO: " + usuario.getDsEmail());

        System.out.println("PRESTADOR: " +
                (usuario.getPrestador() != null
                        ? usuario.getPrestador().getIdPrestador()
                        : "NULL"));

        if (usuario.getPrestador() == null) {

            throw new IllegalStateException(
                    "Usuário autenticado não está vinculado a um prestador."
            );
        }

        return usuario.getPrestador();
    }


    private void validarPrestadorDoProcedimento(
            PreGuiaItem item,
            Prestador prestador
    ) {
        if (item.getPrestador() == null ||
                !Objects.equals(
                        item.getPrestador().getIdPrestador(),
                        prestador.getIdPrestador()
                )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Este procedimento não pertence ao prestador autenticado."
            );
        }
    }

}