package br.com.bughunters.fusexflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EspelhoItemResponse(
        Long idItem,
        String codTuss,
        String descricao,
        LocalDate dataRealizacao,
        Integer quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotal
) {}