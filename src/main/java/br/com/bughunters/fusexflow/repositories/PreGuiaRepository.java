package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.PreGuia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PreGuiaRepository extends JpaRepository<PreGuia, Long> {

    List<PreGuia> findByUsuario_IdUsuario(Long idUsuario);
    Optional<PreGuia> findByIdPreGuiaAndUsuario_IdUsuario(Long idPreGuia, Long idUsuario);
}