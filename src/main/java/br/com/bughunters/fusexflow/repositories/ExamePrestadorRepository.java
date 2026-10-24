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

    /**
     * Contrato do prestador para um código TUSS, vigente na data informada
     * (usado pela Lisura com a data de realização do item do espelho).
     * Mais recente primeiro, caso haja mais de um.
     */
    @Query("""
            SELECT ep FROM ExamePrestador ep
            WHERE ep.exame.catalogoTuss.codTuss = :codTuss
              AND ep.prestador.idPrestador = :idPrestador
              AND ep.status = 'ATIVO'
              AND ep.inicioVigencia <= :data
              AND (ep.fimVigencia IS NULL OR ep.fimVigencia >= :data)
            ORDER BY ep.inicioVigencia DESC
            """)
    List<ExamePrestador> findVigentesByCodTussAndPrestador(
            @Param("codTuss") String codTuss,
            @Param("idPrestador") Long idPrestador,
            @Param("data") LocalDate data
    );
}