package br.com.bughunters.fusexflow.mock;

import br.com.bughunters.fusexflow.entity.Usuario;

import java.util.ArrayList;
import java.util.List;

public class UsuarioMock {

    private static final List<Usuario> usuarios = new ArrayList<>();

    static {
        Usuario usuario1 = new Usuario(
                "João da Silva",
                "joao.silva@email.com",
                "senha-hash",
                "12345678901",
                "12999999999"
        );

        usuario1.setIdUsuario(1L);

        Usuario usuario2 = new Usuario(
                "Maria Oliveira",
                "maria.oliveira@email.com",
                "senha-hash",
                "98765432100",
                "12988888888"
        );

        usuario2.setIdUsuario(2L);

        usuarios.add(usuario1);
        usuarios.add(usuario2);
    }

    public static List<Usuario> getUsuarios() {
        return usuarios;
    }
}