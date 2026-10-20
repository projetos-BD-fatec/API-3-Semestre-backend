package br.com.bughunters.fusexflow.dto.request;

import java.math.BigDecimal;
import java.util.List;

/**
 * Corpo do PUT /lisura/{guiaId}/revisao. Aceita um ou vários itens.
 *
 * decisao: ACATAR_CONTRATUAL | MANTER_COBRADO | VALOR_AJUSTADO
 * valorAjustado: obrigatório só para VALOR_AJUSTADO (valor unitário).
 */
public record LisuraRevisaoRequest(List<Item> itens) {

    public record Item(
            Long idItem,
            String decisao,
            BigDecimal valorAjustado
    ) {}
}
