package br.com.bughunters.fusexflow.service;

/**
 * Código de assinatura incorreto. Existe como classe própria para que a
 * transação NÃO seja desfeita: a tentativa errada precisa ficar registrada.
 */
public class CodigoAssinaturaInvalidoException extends RuntimeException {

    public CodigoAssinaturaInvalidoException(String message) {
        super(message);
    }
}
