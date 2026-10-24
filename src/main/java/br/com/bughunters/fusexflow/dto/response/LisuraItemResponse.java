package br.com.bughunters.fusexflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Um item do espelho na visão da Lisura: valor cobrado pela OCS
 * lado a lado com o valor do contrato (EXAME_PRESTADOR).
 *
 * situacao:
 *   CONFORME     -> cobrado = contratual
 *   ACIMA        -> cobrado > contratual (cobrança a maior)
 *   ABAIXO       -> cobrado < contratual
 *   SEM_CONTRATO -> não há valor contratual vigente para o procedimento
 *
 * procedimentoNaGuia (confirmação feita pela OCS no painel do prestador):
 *   CONFIRMADO     -> procedimento da guia marcado como realizado
 *   NAO_CONFIRMADO -> está na guia, mas a OCS não confirmou a realização
 *   FORA_DA_GUIA   -> cobrado no espelho, mas não consta na guia
 *
 * decisao / valorFinal* / revisado*: preenchidos depois da revisão
 * (PUT /lisura/{guiaId}/revisao); nulos enquanto o item não foi revisado.
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
        String situacao,
        String procedimentoNaGuia,
        LocalDateTime realizadoEm,
        String decisao,
        BigDecimal valorFinalUnitario,
        BigDecimal valorFinalTotal,
        String revisadoPor,
        LocalDateTime revisadoEm
) {}
