package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.request.UsuarioRequest;
import br.com.bughunters.fusexflow.dto.response.BeneficiarioResponse;
import br.com.bughunters.fusexflow.entity.Usuario;
import br.com.bughunters.fusexflow.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public Usuario cadastrar(UsuarioRequest request) {

        if (repository.existsByNrCpf(request.cpf())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        if (repository.existsByDsEmail(request.email())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        if (repository.existsByNmUsuario(request.nomeUsuario())) {
            throw new RuntimeException("Nome de usuário já cadastrado");
        }

        if (repository.existsByNrPrecCp(request.precCp())) {
            throw new RuntimeException("PREC/CP já cadastrado");
        }

        String senhaHash = passwordEncoder.encode(request.senha());

        Usuario usuario = new Usuario(
                request.nomeUsuario(),
                request.email(),
                senhaHash,
                request.precCp(),
                request.cpf(),
                request.telefone()
        );
        usuario.setFlAtivo("S");

        return repository.save(usuario);
    }

    /**
     * Dados do usuário autenticado (o login do token é o e-mail,
     * mesmo critério usado em PreGuiaService e AuthService).
     */
    public BeneficiarioResponse buscarLogado() {
        String loginAtual = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuario = repository.findByDsEmail(loginAtual)
                .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado: " + loginAtual));

        return new BeneficiarioResponse(
                usuario.getNmUsuario(),
                usuario.getNrPrecCp()
        );
    }
}
