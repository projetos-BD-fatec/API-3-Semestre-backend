package br.com.bughunters.fusexflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PreGuiaResponse(
        Long idPreGuia,
        String nmArquivo,
        String status,
        LocalDateTime criadoEm,
        List<PreGuiaItemResponse> itens
) {}