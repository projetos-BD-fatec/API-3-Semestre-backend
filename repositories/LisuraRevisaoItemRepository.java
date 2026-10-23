package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.LisuraRevisaoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LisuraRevisaoItemRepository extends JpaRepository<LisuraRevisaoItem, Long> {
    List<LisuraRevisaoItem> findByEspelhoItem_Espelho_IdEspelho(Long idEspelho);
    Optional<LisuraRevisaoItem> findByEspelhoItem_IdEspelhoItem(Long idEspelhoItem);
}
