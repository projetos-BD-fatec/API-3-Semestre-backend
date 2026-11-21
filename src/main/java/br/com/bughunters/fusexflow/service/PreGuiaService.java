package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.request.PreGuiaItemRequest;
import br.com.bughunters.fusexflow.dto.request.PreGuiaRequest;
import br.com.bughunters.fusexflow.dto.response.PreGuiaItemResponse;
import br.com.bughunters.fusexflow.dto.response.PreGuiaResponse;
import br.com.bughunters.fusexflow.entity.*;
import br.com.bughunters.fusexflow.enums.EtapaFluxo;
import br.com.bughunters.fusexflow.enums.StatusPreGuia;
import br.com.bughunters.fusexflow.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import br.com.bughunters.fusexflow.enums.StatusPreGuia;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import br.com.bughunters.fusexflow.entity.GuiaOficial;
import br.com.bughunters.fusexflow.repositories.GuiaOficialRepository;

import java.io.IOException;
import java.util.List;

@Service
public class PreGuiaService {

    private final ArquivoService arquivoService;
    private final UsuarioRepository usuarioRepository;
    private final PrestadorRepository prestadorRepository;
    private final ExameRepository exameRepository;
    private final ExamePrestadorRepository examePrestadorRepository;
    private final PreGuiaRepository preGuiaRepository;
    private final PreGuiaItemRepository preGuiaItemRepository;
    private final GuiaOficialRepository guiaOficialRepository;
    private final HistoricoStatusService historicoStatusService;


    public PreGuiaService(
            ArquivoService arquivoService,
            UsuarioRepository usuarioRepository,
            PrestadorRepository prestadorRepository,
            ExameRepository exameRepository,
            ExamePrestadorRepository examePrestadorRepository,
            PreGuiaRepository preGuiaRepository,
            PreGuiaItemRepository preGuiaItemRepository,
            GuiaOficialRepository guiaOficialRepository,
            HistoricoStatusService historicoStatusService
    ) {
        this.arquivoService = arquivoService;
        this.usuarioRepository = usuarioRepository;
        this.prestadorRepository = prestadorRepository;
        this.exameRepository = exameRepository;
        this.examePrestadorRepository = examePrestadorRepository;
        this.preGuiaRepository = preGuiaRepository;
        this.preGuiaItemRepository = preGuiaItemRepository;
        this.guiaOficialRepository = guiaOficialRepository;
        this.historicoStatusService = historicoStatusService;
    }


