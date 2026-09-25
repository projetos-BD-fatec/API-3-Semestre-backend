package br.com.bughunters.fusexflow.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "USUARIO_PERFIL")
public class UsuarioPerfil {

    @EmbeddedId
    private UsuarioPerfilId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUsuario")
    @JoinColumn(name = "ID_USUARIO")
    private Usuario usuario;

    protected UsuarioPerfil() {
    }

    public UsuarioPerfil(Usuario usuario, String cdPerfil) {
        this.usuario = usuario;
        this.id = new UsuarioPerfilId(usuario.getIdUsuario(), cdPerfil);
    }

    public String getCdPerfil() {
        return id.getCdPerfil();
    }

    public Usuario getUsuario() {
        return usuario;
    }
}