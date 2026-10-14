package br.com.bughunters.fusexflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EspelhoResponse(
        Long idEspelho,
        Long codigoGuia,
        String nomeBeneficiario,
        String status,
        LocalDateTime enviadoEm,
        List<EspelhoItemResponse> itens,
        BigDecimal valorTotal
) {}