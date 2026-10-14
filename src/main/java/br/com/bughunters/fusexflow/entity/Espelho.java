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

    public boolean isEditavel() {
        return status == StatusEspelho.RASCUNHO;
    }

    public void marcarAtualizado() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public void enviar() {
        this.status = StatusEspelho.ENVIADO;
        this.enviadoEm = LocalDateTime.now();
    }

    public Long getIdEspelho() { return idEspelho; }
    public GuiaOficial getGuiaOficial() { return guiaOficial; }
    public Prestador getPrestador() { return prestador; }
    public StatusEspelho getStatus() { return status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public LocalDateTime getEnviadoEm() { return enviadoEm; }
}