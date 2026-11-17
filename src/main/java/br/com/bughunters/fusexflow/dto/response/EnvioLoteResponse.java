package br.com.bughunters.fusexflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EnvioLoteResponse(
        int totalEnviados,
        BigDecimal valorTotal,
        LocalDateTime enviadoEm,
        List<Long> idsEspelho
) {}
