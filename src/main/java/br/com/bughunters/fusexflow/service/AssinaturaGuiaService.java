package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.config.RegrasGuiaProperties;
import br.com.bughunters.fusexflow.dto.response.AssinaturaConfirmadaResponse;
import br.com.bughunters.fusexflow.dto.response.AssinaturaPendenteResponse;
import br.com.bughunters.fusexflow.dto.response.AssinaturaSolicitadaResponse;
import br.com.bughunters.fusexflow.entity.*;
import br.com.bughunters.fusexflow.enums.StatusGuia;
import br.com.bughunters.fusexflow.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Assinatura do beneficiário na guia utilizada.
 *
 * Fluxo:
 *  1. A OCS marca a guia como UTILIZADA e pede a assinatura (solicitarCodigo).
 *  2. O sistema gera um código de 6 dígitos, que só aparece na área do
 *     beneficiário dono da guia (listarPendentes) — a OCS não o vê.
 *  3. O beneficiário diz o código à OCS, que o digita (confirmar).
 *  4. Código correto: a guia fica assinada (data + beneficiário) e o espelho
 *     pode ser criado.
 *
 * Como o código só existe na conta do beneficiário e vale poucos minutos, a OCS
 * não consegue "assinar" sozinha: o beneficiário precisa estar presente.
 */
