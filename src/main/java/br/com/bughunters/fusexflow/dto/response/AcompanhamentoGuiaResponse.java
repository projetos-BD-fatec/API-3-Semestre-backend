package br.com.bughunters.fusexflow.dto.response;

import java.util.List;

/**
 * Acompanhamento completo de uma guia (US17): onde o processo está e por
 * quais etapas já passou, com a data de cada uma.
 *
 * @param idGuia          id da guia oficial; nulo se ainda não foi emitida
 * @param idPreGuia       id da solicitação, raiz do processo
 * @param codigoGuia      código da guia oficial, quando já emitida
 * @param nomeBeneficiario quem abriu a solicitação
 * @param statusGuia      status atual da guia oficial
 * @param etapaAtual      etapa em que o processo está
 * @param rotuloEtapaAtual nome da etapa atual para exibir
 * @param concluido       se o processo chegou à liquidação
 * @param etapas          as seis etapas do fluxo, em ordem
 */
public record AcompanhamentoGuiaResponse(
        Long idGuia,
        Long idPreGuia,
        String codigoGuia,
        String nomeBeneficiario,
        String statusGuia,
        String etapaAtual,
        String rotuloEtapaAtual,
        boolean concluido,
        List<EtapaAcompanhamentoResponse> etapas
) {}
