package br.com.bughunters.fusexflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Um item do espelho na visão da Lisura: valor cobrado pela OCS
 * lado a lado com o valor do contrato (EXAME_PRESTADOR).
 *
 * situacao:
 *   CONFORME     -> cobrado = contratual
 *   ACIMA        -> cobrado > contratual (cobrança a maior)
 *   ABAIXO       -> cobrado < contratual
 *   SEM_CONTRATO -> não há valor contratual vigente para o procedimento
 */
public record LisuraItemResponse(
        Long idItem,
        String codTuss,
        String descricao,
        LocalDate dataRealizacao,
        Integer quantidade,
        BigDecimal valorCobradoUnitario,
        BigDecimal valorContratualUnitario,
        BigDecimal valorCobradoTotal,
        BigDecimal valorContratualTotal,
        BigDecimal diferenca,
        String situacao
) {}