@Service
public class AssinaturaGuiaService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final GuiaOficialRepository guiaRepository;
    private final GuiaAssinaturaCodigoRepository codigoRepository;
    private final PreGuiaRepository preGuiaRepository;
    private final PreGuiaItemRepository preGuiaItemRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegrasGuiaProperties regras;

    public AssinaturaGuiaService(GuiaOficialRepository guiaRepository,
                                 GuiaAssinaturaCodigoRepository codigoRepository,
                                 PreGuiaRepository preGuiaRepository,
                                 PreGuiaItemRepository preGuiaItemRepository,
                                 UsuarioRepository usuarioRepository,
                                 RegrasGuiaProperties regras) {
        this.guiaRepository = guiaRepository;
        this.codigoRepository = codigoRepository;
        this.preGuiaRepository = preGuiaRepository;
        this.preGuiaItemRepository = preGuiaItemRepository;
        this.usuarioRepository = usuarioRepository;
        this.regras = regras;
    }

    /* =====================================================
       OCS: pede um código (invalida o anterior)
    ===================================================== */

    @Transactional
    public AssinaturaSolicitadaResponse solicitarCodigo(Long idGuia) {
        Prestador prestador = prestadorLogado();
        GuiaOficial guia = guiaDoPrestador(idGuia, prestador);
        validarAssinavel(guia);

        LocalDateTime agora = LocalDateTime.now();

        // Evita que a OCS fique pedindo códigos novos para tentar adivinhar
        long recentes = codigoRepository.countByIdGuiaOficialAndCriadoEmAfter(idGuia, agora.minusHours(1));
        if (recentes >= regras.getMaxSolicitacoesPorHora()) {
            throw new IllegalStateException(
                    "Muitos códigos foram solicitados para esta guia na última hora. Aguarde um pouco e tente novamente.");
        }

        // Só vale o código mais recente
        codigoRepository.findByIdGuiaOficialAndInvalidadoEmIsNull(idGuia)
                .forEach(c -> c.invalidar(agora));

        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        int minutos = regras.getCodigoValidadeMinutos();

        GuiaAssinaturaCodigo novo = codigoRepository.save(
                new GuiaAssinaturaCodigo(idGuia, codigo, agora, agora.plusMinutes(minutos)));

        return new AssinaturaSolicitadaResponse(novo.getExpiraEm(), minutos);
    }

    /* =====================================================
       OCS: informa o código que o beneficiário disse
    ===================================================== */

    // noRollbackFor: a tentativa errada tem que ficar gravada
    @Transactional(noRollbackFor = CodigoAssinaturaInvalidoException.class)
    public AssinaturaConfirmadaResponse confirmar(Long idGuia, String codigoInformado) {
        Prestador prestador = prestadorLogado();
        GuiaOficial guia = guiaDoPrestador(idGuia, prestador);
        validarAssinavel(guia);

        String digitos = codigoInformado == null ? "" : codigoInformado.replaceAll("\\D", "");
        if (digitos.length() != 6) {
            throw new IllegalArgumentException("Informe o código de 6 dígitos exibido no aplicativo do beneficiário.");
        }

        LocalDateTime agora = LocalDateTime.now();

        GuiaAssinaturaCodigo vigente = codigoRepository
                .findFirstByIdGuiaOficialAndInvalidadoEmIsNullAndExpiraEmAfterOrderByCriadoEmDesc(idGuia, agora)
                .orElseThrow(() -> new IllegalStateException(
                        "Não há código válido para esta guia (ele pode ter expirado). Solicite um novo código."));

        if (!iguais(vigente.getCodigo(), digitos)) {
            vigente.registrarTentativaErrada();

            int restantes = regras.getMaxTentativas() - vigente.getTentativas();
            if (restantes <= 0) {
                vigente.invalidar(agora);
                throw new CodigoAssinaturaInvalidoException(
                        "Código incorreto. Limite de tentativas atingido: solicite um novo código.");
            }

            throw new CodigoAssinaturaInvalidoException("Código incorreto. Restam " + restantes + " tentativa(s).");
        }

        PreGuia preGuia = preGuiaRepository.findById(guia.getSolicitacaoId())
                .orElseThrow(() -> new EntityNotFoundException("Pré-guia da guia não encontrada."));

        guia.assinar(preGuia.getUsuario().getIdUsuario(), agora);
        vigente.invalidar(agora);

        return new AssinaturaConfirmadaResponse(guia.getId(), guia.getDataAssinatura());
    }

    /* =====================================================
       BENEFICIÁRIO: guias aguardando a assinatura dele
    ===================================================== */

    @Transactional(readOnly = true)
    public List<AssinaturaPendenteResponse> listarPendentes() {
        Usuario usuario = usuarioLogado();

        List<Long> idsPreGuia = preGuiaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .stream()
                .map(PreGuia::getIdPreGuia)
                .toList();

        if (idsPreGuia.isEmpty()) {
            return List.of();
        }

        Map<Long, GuiaOficial> guias = guiaRepository.findBySolicitacaoIdIn(idsPreGuia).stream()
                .filter(g -> g.getStatus() == StatusGuia.UTILIZADA && !g.isAssinada())
                .collect(Collectors.toMap(GuiaOficial::getId, Function.identity()));

        if (guias.isEmpty()) {
            return List.of();
        }

        LocalDateTime agora = LocalDateTime.now();

        return codigoRepository
                .findByIdGuiaOficialInAndInvalidadoEmIsNullAndExpiraEmAfter(guias.keySet(), agora)
                .stream()
                .sorted(Comparator.comparing(GuiaAssinaturaCodigo::getCriadoEm).reversed())
                .map(c -> {
                    GuiaOficial guia = guias.get(c.getIdGuiaOficial());
                    List<PreGuiaItem> itens = preGuiaItemRepository.findByPreGuia_IdPreGuia(guia.getSolicitacaoId());

                    String prestador = itens.isEmpty() || itens.get(0).getPrestador() == null
                            ? null
                            : itens.get(0).getPrestador().getNomeExibicao();

                    return new AssinaturaPendenteResponse(
                            guia.getId(),
                            guia.getCodigoGuia(),
                            prestador,
                            itens.stream().map(i -> i.getExame().getDescExame()).toList(),
                            c.getCodigo(),
                            c.getExpiraEm(),
                            Math.max(0L, Duration.between(agora, c.getExpiraEm()).getSeconds())
                    );
                })
                .toList();
    }

    /* =====================================================
       REGRAS / APOIO
    ===================================================== */

    /** Só guia UTILIZADA e ainda sem assinatura pode ser assinada. */
    private void validarAssinavel(GuiaOficial guia) {
        if (guia.getStatus() != StatusGuia.UTILIZADA) {
            throw new IllegalStateException("A assinatura só é possível em guias utilizadas.");
        }

        if (guia.isAssinada()) {
            throw new IllegalStateException("Esta guia já foi assinada pelo beneficiário.");
        }
    }

    /** Comparação em tempo constante. */
    private boolean iguais(String esperado, String informado) {
        return MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8),
                informado.getBytes(StandardCharsets.UTF_8));
    }

    /** A guia precisa ter procedimentos da OCS logada (mesma regra da tela de detalhes). */
    private GuiaOficial guiaDoPrestador(Long idGuia, Prestador prestador) {
        GuiaOficial guia = guiaRepository.findById(idGuia)
                .orElseThrow(() -> new NoSuchElementException("Guia não encontrada."));

        boolean daOcs = !preGuiaItemRepository
                .findByPreGuia_IdPreGuiaAndPrestador_IdPrestador(guia.getSolicitacaoId(), prestador.getIdPrestador())
                .isEmpty();

        if (!daOcs) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta guia não pertence ao prestador autenticado.");
        }

        return guia;
    }

    private Usuario usuarioLogado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return usuarioRepository.findByDsEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado."));
    }

    private Prestador prestadorLogado() {
        Usuario usuario = usuarioLogado();

        if (usuario.getPrestador() == null) {
            throw new IllegalStateException("Usuário autenticado não está vinculado a um prestador.");
        }

        return usuario.getPrestador();
    }
}
