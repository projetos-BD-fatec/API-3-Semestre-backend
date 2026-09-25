package br.com.bughunters.fusexflow.dto.response;

public record LoginResponse(
        String token,
        String tipo,
        Long idUsuario,
        String nomeUsuario
) {
}