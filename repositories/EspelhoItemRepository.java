package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.EspelhoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EspelhoItemRepository extends JpaRepository<EspelhoItem, Long> {
    List<EspelhoItem> findByEspelho_IdEspelhoOrderByIdEspelhoItem(Long idEspelho);

    Optional<EspelhoItem> findByIdEspelhoItemAndEspelho_IdEspelho(Long idEspelhoItem, Long idEspelho);

    long countByEspelho_IdEspelho(Long idEspelho);

    /** Soma (valor unitário x quantidade) de vários espelhos de uma vez: [idEspelho, total]. */
    @Query("""
           select i.espelho.idEspelho, sum(i.valorUnitario * i.quantidade)
           from EspelhoItem i
           where i.espelho.idEspelho in :ids
           group by i.espelho.idEspelho
           """)
    List<Object[]> somarTotaisPorEspelho(@Param("ids") Collection<Long> ids);
}
