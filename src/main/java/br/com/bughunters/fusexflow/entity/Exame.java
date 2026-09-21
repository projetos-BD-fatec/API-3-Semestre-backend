package br.com.bughunters.fusexflow.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "EXAME")
public class Exame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_EXAME")
    private Long idExame;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COD_TUSS", nullable = false)
    private CatalogoTuss catalogoTuss;

    @Column(name = "DESC_EXAME", length = 250, nullable = false)
    private String descExame;

    protected Exame() {
    }

    public Exame(
            CatalogoTuss catalogoTuss,
            String descExame
    ) {
        this.catalogoTuss = catalogoTuss;
        this.descExame = descExame;
    }

    public void setIdExame(Long idExame) {
        this.idExame = idExame;
    }
}
