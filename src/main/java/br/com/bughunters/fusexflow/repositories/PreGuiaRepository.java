package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.PreGuia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreGuiaRepository extends JpaRepository<PreGuia, Long> {

    List<PreGuia> findByUsuario_IdUsuario(Long idUsuario);
}