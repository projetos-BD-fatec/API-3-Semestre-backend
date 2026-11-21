package br.com.bughunters.fusexflow.dto.response;

import java.time.LocalDateTime;

/**
 * Linha da listagem de acompanhamento do FUSEx (US17): uma guia e a etapa
 * do fluxo em que ela está.
 *
 * @param idGuia           guia oficial
 * @param codigoGuia       código da guia
 * @param nomeBeneficiario quem abriu a solicitação
 * @param statusGuia       status atual da guia
 * @param etapaAtual       etapa em que o processo está
 * @param rotuloEtapaAtual nome da etapa para exibir
 * @param ordemEtapaAtual  posição da etapa no fluxo, de 1 a 6
 * @param atualizadoEm     data do último evento registrado
 * @param concluido        se o processo chegou à liquidação
 */
public record AcompanhamentoResumoResponse(
        Long idGuia,
        String codigoGuia,
        String nomeBeneficiario,
        String statusGuia,
        String etapaAtual,
        String rotuloEtapaAtual,
        int ordemEtapaAtual,
        LocalDateTime atualizadoEm,
        boolean concluido
) {}
