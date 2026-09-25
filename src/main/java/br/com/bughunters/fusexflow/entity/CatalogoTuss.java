package br.com.bughunters.fusexflow.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "CATALOGO_TUSS")
public class CatalogoTuss {

    @Id
    @Column(name = "COD_TUSS", length = 200)
    private String codTuss;

    @Column(name = "DESC_TUSS", length = 250, nullable = false)
    private String descTuss;

    @Column(name = "TABELA_TUSS", length = 50)
    private String tabelaTuss;

    @Column(name = "INICIO_VIGENCIA")
    private LocalDate inicioVigencia;

    @Column(name = "FIM_VIGENCIA")
    private LocalDate fimVigencia;

    protected CatalogoTuss() {
    }

    public CatalogoTuss(
            String codTuss,
            String descTuss,
            String tabelaTuss,
            LocalDate inicioVigencia,
            LocalDate fimVigencia
    ) {
        this.codTuss = codTuss;
        this.descTuss = descTuss;
        this.tabelaTuss = tabelaTuss;
        this.inicioVigencia = inicioVigencia;
        this.fimVigencia = fimVigencia;
    }

    public String getCodTuss() {
        return codTuss;
    }
}