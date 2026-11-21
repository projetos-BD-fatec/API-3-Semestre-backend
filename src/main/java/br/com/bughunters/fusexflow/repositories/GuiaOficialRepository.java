package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.GuiaOficial;
import br.com.bughunters.fusexflow.enums.StatusGuia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GuiaOficialRepository extends JpaRepository<GuiaOficial, Long> {

    /** Guias oficiais geradas a partir das pré-guias informadas. */
    List<GuiaOficial> findBySolicitacaoIdIn(Collection<Long> idsPreGuia);

    @Modifying
    @Query("update GuiaOficial g set g.status = :novo where g.status = :atual and g.dataValidade < :hoje")
    int atualizarStatusDasVencidas(@Param("novo") StatusGuia novo,
                                   @Param("atual") StatusGuia atual,
                                   @Param("hoje") LocalDate hoje);

    /** ATIVA com validade anterior a {@code hoje} passa a VENCIDA. Devolve quantas foram alteradas. */
    default int marcarVencidas(LocalDate hoje) {
        return atualizarStatusDasVencidas(StatusGuia.VENCIDA, StatusGuia.ATIVA, hoje);
    }
  
    boolean existsBySolicitacaoId(Long solicitacaoId);

    Optional<GuiaOficial> findFirstBySolicitacaoIdOrderByIdDesc(
            Long solicitacaoId
    );
}
