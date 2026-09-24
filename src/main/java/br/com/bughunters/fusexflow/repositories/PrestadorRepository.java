package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.Prestador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrestadorRepository extends JpaRepository<Prestador, Long> {
}