    /**
     * Uso futuro: tela do funcionário FUSEX, que precisa ver todas as solicitações.
     * Não usar essa versão na tela do usuário comum.
     */
    public List<PreGuiaResponse> findAll() {
        return preGuiaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Uso na tela do usuário: retorna apenas as pré-guias abertas por ele.
     */
    public List<PreGuiaResponse> findMinhas() {
        Usuario usuario = usuarioLogado();

        return preGuiaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PreGuiaResponse criar(PreGuiaRequest request, MultipartFile arquivo) {

        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("O arquivo é obrigatório.");
        }

        if (request == null || request.itens() == null || request.itens().isEmpty()) {
            throw new IllegalArgumentException("A pré-guia deve possuir pelo menos um item.");
        }

        Usuario usuario = usuarioLogado();

        PreGuia preGuia = new PreGuia(usuario);
        preGuia = preGuiaRepository.save(preGuia);

        // US17: primeira etapa do fluxo acompanhado pelo FUSEx.
        historicoStatusService.registrar(
                EtapaFluxo.SOLICITACAO,
                preGuia.getIdPreGuia(),
                null,
                null,
                StatusPreGuia.EM_ANALISE.name(),
                "Solicitação aberta por " + usuario.getNmUsuario() + "."
        );

        for (PreGuiaItemRequest itemRequest : request.itens()) {

            Exame exame = exameRepository.findById(itemRequest.idExame())
                    .orElseThrow(() -> new EntityNotFoundException("Exame não encontrado: " + itemRequest.idExame()));

            Prestador prestador = prestadorRepository.findById(itemRequest.idPrestador())
                    .orElseThrow(() -> new EntityNotFoundException("Prestador não encontrado: " + itemRequest.idPrestador()));

            ExamePrestador examePrestador = examePrestadorRepository
                    .findAtivoByExameIdAndPrestadorId(itemRequest.idExame(), itemRequest.idPrestador(), java.time.LocalDate.now())
                    .orElseThrow(() -> new IllegalArgumentException("Exame não possui relação ativa com o prestador informado."));

            PreGuiaItem item = new PreGuiaItem(
                    preGuia,
                    prestador,
                    exame,
                    examePrestador.getValorContratual()
            );

            preGuiaItemRepository.save(item);
        }

        String nomeArquivo;
        try {
            nomeArquivo = arquivoService.salvar(arquivo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar o arquivo.", e);
        }
        preGuia.setNmAnexo(nomeArquivo);
        List<PreGuiaItem> itens = preGuiaItemRepository.findByPreGuia_IdPreGuia(preGuia.getIdPreGuia());

        return toResponse(preGuia, itens);
    }

    public ArquivoDownload buscarArquivo(Long idPreGuia) {
        Usuario usuario = usuarioLogado();

        PreGuia preGuia = preGuiaRepository
                .findByIdPreGuiaAndUsuario_IdUsuario(idPreGuia, usuario.getIdUsuario())
                .orElseThrow(() -> new EntityNotFoundException("Pré-guia não encontrada: " + idPreGuia));

        String nomeArquivo = preGuia.getNmAnexo();

        if (nomeArquivo == null || nomeArquivo.isBlank()) {
            throw new EntityNotFoundException("A pré-guia não possui arquivo anexado.");
        }

        return new ArquivoDownload(
                arquivoService.carregar(nomeArquivo),
                arquivoService.tipoDoArquivo(nomeArquivo),
                nomeArquivo
        );
    }

    private Usuario usuarioLogado() {
        String loginAtual = SecurityContextHolder.getContext().getAuthentication().getName();

        return usuarioRepository.findByDsEmail(loginAtual)
                .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado: " + loginAtual));
    }

    private PreGuiaResponse toResponse(PreGuia preGuia) {
        List<PreGuiaItem> itens = preGuiaItemRepository.findByPreGuia_IdPreGuia(preGuia.getIdPreGuia());
        return toResponse(preGuia, itens);
    }

    private PreGuiaResponse toResponse(PreGuia preGuia, List<PreGuiaItem> itens) {
        List<PreGuiaItemResponse> itensResponse = itens.stream()
                .map(item -> new PreGuiaItemResponse(
                        item.getIdPreGuiaItem(),
                        item.getExame().getCatalogoTuss().getCodTuss(),
                        item.getExame().getDescExame(),
                        item.getPrestador().getNomeExibicao(),
                        item.getPrestador().getDsLogradouro(),
                        item.getPrestador().getNrEndereco(),
                        item.getPrestador().getDsComplemento(),
                        item.getPrestador().getNmBairro(),
                        item.getPrestador().getNmCidade(),
                        item.getPrestador().getSgUf(),
                        item.getValor(),
                        item.getStatus().name()
                ))
                .toList();

        // A guia é exibida somente após a autorização e emissão.
        GuiaOficial guiaOficial = preGuia.getStatus() == StatusPreGuia.AUTORIZADA
                ? guiaOficialRepository.findFirstBySolicitacaoIdOrderByIdDesc(
                preGuia.getIdPreGuia()).orElse(null)
                : null;

        return new PreGuiaResponse(
                preGuia.getIdPreGuia(),
                preGuia.getNmAnexo(),
                preGuia.getStatus().name(),
                preGuia.getCriadoEm(),
                itensResponse,
                preGuia.getStatus() == StatusPreGuia.NEGADA
                        ? preGuia.getMotivoRecusa()
                        : null,
                guiaOficial == null ? null : guiaOficial.getId(),
                guiaOficial == null ? null : guiaOficial.getCodigoGuia()
        );
    }


/* =====================================================
   PAINEL DO COLABORADOR FUSEX
===================================================== */

    public record PreGuiaPainelResponse(
            Long idPreGuia,
            String nomeBeneficiario,
            String emailBeneficiario,
            String status,
            LocalDateTime criadoEm,
            boolean possuiEncaminhamento,
            boolean solicitacaoPropria,
            List<PreGuiaItemResponse> itens,
            Long idAnalisadoPor,
            LocalDateTime dataAnalise,
            String motivoRecusa,
            Long idGuiaOficial,
            String codigoGuia,
            String statusGuiaOficial
    ) {
    }


    @Transactional(readOnly = true)
    public List<PreGuiaPainelResponse> listarParaColaborador() {

        Usuario colaborador = usuarioLogado();
        Long idColaborador = colaborador.getIdUsuario();

        return preGuiaRepository.findAll()
                .stream()
                .map(preGuia -> {

                    PreGuiaResponse dados = toResponse(preGuia);

                    boolean propria = idColaborador.equals(
                            preGuia.getUsuario().getIdUsuario()
                    );

                    GuiaOficial guiaOficial = guiaOficialRepository
                            .findFirstBySolicitacaoIdOrderByIdDesc(
                                    preGuia.getIdPreGuia()
                            )
                            .orElse(null);


                    return new PreGuiaPainelResponse(
                            preGuia.getIdPreGuia(),
                            preGuia.getUsuario().getNmUsuario(),
                            preGuia.getUsuario().getDsEmail(),
                            preGuia.getStatus().name(),
                            preGuia.getCriadoEm(),
                            preGuia.getNmAnexo() != null
                                    && !preGuia.getNmAnexo().isBlank(),
                            propria,
                            dados.itens(),
                            preGuia.getAnalisadoPor() == null
                                    ? null
                                    : preGuia.getAnalisadoPor().getIdUsuario(),
                            preGuia.getDataAnalise(),
                            preGuia.getMotivoRecusa(),

                            // Dados da guia oficial
                            guiaOficial == null
                                    ? null
                                    : guiaOficial.getId(),

                            guiaOficial == null
                                    ? null
                                    : guiaOficial.getCodigoGuia(),

                            guiaOficial == null
                                    ? null
                                    : guiaOficial.getStatus().name()
                    );

                })
                .sorted((a, b) ->
                        b.criadoEm().compareTo(a.criadoEm())
                )
                .toList();
    }


    @Transactional(readOnly = true)
    public ArquivoDownload buscarArquivoParaColaborador(Long idPreGuia) {

        PreGuia preGuia = preGuiaRepository.findById(idPreGuia)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Pré-guia não encontrada: " + idPreGuia
                ));

        String nomeArquivo = preGuia.getNmAnexo();

        if (nomeArquivo == null || nomeArquivo.isBlank()) {
            throw new EntityNotFoundException(
                    "Esta pré-guia não possui encaminhamento anexado."
            );
        }

        return new ArquivoDownload(
                arquivoService.carregar(nomeArquivo),
                arquivoService.tipoDoArquivo(nomeArquivo),
                nomeArquivo
        );
    }


/* =====================================================
   DECISÃO DO COLABORADOR FUSEX
===================================================== */

