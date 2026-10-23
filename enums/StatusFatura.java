package br.com.bughunters.fusexflow.enums;

/**
 * Filtro de status aceito pela rota GET /faturas (visão interna do FUSEX).
 *
 * Toda guia que tem espelho já está UTILIZADA (o espelho só é aberto para guias
 * utilizadas), então o status é avaliado sobre o espelho:
 *   UTILIZADAS -> espelho ENVIADO   (a OCS já enviou o espelho)
 *   PENDENTES  -> espelho RASCUNHO  (espelho aberto, ainda não enviado)
 */
public enum StatusFatura {
    UTILIZADAS(StatusEspelho.ENVIADO),
    PENDENTES(StatusEspelho.RASCUNHO);

    private final StatusEspelho statusEspelho;

    StatusFatura(StatusEspelho statusEspelho) {
        this.statusEspelho = statusEspelho;
    }

    public StatusEspelho toStatusEspelho() {
        return statusEspelho;
    }
}
