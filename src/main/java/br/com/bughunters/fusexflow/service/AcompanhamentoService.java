package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.response.AcompanhamentoGuiaResponse;
import br.com.bughunters.fusexflow.dto.response.AcompanhamentoResumoResponse;
import br.com.bughunters.fusexflow.dto.response.EtapaAcompanhamentoResponse;
import br.com.bughunters.fusexflow.entity.GuiaOficial;
import br.com.bughunters.fusexflow.entity.HistoricoStatus;
import br.com.bughunters.fusexflow.entity.PreGuia;
import br.com.bughunters.fusexflow.entity.Usuario;
import br.com.bughunters.fusexflow.enums.EtapaFluxo;
import br.com.bughunters.fusexflow.repositories.GuiaOficialRepository;
import br.com.bughunters.fusexflow.repositories.HistoricoStatusRepository;
import br.com.bughunters.fusexflow.repositories.PreGuiaRepository;
import br.com.bughunters.fusexflow.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Acompanhamento do fluxo da guia (US17).
 *
 * Monta a linha do tempo a partir da trilha gravada em HISTORICO_STATUS:
 * cada etapa concluída vira um ponto com data, e as que ainda não
 * aconteceram vêm marcadas como pendentes para a tela desenhar o caminho
 * inteiro.
 */
@Service
public class AcompanhamentoService {

    /** Quem enxerga o acompanhamento. É a visão interna do FUSEx. */
    private static final Set<String> PERFIS_PERMITIDOS =
            Set.of("ROLE_FUSEX", "ROLE_ADMIN");

    private final HistoricoStatusRepository historicoRepository;
    private final GuiaOficialRepository guiaRepository;
    private final PreGuiaRepository preGuiaRepository;
    private final UsuarioRepository usuarioRepository;

