package br.com.bughunters.fusexflow.dto;

public record PrestadorResponse(
        Long idPrestador,
        String nmPrestador,
        String nmFantasia,
        String dsLogradouro,
        String nrEndereco,
        String dsComplemento,
        String nmBairro,
        String nmCidade,
        String sgUf
) {
}
