package br.com.bughunters.fusexflow.enums;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Etapas do fluxo acompanhado pelo FUSEx (US17), da solicitação à liquidação.
 *
 * A ordem é a do processo e é o que a linha do tempo usa para desenhar as
 * etapas sempre na mesma sequência, inclusive as que ainda não aconteceram.
 */
public enum EtapaFluxo {

    /** O beneficiário abriu a pré-guia. */
    SOLICITACAO(1, "Solicitação", "Pré-guia aberta pelo beneficiário"),

    /** O FUSEx anexou a guia oficial à solicitação. */
    EMISSAO(2, "Emissão", "Guia oficial emitida pelo FUSEx"),

    /** O prestador confirmou que a guia foi utilizada. */
    REALIZACAO(3, "Realização", "Procedimentos realizados pelo prestador"),

    /** A OCS enviou o espelho com os valores cobrados. */
    FATURAMENTO(4, "Faturamento", "Espelho enviado pela OCS"),

    /** A Lisura conferiu os valores e aprovou ou recusou. */
    LISURA(5, "Lisura", "Conferência de valores pela Lisura"),

    /** O FUSEx registrou o pagamento da guia. */
    LIQUIDACAO(6, "Liquidação", "Pagamento registrado pelo FUSEx");

    private final int ordem;
    private final String rotulo;
    private final String descricao;

    EtapaFluxo(int ordem, String rotulo, String descricao) {
        this.ordem = ordem;
        this.rotulo = rotulo;
        this.descricao = descricao;
    }

    public int getOrdem() {
        return ordem;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getDescricao() {
        return descricao;
    }

    /** As seis etapas na ordem do processo. */
    public static List<EtapaFluxo> naOrdemDoFluxo() {
        return Arrays.stream(values())
                .sorted(Comparator.comparingInt(EtapaFluxo::getOrdem))
                .toList();
    }

    /**
     * Converte o texto recebido na query string da listagem.
     * Devolve null quando o filtro vem vazio (sem filtro).
     */
    public static EtapaFluxo doFiltro(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Etapa inválida: " + valor
                    + ". Use uma destas: " + Arrays.toString(values()));
        }
    }
}
