package br.com.bughunters.fusexflow.usuario.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "USUARIO")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO")
    private Long idUsuario;

    @Column(name = "ID_PRESTADOR")
    private Long idPrestador;

    @Column(name = "ID_UG")
    private Long idUg;

    @Column(name = "NM_USUARIO", nullable = false, length = 150)
    private String nomeUsuario;

    @Column(name = "DS_EMAIL", nullable = false, length = 150)
    private String email;

    @Column(name = "DS_SENHA_HASH", nullable = false, length = 200)
    private String senhaHash;

    @Column(name = "NR_CPF", nullable = false, length = 11)
    private String cpf;

    @Column(name = "NR_PREC_CP", nullable = false, length = 30)
    private String precCp;

    @Column(name = "NR_TELEFONE", length = 20)
    private String telefone;

    @Column(name = "FL_ATIVO", length = 1)
    private String ativo;

    @Column(name = "DT_ULTIMO_ACESSO")
    private LocalDateTime ultimoAcesso;

    @Column(name = "CRIADO_EM")
    private LocalDateTime criadoEm;

    @Column(name = "ATUALIZADO_EM")
    private LocalDateTime atualizadoEm;
}