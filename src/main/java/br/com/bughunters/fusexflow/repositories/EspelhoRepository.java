package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.Espelho;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EspelhoRepository extends JpaRepository<Espelho, Long> {
    Optional<Espelho> findByGuiaOficial_Id(Long idGuiaOficial);
}