    public record DecisaoPreGuiaRequest(
            String decisao,
            String motivoRecusa
    ) {
    }

    @Transactional
    public PreGuiaResponse analisarPreGuia(
            Long idPreGuia,
            DecisaoPreGuiaRequest request
    ) {
        Authentication autenticacao =
                SecurityContextHolder.getContext().getAuthentication();

        boolean possuiPermissao = autenticacao != null
                && autenticacao.isAuthenticated()
                && autenticacao.getAuthorities().stream()
                .anyMatch(autoridade ->
                        autoridade.getAuthority().equals("ROLE_FUSEX")
                                || autoridade.getAuthority().equals("ROLE_ADMIN")
                );

        if (!possuiPermissao) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem permissão para analisar pré-guias."
            );
        }

        if (request == null || request.decisao() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe a decisão."
            );
        }

        String decisao = request.decisao().trim().toUpperCase();

        if (!decisao.equals("AUTORIZADA") && !decisao.equals("NEGADA")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A decisão deve ser AUTORIZADA ou NEGADA."
            );
        }

        Usuario colaborador = usuarioLogado();

        PreGuia preGuia = preGuiaRepository
                .buscarParaAnalise(idPreGuia)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pré-guia não encontrada."
                ));

        // Bloqueio obrigatório: funcionário não analisa pedido próprio.
        if (preGuia.getUsuario().getIdUsuario()
                .equals(colaborador.getIdUsuario())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não pode aprovar ou negar sua própria solicitação."
            );
        }

        if (preGuia.getStatus() != StatusPreGuia.EM_ANALISE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta pré-guia já foi analisada."
            );
        }

        String motivo = request.motivoRecusa() == null
                ? ""
                : request.motivoRecusa().trim();

        if (decisao.equals("NEGADA") && motivo.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o motivo da recusa."
            );
        }

        if (motivo.length() > 1000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O motivo deve ter no máximo 1000 caracteres."
            );
        }

        StatusPreGuia novoStatus =
                StatusPreGuia.valueOf(decisao);

        preGuia.registrarDecisao(
                novoStatus,
                colaborador,
                motivo
        );

        List<PreGuiaItem> itens =
                preGuiaItemRepository.findByPreGuia_IdPreGuia(
                        preGuia.getIdPreGuia()
                );

        for (PreGuiaItem item : itens) {
            item.setStatus(novoStatus);
        }

        return toResponse(preGuia, itens);
    }



}