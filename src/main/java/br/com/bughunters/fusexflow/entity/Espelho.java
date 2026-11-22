package br.com.bughunters.fusexflow.entity;

import br.com.bughunters.fusexflow.enums.StatusEspelho;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ESPELHO")
public class Espelho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ESPELHO")
    private Long idEspelho;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_GUIA_OFICIAL", nullable = false, unique = true)
    private GuiaOficial guiaOficial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PRESTADOR", nullable = false)
    private Prestador prestador;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    private StatusEspelho status;

    @Column(name = "CRIADO_EM", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "ATUALIZADO_EM")
    private LocalDateTime atualizadoEm;

    @Column(name = "SALVO_EM")
    private LocalDateTime salvoEm;

    @Column(name = "ENVIADO_EM")
    private LocalDateTime enviadoEm;

    protected Espelho() {
    }

    public Espelho(GuiaOficial guiaOficial, Prestador prestador) {
        this.guiaOficial = guiaOficial;
        this.prestador = prestador;
        this.status = StatusEspelho.RASCUNHO;
        this.criadoEm = LocalDateTime.now();
    }

    /** A OCS pode alterar os itens enquanto o espelho não foi enviado ao FUSEx. */
    public boolean isEditavel() {
        return status == StatusEspelho.RASCUNHO || status == StatusEspelho.SALVO;
    }

    public boolean isSalvo() {
        return status == StatusEspelho.SALVO;
    }

    public boolean isEnviado() {
        return status == StatusEspelho.ENVIADO;
    }

    public void marcarAtualizado() {
        this.atualizadoEm = LocalDateTime.now();
    }

    /** Conclui o espelho: continua com a OCS até ela enviá-lo ao FUSEx. */
    public void salvar() {
        this.status = StatusEspelho.SALVO;
        this.salvoEm = LocalDateTime.now();
    }

    /** Volta para rascunho (ex.: o último item de um espelho salvo foi excluído). */
    public void voltarParaRascunho() {
        this.status = StatusEspelho.RASCUNHO;
        this.salvoEm = null;
    }

    /** Envia ao FUSEx. Em lote, todos os espelhos do lote recebem o mesmo horário. */
    public void enviar(LocalDateTime quando) {
        this.status = StatusEspelho.ENVIADO;
        this.enviadoEm = quando;
    }

    public Long getIdEspelho() { return idEspelho; }
    public GuiaOficial getGuiaOficial() { return guiaOficial; }
    public Prestador getPrestador() { return prestador; }
    public StatusEspelho getStatus() { return status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public LocalDateTime getSalvoEm() { return salvoEm; }
    public LocalDateTime getEnviadoEm() { return enviadoEm; }
}