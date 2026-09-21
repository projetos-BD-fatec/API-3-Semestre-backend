package br.com.bughunters.fusexflow.entity;

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
    private String status;

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
        this.status = "EM ANÁLISE";
        this.criadoEm = LocalDateTime.now();
    }

}