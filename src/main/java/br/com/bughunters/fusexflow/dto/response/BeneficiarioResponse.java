package br.com.bughunters.fusexflow.dto.response;

/**
 * Dados do beneficiário logado, usados na ficha da pré-guia
 * e na saudação da tela inicial.
 */
public record BeneficiarioResponse(
        String nmBeneficiario,
        String nrPrecCp
) {
}
