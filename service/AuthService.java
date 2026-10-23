
package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.request.LoginRequest;
import br.com.bughunters.fusexflow.dto.request.PrestadorLoginRequest;
import br.com.bughunters.fusexflow.dto.response.LoginResponse;
import br.com.bughunters.fusexflow.entity.Usuario;
import br.com.bughunters.fusexflow.repositories.UsuarioPerfilRepository;
import br.com.bughunters.fusexflow.repositories.UsuarioRepository;
import br.com.bughunters.fusexflow.security.JwtService;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioPerfilRepository usuarioPerfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            UsuarioPerfilRepository usuarioPerfilRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioPerfilRepository = usuarioPerfilRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /* =============================================
       LOGIN - BENEFICIÁRIO / FUNCIONÁRIO
    ============================================= */

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        Usuario usuario = usuarioRepository
                .findByDsEmail(request.login())
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Login ou senha inválidos"
                        )
                );

        validarSenhaEUsuarioAtivo(usuario, request.senha());

        List<String> perfis = buscarPerfis(usuario);

        return gerarRespostaLogin(usuario, perfis);
    }

    /* =====================================================
       LOGIN DO PRESTADOR - CNPJ + SENHA
    ===================================================== */

    @Transactional(readOnly = true)
    public LoginResponse loginPrestador(PrestadorLoginRequest request) {

        // Remove pontos, barras e hífen do CNPJ
        String cnpj = request.cnpj().replaceAll("\\D", "");

        // Valida o formato básico do documento
        if (cnpj.length() != 14) {
            throw new BadCredentialsException(
                    "CNPJ ou senha inválidos"
            );
        }

        // Localiza a conta institucional vinculada ao CNPJ
        Usuario usuario = usuarioRepository
                .findByPrestador_NrDocumento(cnpj)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "CNPJ ou senha inválidos"
                        )
                );

        // Valida a senha e se o usuário está ativo
        validarSenhaEUsuarioAtivo(usuario, request.senha());

        // Busca os perfis cadastrados no banco
        List<String> perfis = buscarPerfis(usuario);

        // Garante que seja uma conta autorizada de Prestador
        if (!perfis.contains("PRESTADOR")) {
            throw new BadCredentialsException(
                    "CNPJ ou senha inválidos"
            );
        }

        // Reutiliza o JWT já utilizado no sistema
        return gerarRespostaLogin(usuario, perfis);
    }

    /* =====================================================
       VALIDAÇÃO DE CREDENCIAIS
    ===================================================== */

    private void validarSenhaEUsuarioAtivo(
            Usuario usuario,
            String senha
    ) {

        if (!passwordEncoder.matches(
                senha,
                usuario.getDsSenhaHash()
        )) {
            throw new BadCredentialsException(
                    "Login ou senha inválidos"
            );
        }

        if (!"S".equals(usuario.getFlAtivo())) {
            throw new BadCredentialsException(
                    "Login ou senha inválidos"
            );
        }
    }

    /* =====================================================
       CONSULTA DOS PERFIS
    ===================================================== */

    private List<String> buscarPerfis(Usuario usuario) {

        return usuarioPerfilRepository
                .findByUsuario_IdUsuario(
                        usuario.getIdUsuario()
                )
                .stream()
                .map(perfil -> perfil.getCdPerfil())
                .toList();
    }

    /* =====================================================
       GERAÇÃO DO JWT
    ===================================================== */

    private LoginResponse gerarRespostaLogin(
            Usuario usuario,
            List<String> perfis
    ) {

        String token = jwtService.gerarToken(
                usuario.getIdUsuario(),
                usuario.getDsEmail(),
                perfis
        );

        return new LoginResponse(
                token,
                "Bearer",
                usuario.getIdUsuario(),
                usuario.getNmUsuario()
        );
    }
}
