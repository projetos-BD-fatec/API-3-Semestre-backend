package br.com.bughunters.fusexflow.dto;

public record PrestadorResponse(
        Long idPrestador,
        String nmPrestador,
        String nmFantasia,
        String nmCidade,
        String sgUf
) {
}
