package br.com.bughunters.fusexflow.entity;

import br.com.bughunters.fusexflow.enums.StatusGuia;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "guia_oficial")
public class GuiaOficial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_guia_oficial")
    private Long id;

    @Column(name = "solicitacao_id", nullable = false)
    private Long solicitacaoId;

    @Column(name = "codigo_guia", length = 50, unique = true)
    private String codigoGuia;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "caminho_arquivo", nullable = false)
    private String caminhoArquivo;

    @Column(name = "data_upload", nullable = false)
    private LocalDateTime dataUpload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusGuia status;

    // ---------- validade e assinatura do beneficiário ----------

    /** Último dia em que a guia pode ser utilizada (inclusive). */
    @Column(name = "dt_validade")
    private LocalDate dataValidade;

    /** Quando o beneficiário assinou (confirmou) a utilização da guia. */
    @Column(name = "dt_assinatura")
    private LocalDateTime dataAssinatura;

    /** Beneficiário que assinou. */
    @Column(name = "id_usuario_assinante")
    private Long idUsuarioAssinante;

    // ---------- liquidação: última etapa do fluxo (US17 / SCRUM-65) ----------
    // Fica em colunas próprias em vez de virar um novo valor de StatusGuia
    // para não apagar o resultado da Lisura: uma guia liquidada continua
    // sendo uma guia LISURA_APROVADA.

    @Column(name = "liquidado_em")
    private LocalDateTime liquidadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_liquidacao")
    private Usuario usuarioLiquidacao;

    /** Total aprovado processado na liquidação (US16 / SCRUM-62). */
    @Column(name = "valor_liquidado", precision = 14, scale = 2)
    private java.math.BigDecimal valorLiquidado;

    // Construtor exigido pelo JPA
    public GuiaOficial() {
    }

    // Construtor completo
    public GuiaOficial(
            Long id,
            Long solicitacaoId,
            String codigoGuia,
            String nomeArquivo,
            String caminhoArquivo,
            LocalDateTime dataUpload,
            StatusGuia status) {

        this.id = id;
        this.solicitacaoId = solicitacaoId;
        this.codigoGuia = codigoGuia;
        this.nomeArquivo = nomeArquivo;
        this.caminhoArquivo = caminhoArquivo;
        this.dataUpload = dataUpload;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSolicitacaoId() {
        return solicitacaoId;
    }

    public void setSolicitacaoId(Long solicitacaoId) {
        this.solicitacaoId = solicitacaoId;
    }

    public String getCodigoGuia() {
        return codigoGuia;
    }

    public void setCodigoGuia(String codigoGuia) {
        this.codigoGuia = codigoGuia;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public void setCaminhoArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    public LocalDateTime getDataUpload() {
        return dataUpload;
    }

    public void setDataUpload(LocalDateTime dataUpload) {
        this.dataUpload = dataUpload;
    }

    public StatusGuia getStatus() {
        return status;
    }

    public void setStatus(StatusGuia status) {
        this.status = status;
    }

    // ---------- validade e assinatura ----------

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }

    public LocalDateTime getDataAssinatura() {
        return dataAssinatura;
    }

    public Long getIdUsuarioAssinante() {
        return idUsuarioAssinante;
    }

    /** O beneficiário já assinou a utilização desta guia. */
    public boolean isAssinada() {
        return dataAssinatura != null;
    }

    /** Passou do último dia de validade (guias sem validade definida não vencem). */
    public boolean vencidaEm(LocalDate hoje) {
        return dataValidade != null && hoje.isAfter(dataValidade);
    }

    public void assinar(Long idUsuarioAssinante, LocalDateTime quando) {
        this.idUsuarioAssinante = idUsuarioAssinante;
        this.dataAssinatura = quando;
    }

    // ---------- liquidação ----------

    public LocalDateTime getLiquidadoEm() {
        return liquidadoEm;
    }

    public Usuario getUsuarioLiquidacao() {
        return usuarioLiquidacao;
    }

    public java.math.BigDecimal getValorLiquidado() {
        return valorLiquidado;
    }

    /** A guia já foi paga pelo FUSEx. */
    public boolean foiLiquidada() {
        return liquidadoEm != null;
    }

    /**
     * Registra a liquidação: quem, quando e por quanto.
     *
     * As três informações andam juntas por causa da CK_GUIA_LIQUIDACAO —
     * ou a guia está liquidada por inteiro, ou não está. Isso impede meia
     * liquidação gravada por engano.
     */
    public void liquidar(Usuario responsavel, java.math.BigDecimal valor) {
        if (responsavel == null) {
            throw new IllegalArgumentException("A liquidação precisa de um responsável.");
        }
        if (valor == null || valor.signum() < 0) {
            throw new IllegalArgumentException("A liquidação precisa de um valor não negativo.");
        }
        this.liquidadoEm = LocalDateTime.now();
        this.usuarioLiquidacao = responsavel;
        this.valorLiquidado = valor;
    }
}