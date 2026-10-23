package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.Espelho;
import br.com.bughunters.fusexflow.enums.StatusEspelho;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EspelhoRepository extends JpaRepository<Espelho, Long> {
    Optional<Espelho> findByGuiaOficial_Id(Long idGuiaOficial);

    /** Espelhos de um status, mais recentes primeiro (usado na lista da Lisura). */
    List<Espelho> findByStatusOrderByEnviadoEmDesc(StatusEspelho status);

    // ---- GET /faturas (FUSEX) ----
    // Parte do Espelho: só existem guias que já têm espelho atrelado.
    // O EntityGraph traz guia e prestador no mesmo SELECT (evita N+1).
    @EntityGraph(attributePaths = {"guiaOficial", "prestador"})
    List<Espelho> findAllByOrderByCriadoEmDesc();

    @EntityGraph(attributePaths = {"guiaOficial", "prestador"})
    List<Espelho> findByStatusOrderByCriadoEmDesc(StatusEspelho status);
}
