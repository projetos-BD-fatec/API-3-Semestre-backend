package br.com.bughunters.fusexflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PreGuiaResponse(
        Long idPreGuia,
        String nmExame,
        String nmPrestador,
        String dsLogradouro,
        String nrEndereco,
        String nmBairro,
        String nmCidade,
        String sgUf,
        BigDecimal valor,
        String status,
        LocalDateTime criadoEm
) {}