package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.PreGuia;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PreGuiaRepository extends JpaRepository<PreGuia, Long> {

    List<PreGuia> findByUsuario_IdUsuario(Long idUsuario);
    Optional<PreGuia> findByIdPreGuiaAndUsuario_IdUsuario(Long idPreGuia, Long idUsuario);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PreGuia p WHERE p.idPreGuia = :id")
    Optional<PreGuia> buscarParaAnalise(@Param("id") Long id);
}