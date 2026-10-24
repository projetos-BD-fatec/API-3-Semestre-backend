package br.com.bughunters.fusexflow.enums;

public enum StatusGuia {
    ATIVA,
    UTILIZADA,
    VENCIDA,
    CANCELADA,
    // Decisão final da Lisura (SCRUM-53)
    LISURA_APROVADA,
    LISURA_RECUSADA;

    /** A Lisura já aprovou ou recusou a guia: não aceita mais revisão. */
    public boolean decididaPelaLisura() {
        return this == LISURA_APROVADA || this == LISURA_RECUSADA;
    }

    /** A OCS já registrou a utilização (inclusive se a Lisura já decidiu depois). */
    public boolean foiUtilizada() {
        return this == UTILIZADA || decididaPelaLisura();
    }
}
