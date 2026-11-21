package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.response.LiquidacaoItemResponse;
import br.com.bughunters.fusexflow.dto.response.LiquidacaoResumoResponse;
import br.com.bughunters.fusexflow.entity.Espelho;
import br.com.bughunters.fusexflow.entity.EspelhoItem;
import br.com.bughunters.fusexflow.entity.GuiaOficial;
import br.com.bughunters.fusexflow.entity.LisuraRevisaoItem;
import br.com.bughunters.fusexflow.entity.PreGuia;
import br.com.bughunters.fusexflow.entity.Usuario;
import br.com.bughunters.fusexflow.enums.DecisaoLisura;
import br.com.bughunters.fusexflow.enums.EtapaFluxo;
import br.com.bughunters.fusexflow.enums.StatusGuia;
import br.com.bughunters.fusexflow.repositories.EspelhoItemRepository;
import br.com.bughunters.fusexflow.repositories.EspelhoRepository;
import br.com.bughunters.fusexflow.repositories.GuiaOficialRepository;
import br.com.bughunters.fusexflow.repositories.LisuraRevisaoItemRepository;
import br.com.bughunters.fusexflow.repositories.PreGuiaRepository;
import br.com.bughunters.fusexflow.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Liquidação da guia (US16 / SCRUM-63).
 *
 * Encerra o fluxo: processa os valores que a Lisura aprovou e registra que
 * a guia foi paga, com data, responsável e valor.
 *
 * O valor não é digitado por ninguém. Ele é somado a partir do que já está
 * no banco: para cada item do espelho, o valor final que a Lisura decidiu
 * (LISURA_REVISAO_ITEM) ou, nos itens conformes que não exigem revisão, o
 * próprio valor cobrado. Digitar o total de novo abriria espaço para a
 * liquidação divergir da conferência.
 */
@Service
public class LiquidacaoService {

    /** Quem liquida. Mesma regra do acompanhamento: é visão interna do FUSEx. */
    private static final Set<String> PERFIS_PERMITIDOS =
            Set.of("ROLE_FUSEX", "ROLE_ADMIN");

    private final GuiaOficialRepository guiaRepository;
    private final EspelhoRepository espelhoRepository;
    private final EspelhoItemRepository espelhoItemRepository;
    private final LisuraRevisaoItemRepository revisaoRepository;
    private final PreGuiaRepository preGuiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoStatusService historicoStatusService;

    public LiquidacaoService(GuiaOficialRepository guiaRepository,
                             EspelhoRepository espelhoRepository,
                             EspelhoItemRepository espelhoItemRepository,
                             LisuraRevisaoItemRepository revisaoRepository,
                             PreGuiaRepository preGuiaRepository,
                             UsuarioRepository usuarioRepository,
                             HistoricoStatusService historicoStatusService) {

        this.guiaRepository = guiaRepository;
        this.espelhoRepository = espelhoRepository;
        this.espelhoItemRepository = espelhoItemRepository;
        this.revisaoRepository = revisaoRepository;
        this.preGuiaRepository = preGuiaRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoStatusService = historicoStatusService;
    }

    // ---------- resumo (tela de revisão) ----------

    /**
     * Valores aprovados da guia, item a item, com o total.
     *
     * Antes de liquidar é a tela de revisão; depois, o comprovante do que
     * foi processado.
     */
    @Transactional(readOnly = true)
    public LiquidacaoResumoResponse resumir(Long idGuia) {
        validarPerfil();
        return montarResumo(buscarGuia(idGuia));
    }

    // ---------- liquidação ----------

