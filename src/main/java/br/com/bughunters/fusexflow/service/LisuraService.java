package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.response.LisuraEspelhoResponse;
import br.com.bughunters.fusexflow.dto.response.LisuraItemResponse;
import br.com.bughunters.fusexflow.entity.Espelho;
import br.com.bughunters.fusexflow.entity.EspelhoItem;
import br.com.bughunters.fusexflow.entity.ExamePrestador;
import br.com.bughunters.fusexflow.entity.GuiaOficial;
import br.com.bughunters.fusexflow.enums.StatusEspelho;
import br.com.bughunters.fusexflow.repositories.EspelhoItemRepository;
import br.com.bughunters.fusexflow.repositories.EspelhoRepository;
import br.com.bughunters.fusexflow.repositories.ExamePrestadorRepository;
import br.com.bughunters.fusexflow.repositories.PreGuiaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/** Conferência (Lisura): valor cobrado pela OCS x valor contratual. */
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
    private final PreGuiaRepository preGuiaRepository;

    public LisuraService(EspelhoRepository espelhoRepository,
                         EspelhoItemRepository espelhoItemRepository,
                         ExamePrestadorRepository examePrestadorRepository,
                         PreGuiaRepository preGuiaRepository) {
        this.espelhoRepository = espelhoRepository;
        this.espelhoItemRepository = espelhoItemRepository;
        this.examePrestadorRepository = examePrestadorRepository;
        this.preGuiaRepository = preGuiaRepository;
    }

    @Transactional(readOnly = true)
    public LisuraEspelhoResponse conferir(Long idGuia) {
        validarPerfil();

        Espelho espelho = espelhoRepository.findByGuiaOficial_Id(idGuia)
                .orElseThrow(() -> new EntityNotFoundException("Nenhum espelho encontrado para a guia: " + idGuia));

        if (espelho.getStatus() != StatusEspelho.ENVIADO) {
            throw new IllegalStateException("O espelho ainda não foi enviado pela OCS.");
        }

        Long idPrestador = espelho.getPrestador().getIdPrestador();

        List<LisuraItemResponse> itens = espelhoItemRepository
                .findByEspelho_IdEspelhoOrderByIdEspelhoItem(espelho.getIdEspelho())
                .stream()
                .map(item -> comparar(item, idPrestador))
                .toList();

        BigDecimal totalCobrado = BigDecimal.ZERO;
        BigDecimal totalContratual = BigDecimal.ZERO;
        BigDecimal diferencaTotal = BigDecimal.ZERO;
        long divergentes = 0;

        for (LisuraItemResponse i : itens) {
            totalCobrado = totalCobrado.add(i.valorCobradoTotal());

            if (i.valorContratualTotal() != null) {
                totalContratual = totalContratual.add(i.valorContratualTotal());
                diferencaTotal = diferencaTotal.add(i.diferenca());
            }
            if (!CONFORME.equals(i.situacao())) {
                divergentes++;
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
                divergentes
        );
    }

    /** Monta a linha do item: cobrado x contratual (por unidade e total). */
    private LisuraItemResponse comparar(EspelhoItem item, Long idPrestador) {
        String codTuss = item.getCatalogoTuss().getCodTuss();
        BigDecimal quantidade = BigDecimal.valueOf(item.getQuantidade());

        BigDecimal contratualUnit = examePrestadorRepository
                .findVigentesByCodTussAndPrestador(codTuss, idPrestador, item.getDtRealizacao())
                .stream()
                .findFirst()
                .map(ExamePrestador::getValorContratual)
                .orElse(null);

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
                codTuss,
                item.getCatalogoTuss().getDescTuss(),
                item.getDtRealizacao(),
                item.getQuantidade(),
                cobradoUnit,
                contratualUnit,
                cobradoTotal,
                contratualTotal,
                diferenca,
                situacao
        );
    }

    private void validarPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean permitido = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> PERFIS_PERMITIDOS.contains(a.getAuthority()));

        if (!permitido) {
            throw new AccessDeniedException("Acesso restrito ao perfil de Lisura.");
        }
    }
}
