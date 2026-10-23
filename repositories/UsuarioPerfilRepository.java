package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.UsuarioPerfil;
import br.com.bughunters.fusexflow.entity.UsuarioPerfilId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioPerfilRepository extends JpaRepository<UsuarioPerfil, UsuarioPerfilId> {
    List<UsuarioPerfil> findByUsuario_IdUsuario(Long idUsuario);
}