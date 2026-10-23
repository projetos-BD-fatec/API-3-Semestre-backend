package br.com.bughunters.fusexflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Espelho enviado pela OCS, comparado com os valores de contrato.
 *
 * totalFinal: soma do que será pago — valor revisado nos itens com revisão,
 *             valor cobrado nos itens conformes. Itens pendentes não entram.
 * itensPendentes: itens divergentes que ainda não foram revisados.
 * itensNaoConfirmados: itens cobrados que a OCS não confirmou como realizados
 *                      na guia (NAO_CONFIRMADO) ou que nem estão na guia (FORA_DA_GUIA).
 */
public record LisuraEspelhoResponse(
        Long idEspelho,
        Long idGuia,
        String codigoGuia,
        String nomeBeneficiario,
        String nomePrestador,
        String status,
        LocalDateTime enviadoEm,
        List<LisuraItemResponse> itens,
        BigDecimal totalCobrado,
        BigDecimal totalContratual,
        BigDecimal diferencaTotal,
        long itensDivergentes,
        BigDecimal totalFinal,
        long itensPendentes,
        long itensNaoConfirmados
) {}