    /**
     * Confirma a liquidação: calcula o total aprovado, grava na guia e
     * fecha a última etapa do fluxo.
     */
    @Transactional
    public LiquidacaoResumoResponse liquidar(Long idGuia) {
        Usuario responsavel = validarPerfil();

        GuiaOficial guia = buscarGuia(idGuia);

        String impedimento = impedimentoPara(guia);
        if (impedimento != null) {
            throw new IllegalStateException(impedimento);
        }

        List<LiquidacaoItemResponse> itens = itensDaGuia(guia);
        BigDecimal total = somar(itens);

        guia.liquidar(responsavel, total);
        guiaRepository.save(guia);

        historicoStatusService.registrar(
                EtapaFluxo.LIQUIDACAO,
                guia.getSolicitacaoId(),
                guia.getId(),
                StatusGuia.LISURA_APROVADA.name(),
                "LIQUIDADA",
                "Liquidada por " + responsavel.getNmUsuario()
                        + " no valor de R$ " + total.toPlainString() + "."
        );

        return montarResumo(guia);
    }

    // ---------- regras ----------

    /**
     * Por que esta guia não pode ser liquidada. Nulo quando pode.
     *
     * Devolver o motivo em vez de um booleano deixa a tela explicar ao
     * responsável o que falta, em vez de só desabilitar um botão.
     */
    private String impedimentoPara(GuiaOficial guia) {

        if (guia.foiLiquidada()) {
            return "Esta guia já foi liquidada.";
        }

        if (guia.getStatus() != StatusGuia.LISURA_APROVADA) {
            return "Só é possível liquidar uma guia aprovada pela Lisura. "
                    + "Situação atual: " + guia.getStatus() + ".";
        }

        return validarMapaGerado(guia);
    }

    /**
     * Porta do MAPA (critérios 1 e 3 da US16).
     *
     * A US16 exige que só guias com MAPA gerado sejam liquidadas. Essa
     * checagem NÃO existe ainda, e não por esquecimento: a US15 (Geração
     * do MAPA) está sem formato e sem campos definidos — o DoR dela
     * registra as duas perguntas em aberto com o cliente.
     *
     * Este método é o lugar reservado para a regra. Quando a US15 entrar,
     * é aqui que ela encaixa, num ponto só:
     *
     *     if (!mapaRepository.existsByGuia(guia.getId())) {
     *         return "Gere o MAPA antes de liquidar esta guia.";
     *     }
     *
     * @return o impedimento, ou null enquanto a regra não existe
     */
    private String validarMapaGerado(GuiaOficial guia) {
        return null;
    }

    // ---------- valores ----------

    /**
     * Itens do espelho com o valor cobrado e o valor aprovado.
     *
     * Item sem revisão é item conforme: a Lisura não precisa decidir sobre
     * ele, então o aprovado é o cobrado.
     */
    private List<LiquidacaoItemResponse> itensDaGuia(GuiaOficial guia) {

        Espelho espelho = espelhoRepository.findByGuiaOficial_Id(guia.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "A guia " + guia.getId() + " não tem espelho enviado."));

        List<EspelhoItem> itens =
                espelhoItemRepository.findByEspelho_IdEspelhoOrderByIdEspelhoItem(espelho.getIdEspelho());

        // Uma consulta para todas as revisões do espelho, em vez de uma por item.
        Map<Long, LisuraRevisaoItem> revisoes =
                revisaoRepository.findByEspelhoItem_Espelho_IdEspelho(espelho.getIdEspelho())
                        .stream()
                        .collect(Collectors.toMap(
                                r -> r.getEspelhoItem().getIdEspelhoItem(),
                                Function.identity(),
                                (a, b) -> a));

        List<LiquidacaoItemResponse> linhas = new ArrayList<>();

        for (EspelhoItem item : itens) {

            LisuraRevisaoItem revisao = revisoes.get(item.getIdEspelhoItem());

            BigDecimal aprovado = revisao != null
                    ? revisao.getValorFinalUnitario()
                    : item.getValorUnitario();

            BigDecimal quantidade = BigDecimal.valueOf(item.getQuantidade());
            BigDecimal totalCobrado = item.getValorUnitario().multiply(quantidade);
            BigDecimal totalAprovado = aprovado.multiply(quantidade);

            linhas.add(new LiquidacaoItemResponse(
                    item.getCatalogoTuss().getCodTuss(),
                    item.getCatalogoTuss().getDescTuss(),
                    item.getDtRealizacao(),
                    item.getQuantidade(),
                    item.getValorUnitario(),
                    aprovado,
                    totalCobrado,
                    totalAprovado,
                    totalCobrado.subtract(totalAprovado),
                    revisao != null ? revisao.getDecisao().name() : null,
                    revisao != null ? rotuloDa(revisao.getDecisao()) : null
            ));
        }

