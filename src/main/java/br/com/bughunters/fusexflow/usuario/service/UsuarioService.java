package br.com.bughunters.fusexflow.usuario.service;

import br.com.bughunters.fusexflow.usuario.dto.UsuarioCadastroDTO;
import br.com.bughunters.fusexflow.usuario.model.Usuario;
import br.com.bughunters.fusexflow.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrar(UsuarioCadastroDTO dto) {

        if (repository.existsByCpf(dto.getCpf())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        if (repository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        if (repository.existsByNomeUsuario(dto.getNomeUsuario())) {
            throw new RuntimeException("Nome de usuário já cadastrado");
        }

        if (repository.existsByPrecCp(dto.getPrecCp())) {
            throw new RuntimeException("PREC/CP já cadastrado");
        }

        Usuario usuario = new Usuario();

        usuario.setNomeUsuario(dto.getNomeUsuario());
        usuario.setEmail(dto.getEmail());
        usuario.setCpf(dto.getCpf());
        usuario.setPrecCp(dto.getPrecCp());
        usuario.setTelefone(dto.getTelefone());

        usuario.setSenhaHash(
                passwordEncoder.encode(dto.getSenha())
        );

        usuario.setAtivo("S");

        usuario.setCriadoEm(LocalDateTime.now());
        usuario.setAtualizadoEm(LocalDateTime.now());

        return repository.save(usuario);
    }
}