package br.com.bughunters.fusexflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "PRESTADOR",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_PRESTADOR_DOC", columnNames = "NR_DOCUMENTO")
        }
)
public class Prestador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PRESTADOR")
    private Long idPrestador;

    @Column(name = "NM_PRESTADOR", length = 200, nullable = false)
    private String nmPrestador;

    @Column(name = "NM_FANTASIA", length = 200)
    private String nmFantasia;

    @Column(name = "TP_PRESTADOR", length = 10, nullable = false)
    private String tpPrestador;

    @Column(name = "TP_DOCUMENTO", length = 4, nullable = false)
    private String tpDocumento;

    @Column(name = "NR_DOCUMENTO", length = 14)
    private String nrDocumento;

    @Column(name = "DS_LOGRADOURO", length = 200)
    private String dsLogradouro;

    @Column(name = "NR_ENDERECO", length = 20)
    private String nrEndereco;

    @Column(name = "DS_COMPLEMENTO", length = 100)
    private String dsComplemento;

    @Column(name = "NM_BAIRRO", length = 100)
    private String nmBairro;

    @Column(name = "NM_CIDADE", length = 100)
    private String nmCidade;

    @Column(name = "SG_UF", length = 2)
    private String sgUf;

    @Column(name = "NR_CEP", length = 8)
    private String nrCep;

    @Column(name = "FL_ATIVO", length = 1, nullable = false)
    private String flAtivo;

    @Column(name = "CRIADO_EM", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "ATUALIZADO_EM")
    private LocalDateTime atualizadoEm;

    protected Prestador() {
    }

    public Prestador(String nmPrestador, String nmFantasia, String tpPrestador, String tpDocumento, String nrDocumento, String dsLogradouro, String nrEndereco, String dsComplemento, String nmBairro, String nmCidade, String sgUf, String nrCep) {
        this.nmPrestador = nmPrestador;
        this.nmFantasia = nmFantasia;
        this.tpPrestador = tpPrestador;
        this.tpDocumento = tpDocumento;
        this.nrDocumento = nrDocumento;
        this.dsLogradouro = dsLogradouro;
        this.nrEndereco = nrEndereco;
        this.dsComplemento = dsComplemento;
        this.nmBairro = nmBairro;
        this.nmCidade = nmCidade;
        this.sgUf = sgUf;
        this.nrCep = nrCep;
    }

    public void setIdPrestador(Long idPrestador) {
        this.idPrestador = idPrestador;
    }

    public Long getIdPrestador() {
        return idPrestador;
    }

    public String getNmPrestador() {
        return nmPrestador;
    }

    public String getNmFantasia() {
        return nmFantasia;
    }

    public String getDsLogradouro() {
        return dsLogradouro;
    }

    public String getNrEndereco() {
        return nrEndereco;
    }

    public String getDsComplemento() {
        return dsComplemento;
    }

    public String getNmBairro() {
        return nmBairro;
    }

    public String getNmCidade() {
        return nmCidade;
    }

    public String getSgUf() {
        return sgUf;
    }
}