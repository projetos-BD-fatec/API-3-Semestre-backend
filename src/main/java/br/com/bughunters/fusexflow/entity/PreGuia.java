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

    @Column(name = "NM_ANEXO", length = 250)
    private String nmAnexo;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    private StatusPreGuia status;

    @Column(name = "CRIADO_EM", nullable = false)
    private LocalDateTime criadoEm;

    protected PreGuia() {
    }

    public PreGuia(Usuario usuario, String nmAnexo) {
        this.usuario = usuario;
        this.nmAnexo = nmAnexo;
        this.status = StatusPreGuia.EM_ANALISE;
        this.criadoEm = LocalDateTime.now();
    }

    public PreGuia(Usuario usuario) {
        this.usuario = usuario;
        this.status = StatusPreGuia.EM_ANALISE;
        this.criadoEm = LocalDateTime.now();
    }

    public void setNmAnexo(String nmAnexo) {
        this.nmAnexo = nmAnexo;
    }

    public void setStatus(StatusPreGuia status) {
        this.status = status;
    }

    public void setIdPreGuia(Long idPreGuia) {
        this.idPreGuia = idPreGuia;
    }

    public Long getIdPreGuia() {
        return idPreGuia;
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