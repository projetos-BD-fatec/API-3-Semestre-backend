package br.com.bughunters.fusexflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USUARIO")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO")
    private Long idUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PRESTADOR")
    private Prestador prestador;

    @Column(name = "ID_UG")
    private Long idUg;

    @Column(name = "NM_USUARIO", length = 150, nullable = false)
    private String nmUsuario;

    @Column(name = "DS_EMAIL", length = 150, nullable = false)
    private String dsEmail;

    @Column(name = "DS_SENHA_HASH", length = 200, nullable = false)
    private String dsSenhaHash;

    @Column(name = "NR_CPF", length = 11)
    private String nrCpf;

    @Column(name = "NR_TELEFONE", length = 20)
    private String nrTelefone;

    @Column(name = "FL_ATIVO", length = 1, nullable = false)
    private String flAtivo;

    @Column(name = "DT_ULTIMO_ACESSO")
    private LocalDateTime dtUltimoAcesso;

    @Column(name = "CRIADO_EM", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "ATUALIZADO_EM")
    private LocalDateTime atualizadoEm;

    protected Usuario() {
    }

    public Usuario(
            String nmUsuario,
            String dsEmail,
            String dsSenhaHash,
            String nrCpf,
            String nrTelefone
    ) {
        this.nmUsuario = nmUsuario;
        this.dsEmail = dsEmail;
        this.dsSenhaHash = dsSenhaHash;
        this.nrCpf = nrCpf;
        this.nrTelefone = nrTelefone;
    }

}