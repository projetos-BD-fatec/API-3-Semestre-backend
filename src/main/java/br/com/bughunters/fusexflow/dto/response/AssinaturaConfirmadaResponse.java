package br.com.bughunters.fusexflow.dto.response;

import java.time.LocalDateTime;

public record AssinaturaConfirmadaResponse(
        Long idGuia,
        LocalDateTime dataAssinatura
) {}