    public AcompanhamentoService(HistoricoStatusRepository historicoRepository,
                                 GuiaOficialRepository guiaRepository,
                                 PreGuiaRepository preGuiaRepository,
                                 UsuarioRepository usuarioRepository) {

        this.historicoRepository = historicoRepository;
        this.guiaRepository = guiaRepository;
        this.preGuiaRepository = preGuiaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ---------- listagem ----------

    /**
     * Guias com a etapa do fluxo em que cada uma está.
     *
     * @param filtro etapa a filtrar; nulo traz todas
     */
    @Transactional(readOnly = true)
    public List<AcompanhamentoResumoResponse> listar(EtapaFluxo filtro) {
        validarPerfil();

        List<GuiaOficial> guias = guiaRepository.findAll();

        if (guias.isEmpty()) {
            return List.of();
        }

        // Uma consulta só para todos os eventos, agrupados por guia aqui,
        // em vez de uma consulta por guia dentro do laço.
        Map<Long, List<HistoricoStatus>> eventosPorGuia =
                historicoRepository.buscarEventosDasGuias().stream()
                        .collect(Collectors.groupingBy(HistoricoStatus::getIdGuiaOficial));

        Map<Long, String> beneficiarios = beneficiariosDasGuias(guias);

        List<AcompanhamentoResumoResponse> linhas = new ArrayList<>();

        for (GuiaOficial guia : guias) {

            List<HistoricoStatus> eventos =
                    eventosPorGuia.getOrDefault(guia.getId(), List.of());

            HistoricoStatus ultimo = ultimoEvento(eventos);

            EtapaFluxo etapaAtual = ultimo != null
                    ? ultimo.getEtapa()
                    : EtapaFluxo.EMISSAO;

            if (filtro != null && etapaAtual != filtro) {
                continue;
            }

            linhas.add(new AcompanhamentoResumoResponse(
                    guia.getId(),
                    guia.getCodigoGuia(),
                    beneficiarios.get(guia.getSolicitacaoId()),
                    guia.getStatus() != null ? guia.getStatus().name() : null,
                    etapaAtual.name(),
                    etapaAtual.getRotulo(),
                    etapaAtual.getOrdem(),
                    ultimo != null ? ultimo.getRegistradoEm() : guia.getDataUpload(),
                    guia.foiLiquidada()
            ));
        }

        // Mais recentes primeiro: é o que o FUSEx olha ao abrir a tela.
        linhas.sort(Comparator.comparing(
                AcompanhamentoResumoResponse::atualizadoEm,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return linhas;
    }

    // ---------- linha do tempo de uma guia ----------

    /** Etapas já concluídas e as que faltam, com a data de cada uma. */
    @Transactional(readOnly = true)
    public AcompanhamentoGuiaResponse acompanhar(Long idGuia) {
        validarPerfil();

        GuiaOficial guia = buscarGuia(idGuia);
        PreGuia preGuia = buscarPreGuia(guia.getSolicitacaoId());

        return montarAcompanhamento(guia, preGuia);
    }

    // ---------- montagem da linha do tempo ----------

    private AcompanhamentoGuiaResponse montarAcompanhamento(GuiaOficial guia, PreGuia preGuia) {

        List<HistoricoStatus> trilha =
                historicoRepository.buscarTrilhaDoProcesso(preGuia.getIdPreGuia());

        // Uma etapa pode ter mais de um evento (ex.: a Lisura devolve e decide
        // de novo). O que vale para a linha do tempo é o mais recente.
        Map<EtapaFluxo, HistoricoStatus> ultimoPorEtapa = new LinkedHashMap<>();
        for (HistoricoStatus evento : trilha) {
            ultimoPorEtapa.put(evento.getEtapa(), evento);
        }

        EtapaFluxo etapaAtual = etapaMaisAvancada(ultimoPorEtapa.keySet());

        List<EtapaAcompanhamentoResponse> etapas = new ArrayList<>();

        for (EtapaFluxo etapa : EtapaFluxo.naOrdemDoFluxo()) {

            HistoricoStatus evento = ultimoPorEtapa.get(etapa);
            boolean concluida = evento != null;

            etapas.add(new EtapaAcompanhamentoResponse(
                    etapa.name(),
                    etapa.getRotulo(),
                    etapa.getDescricao(),
                    etapa.getOrdem(),
                    concluida,
                    etapa == etapaAtual,
                    concluida ? evento.getStatusNovo() : null,
                    concluida ? evento.getRegistradoEm() : null,
                    concluida && evento.getUsuario() != null
                            ? evento.getUsuario().getNmUsuario()
                            : null,
                    concluida ? evento.getObservacao() : null
            ));
        }

        return new AcompanhamentoGuiaResponse(
                guia.getId(),
                preGuia.getIdPreGuia(),
                guia.getCodigoGuia(),
                preGuia.getUsuario() != null ? preGuia.getUsuario().getNmUsuario() : null,
                guia.getStatus() != null ? guia.getStatus().name() : null,
                etapaAtual != null ? etapaAtual.name() : null,
                etapaAtual != null ? etapaAtual.getRotulo() : null,
                guia.foiLiquidada(),
                etapas
        );
    }

    /**
     * Etapa em que o processo está: a mais avançada já concluída.
     *
     * É a posição no fluxo que decide, não a data, porque eventos de etapas
     * diferentes podem ser gravados fora de ordem (carga inicial, correção
     * manual) e o acompanhamento não pode andar para trás por causa disso.
     */
    private EtapaFluxo etapaMaisAvancada(Set<EtapaFluxo> concluidas) {
        return concluidas.stream()
                .max(Comparator.comparingInt(EtapaFluxo::getOrdem))
                .orElse(null);
    }

    private HistoricoStatus ultimoEvento(List<HistoricoStatus> eventos) {
        return eventos.stream()
                .max(Comparator.comparingInt(e -> e.getEtapa().getOrdem()))
                .orElse(null);
    }

    /** Nome do beneficiário de cada solicitação, em uma consulta só. */
    private Map<Long, String> beneficiariosDasGuias(List<GuiaOficial> guias) {

        List<Long> idsPreGuia = guias.stream()
                .map(GuiaOficial::getSolicitacaoId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        if (idsPreGuia.isEmpty()) {
            return new HashMap<>();
        }

        return preGuiaRepository.findAllById(idsPreGuia).stream()
                .filter(pg -> pg.getUsuario() != null)
                .collect(Collectors.toMap(
                        PreGuia::getIdPreGuia,
                        pg -> pg.getUsuario().getNmUsuario(),
                        (a, b) -> a));
    }

    private GuiaOficial buscarGuia(Long idGuia) {
        return guiaRepository.findById(idGuia)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Guia não encontrada: " + idGuia));
    }

    private PreGuia buscarPreGuia(Long idPreGuia) {
        return preGuiaRepository.findById(idPreGuia)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Solicitação não encontrada: " + idPreGuia));
    }

    // ---------- acesso ----------

    /** Confere o perfil e devolve o usuário logado. Mesma regra da Lisura. */
    private Usuario validarPerfil() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            throw new AccessDeniedException("Acesso restrito ao FUSEx.");
        }

        boolean autorizado = autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(PERFIS_PERMITIDOS::contains);

        if (!autorizado) {
            throw new AccessDeniedException(
                    "Apenas o FUSEx pode acompanhar o fluxo das guias.");
        }

        return usuarioRepository.findByDsEmail(autenticacao.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuário não encontrado."));
    }
}
