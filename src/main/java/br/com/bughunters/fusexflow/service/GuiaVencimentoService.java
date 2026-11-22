package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.repositories.GuiaOficialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Marca como VENCIDA toda guia ATIVA cuja validade (GUIA_OFICIAL.DT_VALIDADE) já passou.
 *
 * Roda sempre que o backend sobe e todo dia no horário de
 * fusex.guia.vencimento-cron (application.properties).
 *
 * Mesmo que o job ainda não tenha rodado, as telas já mostram "Vencida" e a OCS
 * já não consegue usar a guia (a regra também é checada em GuiaOficialService).
 * Guias já UTILIZADAS não vencem: o uso já aconteceu.
 */
@Service
public class GuiaVencimentoService {

    private static final Logger log = LoggerFactory.getLogger(GuiaVencimentoService.class);

    private final GuiaOficialRepository repository;

    public GuiaVencimentoService(GuiaOficialRepository repository) {
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "${fusex.guia.vencimento-cron:0 5 0 * * *}")
    @Transactional
    public int marcarGuiasVencidas() {
        try {
            int total = repository.marcarVencidas(LocalDate.now());

            if (total > 0) {
                log.info("{} guia(s) marcada(s) como VENCIDA.", total);
            }

            return total;

        } catch (RuntimeException e) {
            // Uma falha aqui não pode derrubar o backend
            log.error("Não foi possível marcar as guias vencidas.", e);
            return 0;
        }
    }
}
