package br.com.bughunters.fusexflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Uma linha do resumo de liquidação (US16 / SCRUM-64).
 *
 * Mostra o que a OCS cobrou, o que a Lisura aprovou e por quê, para o
 * responsável conferir antes de confirmar. Item conforme não passa pela
 * revisão: o aprovado dele é o próprio cobrado, e decisao vem nula.
 *
 * @param codTuss         código do procedimento
 * @param descricao       descrição do procedimento
 * @param dataRealizacao  quando o procedimento foi realizado
 * @param quantidade      quantidade cobrada
 * @param valorCobrado    valor unitário cobrado pela OCS
 * @param valorAprovado   valor unitário aprovado pela Lisura
 * @param totalCobrado    valorCobrado x quantidade
 * @param totalAprovado   valorAprovado x quantidade
 * @param diferenca       totalCobrado - totalAprovado; positivo = glosa
 * @param decisao         decisão da Lisura; nula se o item não foi revisado
 * @param rotuloDecisao   a decisão em texto para a tela; nulo se não revisado
 */
public record LiquidacaoItemResponse(
        String codTuss,
        String descricao,
        LocalDate dataRealizacao,
        int quantidade,
        BigDecimal valorCobrado,
        BigDecimal valorAprovado,
        BigDecimal totalCobrado,
        BigDecimal totalAprovado,
        BigDecimal diferenca,
        String decisao,
        String rotuloDecisao
) {}
