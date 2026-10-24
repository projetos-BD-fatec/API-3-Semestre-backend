package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.response.GuiaPrestadorDetalheResponse;
import br.com.bughunters.fusexflow.dto.response.GuiaPrestadorResumoResponse;
import br.com.bughunters.fusexflow.dto.response.GuiaProcedimentoResponse;
import br.com.bughunters.fusexflow.entity.GuiaOficial;
import br.com.bughunters.fusexflow.entity.PreGuia;
import br.com.bughunters.fusexflow.entity.PreGuiaItem;
import br.com.bughunters.fusexflow.entity.Prestador;
import br.com.bughunters.fusexflow.entity.Usuario;
import br.com.bughunters.fusexflow.enums.StatusGuia;
import br.com.bughunters.fusexflow.repositories.GuiaOficialRepository;
import br.com.bughunters.fusexflow.repositories.PreGuiaItemRepository;
import br.com.bughunters.fusexflow.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
import java.time.LocalDateTime;
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

    public GuiaOficialService(
            GuiaOficialRepository repository,
            UsuarioRepository usuarioRepository,
            PreGuiaItemRepository preGuiaItemRepository
    ) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.preGuiaItemRepository = preGuiaItemRepository;
    }

    public GuiaOficial salvar(MultipartFile file, Long solicitacaoId) throws IOException {

        if (file.isEmpty() || !"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Arquivo deve ser um PDF válido.");
        }

        Path diretorio = Paths.get(uploadDir);
        Files.createDirectories(diretorio);

        String nomeUnico = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path destino = diretorio.resolve(nomeUnico).normalize();

        file.transferTo(destino);

        String codigoGuia = "GUIA-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        GuiaOficial guia = new GuiaOficial(
                null,
                solicitacaoId,
                codigoGuia,
                file.getOriginalFilename(),
                destino.toString(),
                LocalDateTime.now(),
                StatusGuia.ATIVA
        );

        return repository.save(guia);
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

        // Evita alteração de uma guia já utilizada ou encerrada
        if (guia.getStatus() != StatusGuia.ATIVA) {
            throw new IllegalStateException(
                    "A guia não está ativa e não pode ser marcada como utilizada."
            );
        }

        guia.setStatus(StatusGuia.UTILIZADA);

        return repository.save(guia);
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
                            guia.getStatus().name(),
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
                guia.getStatus().name(),
                guia.getStatus().foiUtilizada(),
                guia.getDataUpload(),
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

        // Primeiro valida se o prestador pode acessar a guia.
        buscarDetalhes(idGuia);

        // Recupera o registro da guia oficial.
        GuiaOficial guia = repository.findById(idGuia)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Guia oficial não encontrada."
                        )
                );

        try {

            // Recupera o caminho salvo durante o upload.
            Path caminho = Paths.get(guia.getCaminhoArquivo())
                    .toAbsolutePath()
                    .normalize();

            // Confirma que o arquivo está dentro do diretório de uploads.
            Path diretorioPermitido = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            if (!caminho.startsWith(diretorioPermitido)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Caminho de arquivo não permitido."
                );
            }

            if (!Files.isRegularFile(caminho) ||
                    !Files.isReadable(caminho)) {

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