package br.com.bughunters.fusexflow.dto.request;

public record PreGuiaItemRequest (
        Long idExame,
        Long idPrestador
) {
}
