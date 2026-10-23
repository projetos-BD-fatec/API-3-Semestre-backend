package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.response.FaturaResumoResponse;
import br.com.bughunters.fusexflow.entity.Espelho;
import br.com.bughunters.fusexflow.entity.GuiaOficial;
import br.com.bughunters.fusexflow.entity.PreGuia;
import br.com.bughunters.fusexflow.enums.StatusFatura;
import br.com.bughunters.fusexflow.repositories.EspelhoItemRepository;
import br.com.bughunters.fusexflow.repositories.EspelhoRepository;
import br.com.bughunters.fusexflow.repositories.PreGuiaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Lista de guias com espelho para a visão interna do FUSEX. */
@Service
public class FaturaService {

    private static final Set<String> PERFIS_PERMITIDOS = Set.of("ROLE_FUSEX");

    private final EspelhoRepository espelhoRepository;
    private final EspelhoItemRepository espelhoItemRepository;
    private final PreGuiaRepository preGuiaRepository;

    public FaturaService(EspelhoRepository espelhoRepository,
                         EspelhoItemRepository espelhoItemRepository,
                         PreGuiaRepository preGuiaRepository) {
        this.espelhoRepository = espelhoRepository;
        this.espelhoItemRepository = espelhoItemRepository;
        this.preGuiaRepository = preGuiaRepository;
    }

    /** Guias que já têm espelho, mais recentes primeiro. filtro == null traz todas. */
    @Transactional(readOnly = true)
    public List<FaturaResumoResponse> listar(StatusFatura filtro) {
        validarPerfil();

        List<Espelho> espelhos = (filtro == null)
                ? espelhoRepository.findAllByOrderByCriadoEmDesc()
                : espelhoRepository.findByStatusOrderByCriadoEmDesc(filtro.toStatusEspelho());

        if (espelhos.isEmpty()) {
            return List.of();
        }

        // Duas consultas em lote para a lista inteira (sem N+1)
        List<Long> idsEspelho = espelhos.stream()
                .map(Espelho::getIdEspelho)
                .toList();

        List<Long> idsPreGuia = espelhos.stream()
                .map(e -> e.getGuiaOficial().getSolicitacaoId())
                .distinct()
                .toList();

        Map<Long, BigDecimal> totais = new HashMap<>();
        for (Object[] linha : espelhoItemRepository.somarTotaisPorEspelho(idsEspelho)) {
            BigDecimal total = (linha[1] instanceof BigDecimal b) ? b : new BigDecimal(linha[1].toString());
            totais.put((Long) linha[0], total);
        }

        Map<Long, String> beneficiarios = preGuiaRepository.findComUsuarioByIdIn(idsPreGuia)
                .stream()
                .collect(Collectors.toMap(
                        PreGuia::getIdPreGuia,
                        p -> p.getUsuario().getNmUsuario()
                ));

        return espelhos.stream()
                .map(e -> toResponse(e, totais, beneficiarios))
                .toList();
    }

    private FaturaResumoResponse toResponse(Espelho espelho,
                                            Map<Long, BigDecimal> totais,
                                            Map<Long, String> beneficiarios) {
        GuiaOficial guia = espelho.getGuiaOficial();

        return new FaturaResumoResponse(
                guia.getId(),
                guia.getCodigoGuia(),
                beneficiarios.get(guia.getSolicitacaoId()),
                espelho.getPrestador().getNomeExibicao(),
                espelho.getIdEspelho(),
                espelho.getStatus().name(),
                espelho.getCriadoEm(),
                espelho.getEnviadoEm(),
                totais.getOrDefault(espelho.getIdEspelho(), BigDecimal.ZERO)
        );
    }

    // Mesmo padrão do LisuraService: a checagem de perfil fica no service.
    private void validarPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean permitido = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> PERFIS_PERMITIDOS.contains(a.getAuthority()));
        if (!permitido) {
            throw new AccessDeniedException("Acesso restrito ao perfil FUSEX.");
        }
    }
}
