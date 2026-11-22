package br.com.bughunters.fusexflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Regras de negócio da guia que podem ser ajustadas sem mexer no código.
 * Os valores ficam em src/main/resources/application.properties
 * (os números depois de ":" são usados se a propriedade não existir).
 */
@Component
public class RegrasGuiaProperties {

    /** Prazo de validade da guia, em meses, contado a partir do upload (emissão). */
    @Value("${fusex.guia.validade-meses:3}")
    private int validadeMeses;

    /** Minutos de validade do código de assinatura. */
    @Value("${fusex.assinatura.codigo-validade-minutos:10}")
    private int codigoValidadeMinutos;

    /** Erros de código permitidos antes de bloqueá-lo. */
    @Value("${fusex.assinatura.max-tentativas:5}")
    private int maxTentativas;

    /** Códigos novos que a OCS pode pedir por guia na última hora. */
    @Value("${fusex.assinatura.max-solicitacoes-por-hora:10}")
    private int maxSolicitacoesPorHora;

    /** Último dia em que uma guia emitida em {@code emissao} pode ser utilizada (inclusive). */
    public LocalDate calcularValidade(LocalDateTime emissao) {
        return emissao.toLocalDate().plusMonths(validadeMeses);
    }

    public int getValidadeMeses() { return validadeMeses; }
    public int getCodigoValidadeMinutos() { return codigoValidadeMinutos; }
    public int getMaxTentativas() { return maxTentativas; }
    public int getMaxSolicitacoesPorHora() { return maxSolicitacoesPorHora; }
}
