package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.request.LisuraRevisaoRequest;
import br.com.bughunters.fusexflow.dto.response.LisuraEspelhoResponse;
import br.com.bughunters.fusexflow.dto.response.LisuraItemResponse;
import br.com.bughunters.fusexflow.entity.Espelho;
import br.com.bughunters.fusexflow.entity.EspelhoItem;
import br.com.bughunters.fusexflow.entity.ExamePrestador;
import br.com.bughunters.fusexflow.entity.GuiaOficial;
import br.com.bughunters.fusexflow.entity.LisuraRevisaoItem;
import br.com.bughunters.fusexflow.entity.Usuario;
import br.com.bughunters.fusexflow.enums.DecisaoLisura;
import br.com.bughunters.fusexflow.enums.StatusEspelho;
import br.com.bughunters.fusexflow.repositories.EspelhoItemRepository;
import br.com.bughunters.fusexflow.repositories.EspelhoRepository;
import br.com.bughunters.fusexflow.repositories.ExamePrestadorRepository;
import br.com.bughunters.fusexflow.repositories.LisuraRevisaoItemRepository;
import br.com.bughunters.fusexflow.repositories.PreGuiaRepository;
import br.com.bughunters.fusexflow.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Conferência (Lisura): valor cobrado pela OCS x valor contratual, e a revisão do analista. */
@Service
public class LisuraService {

    private static final Set<String> PERFIS_PERMITIDOS =
            Set.of("ROLE_LISURA", "ROLE_FUSEX", "ROLE_ADMIN");

    public static final String CONFORME = "CONFORME";
    public static final String ACIMA = "ACIMA";
    public static final String ABAIXO = "ABAIXO";
    public static final String SEM_CONTRATO = "SEM_CONTRATO";

    private final EspelhoRepository espelhoRepository;
    private final EspelhoItemRepository espelhoItemRepository;
    private final ExamePrestadorRepository examePrestadorRepository;
    private final LisuraRevisaoItemRepository revisaoRepository;
    private final PreGuiaRepository preGuiaRepository;
    private final UsuarioRepository usuarioRepository;

