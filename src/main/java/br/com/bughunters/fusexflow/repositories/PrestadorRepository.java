
package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.Prestador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrestadorRepository extends JpaRepository<Prestador, Long> {

    // Localiza o prestador pelo CNPJ, armazenado sem pontuação
    Optional<Prestador> findByNrDocumento(String nrDocumento);
}