package br.com.bughunters.fusexflow.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "EXAME_PRESTADOR")
public class ExamePrestador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_EXAME_PRESTADOR")
    private Long idExamePrestador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EXAME", nullable = false)
    private Exame exame;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PRESTADOR", nullable = false)
    private Prestador prestador;

    @Column(name = "VALOR_CONTRATUAL", nullable = false)
    private BigDecimal valorContratual;

    @Column(name = "STATUS", length = 10, nullable = false)
    private String status;

    @Column(name = "DT_INICIO_VIGENCIA", nullable = false)
    private LocalDate inicioVigencia;

    @Column(name = "DT_FIM_VIGENCIA")
    private LocalDate fimVigencia;

    protected ExamePrestador() {

    }

    public ExamePrestador(Exame exame, Prestador prestador, BigDecimal valorContratual) {
        this.exame = exame;
        this.prestador = prestador;
        this.valorContratual = valorContratual;
        this.status = "ATIVO";
        this.inicioVigencia = LocalDate.now();
    }

    public Exame getExame() {
        return exame;
    }

    public Prestador getPrestador() {
        return prestador;
    }

    public BigDecimal getValorContratual() {
        return valorContratual;
    }
}
