package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.HistoricoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatus, Long> {

    /**
     * Trilha completa de um processo, em ordem cronológica (SCRUM-65).
     *
     * Busca pela pré-guia porque ela é a raiz do fluxo: a etapa de solicitação
     * acontece antes de a guia oficial existir, então consultar só pelo id da
     * guia perderia o primeiro evento da linha do tempo.
     *
     * O desempate por id garante ordem estável quando dois eventos caem no
     * mesmo instante (acontece em carga de dados e em testes).
     */
    @Query("""
            SELECT h
              FROM HistoricoStatus h
              LEFT JOIN FETCH h.usuario
             WHERE h.idPreGuia = :idPreGuia
             ORDER BY h.registradoEm ASC, h.idHistorico ASC
            """)
    List<HistoricoStatus> buscarTrilhaDoProcesso(@Param("idPreGuia") Long idPreGuia);

    /**
     * Eventos de todas as guias já emitidas, em ordem cronológica.
     *
     * Serve à listagem do acompanhamento: o serviço agrupa por guia e o último
     * evento de cada uma é a etapa em que ela está. É uma consulta só para
     * todas as guias (em vez de uma por guia) justamente para não cair no
     * problema de N+1 ao montar a tela.
     */
    @Query("""
            SELECT h
              FROM HistoricoStatus h
             WHERE h.idGuiaOficial IS NOT NULL
             ORDER BY h.registradoEm ASC, h.idHistorico ASC
            """)
    List<HistoricoStatus> buscarEventosDasGuias();
}
