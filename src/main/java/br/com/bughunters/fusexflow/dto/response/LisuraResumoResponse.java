package br.com.bughunters.fusexflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Linha da lista de espelhos enviados pelas OCS (entrada da Lisura). */
public record LisuraResumoResponse(
        Long idGuia,
        String codigoGuia,
        String nomeBeneficiario,
        String nomePrestador,
        LocalDateTime enviadoEm,
        int totalItens,
        BigDecimal totalCobrado,
        long itensDivergentes,
        long itensPendentes,
        long itensNaoConfirmados
) {}