    public LisuraService(EspelhoRepository espelhoRepository,
                         EspelhoItemRepository espelhoItemRepository,
                         ExamePrestadorRepository examePrestadorRepository,
                         LisuraRevisaoItemRepository revisaoRepository,
                         PreGuiaRepository preGuiaRepository,
                         UsuarioRepository usuarioRepository) {
        this.espelhoRepository = espelhoRepository;
        this.espelhoItemRepository = espelhoItemRepository;
        this.examePrestadorRepository = examePrestadorRepository;
        this.revisaoRepository = revisaoRepository;
        this.preGuiaRepository = preGuiaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ---------- cruzamento (SCRUM-49) ----------

    @Transactional(readOnly = true)
    public LisuraEspelhoResponse conferir(Long idGuia) {
        validarPerfil();
        return toResponse(espelhoEnviadoDaGuia(idGuia));
    }

    // ---------- revisão (SCRUM-50) ----------

    /**
     * Grava a decisão do analista para um ou mais itens do espelho.
     * Se o item já tinha revisão, ela é substituída.
     */
    @Transactional
    public LisuraEspelhoResponse revisar(Long idGuia, LisuraRevisaoRequest request) {
        validarPerfil();

        if (request == null || request.itens() == null || request.itens().isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos um item para revisar.");
        }

        Espelho espelho = espelhoEnviadoDaGuia(idGuia);
        Long idPrestador = espelho.getPrestador().getIdPrestador();
        Usuario analista = usuarioLogado();

        for (LisuraRevisaoRequest.Item r : request.itens()) {
            if (r == null || r.idItem() == null) {
                throw new IllegalArgumentException("O item a revisar é obrigatório.");
            }

            EspelhoItem item = espelhoItemRepository
                    .findByIdEspelhoItemAndEspelho_IdEspelho(r.idItem(), espelho.getIdEspelho())
                    .orElseThrow(() -> new EntityNotFoundException("Item não encontrado: " + r.idItem()));

            DecisaoLisura decisao = lerDecisao(r.decisao());
            BigDecimal valorFinal = valorFinalUnitario(decisao, item, idPrestador, r.valorAjustado());

            LisuraRevisaoItem revisao = revisaoRepository
                    .findByEspelhoItem_IdEspelhoItem(item.getIdEspelhoItem())
                    .orElseGet(() -> new LisuraRevisaoItem(item));

            revisao.registrar(decisao, valorFinal, analista);
            revisaoRepository.save(revisao);
        }

        return toResponse(espelho);
    }

    private DecisaoLisura lerDecisao(String decisao) {
        if (decisao == null || decisao.isBlank()) {
            throw new IllegalArgumentException("A decisão da revisão é obrigatória.");
        }
        try {
            return DecisaoLisura.valueOf(decisao.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Decisão inválida: " + decisao);
        }
    }

    private BigDecimal valorFinalUnitario(DecisaoLisura decisao, EspelhoItem item,
                                          Long idPrestador, BigDecimal valorAjustado) {
        return switch (decisao) {
            case MANTER_COBRADO -> item.getValorUnitario();

            case ACATAR_CONTRATUAL -> {
                BigDecimal contratual = valorContratual(item, idPrestador);
                if (contratual == null) {
                    throw new IllegalArgumentException(
                            "O procedimento " + item.getCatalogoTuss().getCodTuss()
                                    + " não tem valor contratual vigente. Informe o valor ajustado.");
                }
                yield contratual;
            }

            case VALOR_AJUSTADO -> {
                if (valorAjustado == null || valorAjustado.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Informe um valor ajustado maior ou igual a zero.");
                }
                yield valorAjustado;
            }
        };
    }

    // ---------- montagem da resposta ----------

    private LisuraEspelhoResponse toResponse(Espelho espelho) {
        Long idPrestador = espelho.getPrestador().getIdPrestador();

        Map<Long, LisuraRevisaoItem> revisoes = revisaoRepository
                .findByEspelhoItem_Espelho_IdEspelho(espelho.getIdEspelho())
                .stream()
                .collect(Collectors.toMap(r -> r.getEspelhoItem().getIdEspelhoItem(), Function.identity()));

        List<LisuraItemResponse> itens = espelhoItemRepository
                .findByEspelho_IdEspelhoOrderByIdEspelhoItem(espelho.getIdEspelho())
                .stream()
                .map(item -> comparar(item, idPrestador, revisoes.get(item.getIdEspelhoItem())))
                .toList();

        BigDecimal totalCobrado = BigDecimal.ZERO;
        BigDecimal totalContratual = BigDecimal.ZERO;
        BigDecimal diferencaTotal = BigDecimal.ZERO;
        BigDecimal totalFinal = BigDecimal.ZERO;
        long divergentes = 0;
        long pendentes = 0;

        for (LisuraItemResponse i : itens) {
            totalCobrado = totalCobrado.add(i.valorCobradoTotal());

            if (i.valorContratualTotal() != null) {
                totalContratual = totalContratual.add(i.valorContratualTotal());
                diferencaTotal = diferencaTotal.add(i.diferenca());
            }

            boolean divergente = !CONFORME.equals(i.situacao());
            if (divergente) {
                divergentes++;
            }

            if (i.valorFinalTotal() != null) {
                totalFinal = totalFinal.add(i.valorFinalTotal());
            } else if (!divergente) {
                totalFinal = totalFinal.add(i.valorCobradoTotal());
            } else {
                pendentes++;
            }
        }

        GuiaOficial guia = espelho.getGuiaOficial();
        String nomeBeneficiario = preGuiaRepository.findById(guia.getSolicitacaoId())
                .map(p -> p.getUsuario().getNmUsuario())
                .orElse(null);

        return new LisuraEspelhoResponse(
                espelho.getIdEspelho(),
                guia.getId(),
                nomeBeneficiario,
                espelho.getPrestador().getNomeExibicao(),
                espelho.getStatus().name(),
                espelho.getEnviadoEm(),
                itens,
                totalCobrado,
                totalContratual,
                diferencaTotal,
                divergentes,
                totalFinal,
                pendentes
        );
    }

    /** Monta a linha do item: cobrado x contratual (por unidade e total) + revisão, se houver. */
    private LisuraItemResponse comparar(EspelhoItem item, Long idPrestador, LisuraRevisaoItem revisao) {
        BigDecimal quantidade = BigDecimal.valueOf(item.getQuantidade());
        BigDecimal contratualUnit = valorContratual(item, idPrestador);

        BigDecimal cobradoUnit = item.getValorUnitario();
        BigDecimal cobradoTotal = item.getValorTotal();

        BigDecimal contratualTotal = null;
        BigDecimal diferenca = null;
        String situacao;

        if (contratualUnit == null) {
            situacao = SEM_CONTRATO;
        } else {
            contratualTotal = contratualUnit.multiply(quantidade);
            diferenca = cobradoTotal.subtract(contratualTotal);

            int cmp = cobradoUnit.compareTo(contratualUnit);
            situacao = cmp > 0 ? ACIMA : cmp < 0 ? ABAIXO : CONFORME;
        }

        return new LisuraItemResponse(
                item.getIdEspelhoItem(),
                item.getCatalogoTuss().getCodTuss(),
                item.getCatalogoTuss().getDescTuss(),
                item.getDtRealizacao(),
                item.getQuantidade(),
                cobradoUnit,
                contratualUnit,
                cobradoTotal,
                contratualTotal,
                diferenca,
                situacao,
                revisao == null ? null : revisao.getDecisao().name(),
                revisao == null ? null : revisao.getValorFinalUnitario(),
                revisao == null ? null : revisao.getValorFinalUnitario().multiply(quantidade),
                revisao == null ? null : revisao.getRevisadoPor().getNmUsuario(),
                revisao == null ? null : revisao.getRevisadoEm()
        );
    }

    /** Valor do contrato do prestador para o TUSS do item, vigente na data de realização. */
    private BigDecimal valorContratual(EspelhoItem item, Long idPrestador) {
        return examePrestadorRepository
                .findVigentesByCodTussAndPrestador(
                        item.getCatalogoTuss().getCodTuss(), idPrestador, item.getDtRealizacao())
                .stream()
                .findFirst()
                .map(ExamePrestador::getValorContratual)
                .orElse(null);
    }

    // ---------- regras de acesso ----------

    private Espelho espelhoEnviadoDaGuia(Long idGuia) {
        Espelho espelho = espelhoRepository.findByGuiaOficial_Id(idGuia)
                .orElseThrow(() -> new EntityNotFoundException("Nenhum espelho encontrado para a guia: " + idGuia));

        if (espelho.getStatus() != StatusEspelho.ENVIADO) {
            throw new IllegalStateException("O espelho desta guia ainda não foi enviado pela OCS.");
        }
        return espelho;
    }

    private void validarPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean permitido = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> PERFIS_PERMITIDOS.contains(a.getAuthority()));

        if (!permitido) {
            throw new AccessDeniedException("Acesso restrito ao perfil de Lisura.");
        }
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();

        return usuarioRepository.findByDsEmail(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado: " + login));
    }
}
