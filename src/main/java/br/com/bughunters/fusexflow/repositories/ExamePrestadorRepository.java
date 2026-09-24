package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.ExamePrestador;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExamePrestadorRepository extends JpaRepository<ExamePrestador, Long> {

    @Query("""
            SELECT ep FROM ExamePrestador ep
            WHERE ep.exame.idExame = :idExame
              AND ep.prestador.idPrestador = :idPrestador
              AND ep.status = 'ATIVO'
              AND ep.inicioVigencia <= :hoje
              AND (ep.fimVigencia IS NULL OR ep.fimVigencia >= :hoje)
            """)
    Optional<ExamePrestador> findAtivoByExameIdAndPrestadorId(
            @Param("idExame") Long idExame,
            @Param("idPrestador") Long idPrestador,
            @Param("hoje") LocalDate hoje
    );

    @Query("""
            SELECT ep FROM ExamePrestador ep
            WHERE ep.exame.idExame = :idExame
              AND ep.status = 'ATIVO'
              AND ep.inicioVigencia <= :hoje
              AND (ep.fimVigencia IS NULL OR ep.fimVigencia >= :hoje)
            """)
    List<ExamePrestador> findAtivosByExameId(
            @Param("idExame") Long idExame,
            @Param("hoje") LocalDate hoje
    );
}