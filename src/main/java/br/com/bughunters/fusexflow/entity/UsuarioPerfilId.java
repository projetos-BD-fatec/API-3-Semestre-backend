package br.com.bughunters.fusexflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UsuarioPerfilId implements Serializable {

    @Column(name = "ID_USUARIO")
    private Long idUsuario;

    @Column(name = "CD_PERFIL", length = 30)
    private String cdPerfil;

    protected UsuarioPerfilId() {
    }

    public UsuarioPerfilId(Long idUsuario, String cdPerfil) {
        this.idUsuario = idUsuario;
        this.cdPerfil = cdPerfil;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getCdPerfil() {
        return cdPerfil;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioPerfilId that)) return false;
        return Objects.equals(idUsuario, that.idUsuario) && Objects.equals(cdPerfil, that.cdPerfil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario, cdPerfil);
    }
}