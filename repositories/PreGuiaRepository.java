package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.PreGuia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PreGuiaRepository extends JpaRepository<PreGuia, Long> {
    List<PreGuia> findByUsuario_IdUsuario(Long idUsuario);
    Optional<PreGuia> findByIdPreGuiaAndUsuario_IdUsuario(Long idPreGuia, Long idUsuario);

    /** Pré-guias já com o beneficiário carregado (evita N+1 ao ler o nome). */
    @Query("select p from PreGuia p join fetch p.usuario where p.idPreGuia in :ids")
    List<PreGuia> findComUsuarioByIdIn(@Param("ids") Collection<Long> ids);
}
