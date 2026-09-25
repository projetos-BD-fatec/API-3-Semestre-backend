package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByNrCpf(String nrCpf);
    boolean existsByDsEmail(String dsEmail);
    boolean existsByNmUsuario(String nmUsuario);
    boolean existsByNrPrecCp(String nrPrecCp);

    Optional<Usuario> findByDsEmail(String dsEmail);
}