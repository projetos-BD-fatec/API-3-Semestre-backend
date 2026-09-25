package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.request.LoginRequest;
import br.com.bughunters.fusexflow.dto.response.LoginResponse;
import br.com.bughunters.fusexflow.entity.Usuario;
import br.com.bughunters.fusexflow.repositories.UsuarioPerfilRepository;
import br.com.bughunters.fusexflow.repositories.UsuarioRepository;
import br.com.bughunters.fusexflow.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioPerfilRepository usuarioPerfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       UsuarioPerfilRepository usuarioPerfilRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioPerfilRepository = usuarioPerfilRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        // PONTO ÚNICO 2/2 — mesmo critério do CustomUserDetailsService.
        Usuario usuario = usuarioRepository.findByDsEmail(request.login())
                .orElseThrow(() -> new BadCredentialsException("Login ou senha inválidos"));

        if (!passwordEncoder.matches(request.senha(), usuario.getDsSenhaHash())) {
            throw new BadCredentialsException("Login ou senha inválidos");
        }

        if (!"S".equals(usuario.getFlAtivo())) {
            throw new BadCredentialsException("Usuário inativo");
        }

        List<String> perfis = usuarioPerfilRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .stream()
                .map(perfil -> perfil.getCdPerfil())
                .toList();

        String token = jwtService.gerarToken(usuario.getIdUsuario(), usuario.getDsEmail(), perfis);

        return new LoginResponse(token, "Bearer", usuario.getIdUsuario(), usuario.getNmUsuario());
    }
}