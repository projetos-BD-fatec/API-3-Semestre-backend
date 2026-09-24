package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.CatalogoTuss;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogoTussRepository extends JpaRepository<CatalogoTuss, String> {
}