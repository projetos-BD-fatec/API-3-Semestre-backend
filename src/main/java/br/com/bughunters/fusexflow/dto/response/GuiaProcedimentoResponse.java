package br.com.bughunters.fusexflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GuiaProcedimentoResponse(
        Long idItem,
        String codigoTuss,
        String nomeProcedimento,
        BigDecimal valor,
        boolean realizado,
        LocalDateTime dataRealizacao
) {
}