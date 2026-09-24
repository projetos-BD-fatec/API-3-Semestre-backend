package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.Exame;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExameRepository extends JpaRepository<Exame, Long> {
}