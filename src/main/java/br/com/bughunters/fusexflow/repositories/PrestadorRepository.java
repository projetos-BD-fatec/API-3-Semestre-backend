
package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.Prestador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PrestadorRepository extends JpaRepository<Prestador, Long> {

    // Localiza o prestador pelo CNPJ, armazenado sem pontuação
    Optional<Prestador> findByNrDocumento(String nrDocumento);

    // Mesma regra do índice único UX_PRESTADOR_NOME: UPPER(TRIM(NM_PRESTADOR))
    @Query("select count(p) > 0 from Prestador p where upper(trim(p.nmPrestador)) = upper(trim(:nome))")
    boolean existeComNome(@Param("nome") String nome);
}