package br.com.bughunters.fusexflow.entity;

import br.com.bughunters.fusexflow.enums.DecisaoLisura;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Revisão da Lisura para um item do espelho (1 revisão por item). */
@Entity
@Table(name = "LISURA_REVISAO_ITEM")
public class LisuraRevisaoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_REVISAO_ITEM")
    private Long idRevisaoItem;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ESPELHO_ITEM", nullable = false, unique = true)
    private EspelhoItem espelhoItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "DECISAO", length = 20, nullable = false)
    private DecisaoLisura decisao;

    @Column(name = "VALOR_FINAL_UNITARIO", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorFinalUnitario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_USUARIO", nullable = false)
    private Usuario revisadoPor;

    @Column(name = "REVISADO_EM", nullable = false)
    private LocalDateTime revisadoEm;

    protected LisuraRevisaoItem() {
    }

    public LisuraRevisaoItem(EspelhoItem espelhoItem) {
        this.espelhoItem = espelhoItem;
    }

    public void registrar(DecisaoLisura decisao, BigDecimal valorFinalUnitario, Usuario revisadoPor) {
        this.decisao = decisao;
        this.valorFinalUnitario = valorFinalUnitario;
        this.revisadoPor = revisadoPor;
        this.revisadoEm = LocalDateTime.now();
    }

    public Long getIdRevisaoItem() { return idRevisaoItem; }
    public EspelhoItem getEspelhoItem() { return espelhoItem; }
    public DecisaoLisura getDecisao() { return decisao; }
    public BigDecimal getValorFinalUnitario() { return valorFinalUnitario; }
    public Usuario getRevisadoPor() { return revisadoPor; }
    public LocalDateTime getRevisadoEm() { return revisadoEm; }
}
