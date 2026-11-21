package br.com.bughunters.fusexflow.dto.response;

import java.time.LocalDateTime;

/** O código NÃO é devolvido à OCS: só o beneficiário o vê. */
public record AssinaturaSolicitadaResponse(
        LocalDateTime expiraEm,
        int validadeMinutos
) {}
