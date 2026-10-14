package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.EspelhoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EspelhoItemRepository extends JpaRepository<EspelhoItem, Long> {
    List<EspelhoItem> findByEspelho_IdEspelhoOrderByIdEspelhoItem(Long idEspelho);
    Optional<EspelhoItem> findByIdEspelhoItemAndEspelho_IdEspelho(Long idEspelhoItem, Long idEspelho);
    long countByEspelho_IdEspelho(Long idEspelho);
}