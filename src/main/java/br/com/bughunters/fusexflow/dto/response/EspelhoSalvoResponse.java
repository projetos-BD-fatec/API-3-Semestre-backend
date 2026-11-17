package br.com.bughunters.fusexflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Linha da lista "espelhos salvos" da OCS (ainda não enviados ao FUSEx). */
public record EspelhoSalvoResponse(
        Long idEspelho,
        Long idGuia,
        String codigoGuia,
        String nomeBeneficiario,
        String precCp,
        LocalDateTime salvoEm,
        int quantidadeItens,
        BigDecimal valorTotal
) {}
