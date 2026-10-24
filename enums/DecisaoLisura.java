package br.com.bughunters.fusexflow.enums;

/** Decisão do analista da Lisura para um item do espelho. */
public enum DecisaoLisura {
    /** Paga o valor do contrato (EXAME_PRESTADOR). */
    ACATAR_CONTRATUAL,
    /** Mantém o valor cobrado pela OCS. */
    MANTER_COBRADO,
    /** O analista informa o valor unitário a pagar. */
    VALOR_AJUSTADO
}
