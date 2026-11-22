package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.request.OcsCadastroRequest;
import br.com.bughunters.fusexflow.entity.Prestador;
import br.com.bughunters.fusexflow.entity.Usuario;
import br.com.bughunters.fusexflow.entity.UsuarioPerfil;
import br.com.bughunters.fusexflow.repositories.PrestadorRepository;
import br.com.bughunters.fusexflow.repositories.UsuarioPerfilRepository;
import br.com.bughunters.fusexflow.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastro de OCS: cria o PRESTADOR, o USUARIO responsável (vinculado ao
 * prestador, com login pelo CNPJ) e o perfil PRESTADOR, numa transação só.
 */
@Service
public class OcsService {

    private static final String PERFIL_PRESTADOR = "PRESTADOR";

    private final PrestadorRepository prestadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioPerfilRepository usuarioPerfilRepository;
    private final PasswordEncoder passwordEncoder;

    public OcsService(PrestadorRepository prestadorRepository,
                      UsuarioRepository usuarioRepository,
                      UsuarioPerfilRepository usuarioPerfilRepository,
                      PasswordEncoder passwordEncoder) {
        this.prestadorRepository = prestadorRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioPerfilRepository = usuarioPerfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @throws IllegalStateException quando CNPJ, razão social, e-mail ou CPF já estão cadastrados
     */
    @Transactional
    public Prestador cadastrar(OcsCadastroRequest request) {

        OcsCadastroRequest.Prestador dados = request.prestador();
        OcsCadastroRequest.Endereco endereco = dados.endereco();
        OcsCadastroRequest.Responsavel responsavel = request.responsavel();

        String email = responsavel.email().trim().toLowerCase();

        if (prestadorRepository.findByNrDocumento(dados.cnpj()).isPresent()) {
            throw new IllegalStateException("Já existe uma OCS cadastrada com este CNPJ.");
        }

        if (prestadorRepository.existeComNome(dados.nomePrestador())) {
            throw new IllegalStateException("Já existe uma OCS cadastrada com esta razão social.");
        }

        if (usuarioRepository.existsByDsEmailIgnoreCase(email)) {
            throw new IllegalStateException("E-mail já cadastrado.");
        }

        if (usuarioRepository.existsByNrCpf(responsavel.cpf())) {
            throw new IllegalStateException("CPF já cadastrado.");
        }

        Prestador prestador = prestadorRepository.save(new Prestador(
                dados.nomePrestador().trim(),
                textoOuNulo(dados.nomeFantasia()),
                "OCS",
                "CNPJ",
                dados.cnpj(),
                endereco.logradouro().trim(),
                endereco.numero().trim(),
                textoOuNulo(endereco.complemento()),
                endereco.bairro().trim(),
                endereco.cidade().trim(),
                endereco.uf().trim().toUpperCase(),
                endereco.cep()
        ));

        Usuario usuario = new Usuario(
                responsavel.nomeUsuario().trim(),
                email,
                passwordEncoder.encode(responsavel.senha()),
                null,
                responsavel.cpf(),
                textoOuNulo(responsavel.telefone())
        );
        usuario.setFlAtivo("S");
        usuario.setPrestador(prestador);
        usuario = usuarioRepository.save(usuario);

        usuarioPerfilRepository.save(new UsuarioPerfil(usuario, PERFIL_PRESTADOR));

        return prestador;
    }

    private static String textoOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
