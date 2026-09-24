package br.com.bughunters.fusexflow.entity;

import br.com.bughunters.fusexflow.enums.StatusPreGuia;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.BigInteger;

@Entity
@Table(name = "PRE_GUIA_ITEM")
public class PreGuiaItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PRE_GUIA_ITEM")
    private Long idPreGuiaItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PRE_GUIA", nullable = false)
    private PreGuia preGuia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PRESTADOR", nullable = false)
    private Prestador prestador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EXAME", nullable = false)
    private Exame exame;

    @Column(name = "VALOR", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    private StatusPreGuia status;

    protected PreGuiaItem() {
    }

    public PreGuiaItem(PreGuia preGuia, Prestador prestador, Exame exame, BigDecimal valor) {
        this.preGuia = preGuia;
        this.prestador = prestador;
        this.exame = exame;
        this.valor = valor;
        this.status = StatusPreGuia.EM_ANALISE;
    }

    public void setIdPreGuiaItem(Long idPreGuiaItem) {
        this.idPreGuiaItem = idPreGuiaItem;
    }

    public Long getIdPreGuiaItem() {
        return idPreGuiaItem;
    }

    public PreGuia getPreGuia() {
        return preGuia;
    }

    public Prestador getPrestador() {
        return prestador;
    }

    public Exame getExame() {
        return exame;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public StatusPreGuia getStatus() {
        return status;
    }
}