        return linhas;
    }

    /**
     * A decisão da Lisura em texto de tela.
     *
     * O rótulo mora aqui, e não no enum, para não mexer num arquivo da US
     * da Lisura: esta tela é a única que precisa dele por enquanto.
     */
    private String rotuloDa(DecisaoLisura decisao) {
        return switch (decisao) {
            case ACATAR_CONTRATUAL -> "Valor do contrato";
            case MANTER_COBRADO -> "Manteve o cobrado";
            case VALOR_AJUSTADO -> "Valor ajustado";
        };
    }

    private BigDecimal somar(List<LiquidacaoItemResponse> itens) {
        return itens.stream()
                .map(LiquidacaoItemResponse::totalAprovado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal somarCobrado(List<LiquidacaoItemResponse> itens) {
        return itens.stream()
                .map(LiquidacaoItemResponse::totalCobrado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ---------- montagem ----------

    private LiquidacaoResumoResponse montarResumo(GuiaOficial guia) {

        List<LiquidacaoItemResponse> itens = itensDaGuia(guia);
        String impedimento = impedimentoPara(guia);

        PreGuia preGuia = preGuiaRepository.findById(guia.getSolicitacaoId()).orElse(null);
        Espelho espelho = espelhoRepository.findByGuiaOficial_Id(guia.getId()).orElse(null);

        BigDecimal cobrado = somarCobrado(itens);
        BigDecimal aprovado = somar(itens);

        // Quem conferiu, e quando: é a procedência do valor que o
        // responsável está prestes a liquidar.
        LisuraRevisaoItem ultimaRevisao = espelho == null ? null
                : revisaoRepository.findByEspelhoItem_Espelho_IdEspelho(espelho.getIdEspelho())
                        .stream()
                        .max(Comparator.comparing(LisuraRevisaoItem::getRevisadoEm))
                        .orElse(null);

        return new LiquidacaoResumoResponse(
                guia.getId(),
                guia.getCodigoGuia(),
                preGuia != null && preGuia.getUsuario() != null
                        ? preGuia.getUsuario().getNmUsuario()
                        : null,
                espelho != null && espelho.getPrestador() != null
                        ? espelho.getPrestador().getNomeExibicao()
                        : null,
                guia.getStatus() != null ? guia.getStatus().name() : null,
                impedimento == null,
                impedimento,
                itens,
                itens.size(),
                (int) itens.stream().filter(i -> i.decisao() != null).count(),
                cobrado,
                aprovado,
                cobrado.subtract(aprovado),
                espelho != null ? espelho.getEnviadoEm() : null,
                ultimaRevisao != null && ultimaRevisao.getRevisadoPor() != null
                        ? ultimaRevisao.getRevisadoPor().getNmUsuario()
                        : null,
                ultimaRevisao != null ? ultimaRevisao.getRevisadoEm() : null,
                guia.foiLiquidada(),
                guia.getLiquidadoEm(),
                guia.getUsuarioLiquidacao() != null
                        ? guia.getUsuarioLiquidacao().getNmUsuario()
                        : null,
                guia.getValorLiquidado()
        );
    }

    private GuiaOficial buscarGuia(Long idGuia) {
        return guiaRepository.findById(idGuia)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Guia não encontrada: " + idGuia));
    }

    // ---------- acesso ----------

    private Usuario validarPerfil() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            throw new AccessDeniedException("Acesso restrito ao FUSEx.");
        }

        boolean autorizado = autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(PERFIS_PERMITIDOS::contains);

        if (!autorizado) {
            throw new AccessDeniedException("Apenas o FUSEx pode liquidar guias.");
        }

        return usuarioRepository.findByDsEmail(autenticacao.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuário não encontrado."));
    }
}
