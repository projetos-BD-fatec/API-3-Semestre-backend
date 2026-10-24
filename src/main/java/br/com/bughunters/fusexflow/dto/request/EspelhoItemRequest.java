package br.com.bughunters.fusexflow.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EspelhoItemRequest(
        String codTuss,
        LocalDate dataRealizacao,
        Integer quantidade,
        BigDecimal valorUnitario
) {}