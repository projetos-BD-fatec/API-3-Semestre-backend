package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.CatalogoTuss;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CatalogoTussRepository extends JpaRepository<CatalogoTuss, String> {
    @Query("""
           select c from CatalogoTuss c
           where lower(c.codTuss) like lower(concat('%', :busca, '%'))
              or lower(c.descTuss) like lower(concat('%', :busca, '%'))
           order by c.codTuss
           """)
    List<CatalogoTuss> buscar(@Param("busca") String busca, Pageable pageable);
}