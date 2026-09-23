package br.com.bughunters.fusexflow.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "CATALOGO_TUSS")
public class CatalogoTuss {

    @Id
    @Column(name = "COD_TUSS", length = 50)
    private String codTuss;

    @Column(name = "DESC_TUSS", length = 250, nullable = false)
    private String descTuss;

    @Column(name = "CATEGORIA", length = 200)
    private String categoria;

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
            String categoria,
            String tabelaTuss,
            LocalDate inicioVigencia,
            LocalDate fimVigencia
    ) {
        this.codTuss = codTuss;
        this.descTuss = descTuss;
        this.categoria = categoria;
        this.tabelaTuss = tabelaTuss;
        this.inicioVigencia = inicioVigencia;
        this.fimVigencia = fimVigencia;
    }

    public String getCodTuss() {
        return codTuss;
    }
}