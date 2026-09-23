package br.com.bughunters.fusexflow.entity;

import br.com.bughunters.fusexflow.enums.StatusPreGuia;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRE_GUIA")
public class PreGuia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PRE_GUIA")
    private Long idPreGuia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_USUARIO", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PRESTADOR", nullable = false)
    private Prestador prestador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EXAME", nullable = false)
    private Exame exame;

    @Column(name = "NM_ANEXO", length = 250)
    private String nmAnexo;

    @Column(name = "STATUS", length = 20)
    private StatusPreGuia status;

    @Column(name = "CRIADO_EM", nullable = false)
    private LocalDateTime criadoEm;

    protected PreGuia() {
    }

    public PreGuia(
            Usuario usuario,
            Prestador prestador,
            Exame exame,
            String nmAnexo
    ) {
        this.usuario = usuario;
        this.prestador = prestador;
        this.exame = exame;
        this.nmAnexo = nmAnexo;
        this.status = StatusPreGuia.EM_ANALISE;
        this.criadoEm = LocalDateTime.now();
    }

    public void setStatus(StatusPreGuia status) {
        this.status = status;
    }

    public Long getIdPreGuia() {
        return idPreGuia;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Prestador getPrestador() {
        return prestador;
    }

    public Exame getExame() {
        return exame;
    }

    public String getNmAnexo() {
        return nmAnexo;
    }

    public StatusPreGuia getStatus() {
        return status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}