package br.com.bughunters.fusexflow.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PreGuiaResponse(
        Long idPreGuia,
        String nmArquivo,
        String status,
        LocalDateTime criadoEm,
        List<PreGuiaItemResponse> itens
) {}