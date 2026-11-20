package br.com.bughunters.fusexflow.dto.response;

import java.time.LocalDateTime;

/**
 * Uma etapa da linha do tempo da guia (US17).
 *
 * As seis etapas são sempre devolvidas, concluídas ou não, para a timeline
 * conseguir desenhar o caminho inteiro e mostrar o que ainda falta.
 *
 * @param etapa      nome técnico da etapa (SOLICITACAO, EMISSAO, ...)
 * @param rotulo     nome para exibir ("Solicitação", "Emissão", ...)
 * @param descricao  o que acontece nessa etapa
 * @param ordem      posição no fluxo, de 1 a 6
 * @param concluida  se a etapa já aconteceu
 * @param atual      se é a etapa mais recente concluída (onde o processo está)
 * @param status     status registrado ao concluir; nulo se não concluída
 * @param ocorridaEm quando foi concluída; nulo se não concluída
 * @param responsavel quem registrou; nulo quando não houve usuário do FUSEx
 * @param observacao  texto curto sobre o evento
 */
public record EtapaAcompanhamentoResponse(
        String etapa,
        String rotulo,
        String descricao,
        int ordem,
        boolean concluida,
        boolean atual,
        String status,
        LocalDateTime ocorridaEm,
        String responsavel,
        String observacao
) {}
