package br.com.bughunters.fusexflow.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record GuiaPrestadorDetalheResponse(
        Long idGuia,
        Long idPreGuia,
        String codigoGuia,
        String nomePaciente,
        String statusGuia,
        boolean guiaUtilizada,
        LocalDateTime dataUpload,
        int procedimentosRealizados,
        int totalProcedimentos,
        List<GuiaProcedimentoResponse> procedimentos
) {
}