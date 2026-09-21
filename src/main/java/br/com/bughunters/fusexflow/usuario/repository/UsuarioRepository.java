package br.com.bughunters.fusexflow.usuario.repository;

import br.com.bughunters.fusexflow.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByNomeUsuario(String nomeUsuario);

    boolean existsByPrecCp(String precCp);
}