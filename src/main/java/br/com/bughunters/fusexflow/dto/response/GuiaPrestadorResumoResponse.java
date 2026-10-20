package br.com.bughunters.fusexflow.dto.response;

import java.time.LocalDateTime;

public record GuiaPrestadorResumoResponse(
        Long idGuia,
        Long idPreGuia,
        String codigoGuia,
        String nomePaciente,
        String statusGuia,
        LocalDateTime dataUpload,
        int procedimentosRealizados,
        int totalProcedimentos
) {
}