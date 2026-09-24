package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}