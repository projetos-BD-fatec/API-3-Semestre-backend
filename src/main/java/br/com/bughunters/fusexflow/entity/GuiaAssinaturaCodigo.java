package br.com.bughunters.fusexflow.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Código de 6 dígitos que o beneficiário vê no app dele e informa à OCS para
 * "assinar" a guia utilizada. Vale poucos minutos, é de uso único e tem número
 * limitado de tentativas.
 */
@Entity
@Table(name = "guia_assinatura_codigo")
public class GuiaAssinaturaCodigo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_codigo")
    private Long idCodigo;

    @Column(name = "id_guia_oficial", nullable = false)
    private Long idGuiaOficial;

    @Column(name = "codigo", nullable = false, length = 6)
    private String codigo;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "tentativas", nullable = false)
    private int tentativas;

    /** Preenchido quando o código foi usado, substituído por outro ou bloqueado. */
    @Column(name = "invalidado_em")
    private LocalDateTime invalidadoEm;

    protected GuiaAssinaturaCodigo() {
    }

    public GuiaAssinaturaCodigo(Long idGuiaOficial, String codigo, LocalDateTime criadoEm, LocalDateTime expiraEm) {
        this.idGuiaOficial = idGuiaOficial;
        this.codigo = codigo;
        this.criadoEm = criadoEm;
        this.expiraEm = expiraEm;
        this.tentativas = 0;
    }

    public void registrarTentativaErrada() {
        this.tentativas++;
    }

    public void invalidar(LocalDateTime quando) {
        if (this.invalidadoEm == null) {
            this.invalidadoEm = quando;
        }
    }

    public Long getIdCodigo() { return idCodigo; }
    public Long getIdGuiaOficial() { return idGuiaOficial; }
    public String getCodigo() { return codigo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
    public int getTentativas() { return tentativas; }
    public LocalDateTime getInvalidadoEm() { return invalidadoEm; }
}
