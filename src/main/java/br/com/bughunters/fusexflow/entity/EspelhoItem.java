package br.com.bughunters.fusexflow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ESPELHO_ITEM")
public class EspelhoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ESPELHO_ITEM")
    private Long idEspelhoItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ESPELHO", nullable = false)
    private Espelho espelho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COD_TUSS", nullable = false)
    private CatalogoTuss catalogoTuss;

    @Column(name = "DT_REALIZACAO", nullable = false)
    private LocalDate dtRealizacao;

    @Column(name = "QUANTIDADE", nullable = false)
    private Integer quantidade;

    @Column(name = "VALOR_UNITARIO", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorUnitario;

    protected EspelhoItem() {
    }

    public EspelhoItem(Espelho espelho, CatalogoTuss catalogoTuss, LocalDate dtRealizacao,
                       Integer quantidade, BigDecimal valorUnitario) {
        this.espelho = espelho;
        this.catalogoTuss = catalogoTuss;
        this.dtRealizacao = dtRealizacao;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    public void atualizar(CatalogoTuss catalogoTuss, LocalDate dtRealizacao,
                          Integer quantidade, BigDecimal valorUnitario) {
        this.catalogoTuss = catalogoTuss;
        this.dtRealizacao = dtRealizacao;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    public BigDecimal getValorTotal() {
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public Long getIdEspelhoItem() { return idEspelhoItem; }
    public Espelho getEspelho() { return espelho; }
    public CatalogoTuss getCatalogoTuss() { return catalogoTuss; }
    public LocalDate getDtRealizacao() { return dtRealizacao; }
    public Integer getQuantidade() { return quantidade; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
}