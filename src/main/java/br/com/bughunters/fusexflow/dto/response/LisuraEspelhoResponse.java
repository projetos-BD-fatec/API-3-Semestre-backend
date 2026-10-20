package br.com.bughunters.fusexflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Espelho enviado pela OCS, comparado com os valores de contrato. */
public record LisuraEspelhoResponse(
        Long idEspelho,
        Long codigoGuia,
        String nomeBeneficiario,
        String nomePrestador,
        String status,
        LocalDateTime enviadoEm,
        List<LisuraItemResponse> itens,
        BigDecimal totalCobrado,
        BigDecimal totalContratual,
        BigDecimal diferencaTotal,
        long itensDivergentes
) {}
