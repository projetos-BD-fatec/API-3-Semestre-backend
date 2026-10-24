package br.com.bughunters.fusexflow.dto.request;

/**
 * Corpo do PATCH /lisura/{guiaId}/decisao.
 *
 * decisao: APROVAR (guia vai para LISURA_APROVADA) | RECUSAR (LISURA_RECUSADA)
 */
public record LisuraDecisaoRequest(String decisao) {}
