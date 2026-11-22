package br.com.bughunters.fusexflow.enums;

public enum StatusEspelho {
    /** Em preenchimento pela OCS. */
    RASCUNHO,
    /** Concluído pela OCS, mas ainda em posse dela (aguardando envio em lote ao FUSEx). */
    SALVO,
    /** Enviado ao FUSEx; não pode mais ser alterado. */
    ENVIADO
}
