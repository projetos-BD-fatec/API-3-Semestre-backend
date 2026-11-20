package br.com.bughunters.fusexflow.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/** Guia utilizada aguardando a assinatura do beneficiário logado (com o código para ele informar à OCS). */
public record AssinaturaPendenteResponse(
        Long idGuia,
        String codigoGuia,
        String nomePrestador,
        List<String> procedimentos,
        String codigo,
        LocalDateTime expiraEm,
        long segundosRestantes
) {}
