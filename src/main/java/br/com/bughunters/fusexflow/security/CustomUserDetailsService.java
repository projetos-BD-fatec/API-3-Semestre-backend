package br.com.bughunters.fusexflow.security;

import br.com.bughunters.fusexflow.entity.Usuario;
import br.com.bughunters.fusexflow.repositories.UsuarioPerfilRepository;
import br.com.bughunters.fusexflow.repositories.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioPerfilRepository usuarioPerfilRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository,
                                    UsuarioPerfilRepository usuarioPerfilRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioPerfilRepository = usuarioPerfilRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {

        // PONTO ÚNICO 1/2 — hoje busca só por e-mail.
        // Se/quando o login também aceitar PREC/CP, troque a linha abaixo
        // por findByNrPrecCp (ou combine as duas). CNPJ de prestador exigiria
        // buscar Prestador por NR_DOCUMENTO e então o Usuario vinculado a ele.
        Usuario usuario = usuarioRepository.findByDsEmail(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + login));

        List<GrantedAuthority> authorities = usuarioPerfilRepository
                .findByUsuario_IdUsuario(usuario.getIdUsuario())
                .stream()
                .map(perfil -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + perfil.getCdPerfil()))
                .toList();

        return new User(
                usuario.getDsEmail(),
                usuario.getDsSenhaHash(),
                "S".equals(usuario.getFlAtivo()),
                true, true, true,
                authorities
        );
    }
}