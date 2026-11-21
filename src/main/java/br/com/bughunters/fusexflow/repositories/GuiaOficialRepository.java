package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.GuiaOficial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GuiaOficialRepository
        extends JpaRepository<GuiaOficial, Long> {

    boolean existsBySolicitacaoId(Long solicitacaoId);

    Optional<GuiaOficial> findFirstBySolicitacaoIdOrderByIdDesc(
            Long solicitacaoId
    );
}