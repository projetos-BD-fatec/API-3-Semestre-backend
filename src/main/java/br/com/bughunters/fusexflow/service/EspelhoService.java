package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.request.EspelhoItemRequest;
import br.com.bughunters.fusexflow.dto.response.EspelhoItemResponse;
import br.com.bughunters.fusexflow.dto.response.EspelhoResponse;
import br.com.bughunters.fusexflow.dto.response.TussResponse;
import br.com.bughunters.fusexflow.entity.*;
import br.com.bughunters.fusexflow.enums.StatusGuia;
import br.com.bughunters.fusexflow.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EspelhoService {

    private final EspelhoRepository espelhoRepository;
    private final EspelhoItemRepository espelhoItemRepository;
    private final GuiaOficialRepository guiaOficialRepository;
    private final PreGuiaRepository preGuiaRepository;
    private final PreGuiaItemRepository preGuiaItemRepository;
    private final CatalogoTussRepository catalogoTussRepository;
    private final UsuarioRepository usuarioRepository;

    public EspelhoService(EspelhoRepository espelhoRepository,
                          EspelhoItemRepository espelhoItemRepository,
                          GuiaOficialRepository guiaOficialRepository,
                          PreGuiaRepository preGuiaRepository,
                          PreGuiaItemRepository preGuiaItemRepository,
                          CatalogoTussRepository catalogoTussRepository,
                          UsuarioRepository usuarioRepository) {
        this.espelhoRepository = espelhoRepository;
        this.espelhoItemRepository = espelhoItemRepository;
        this.guiaOficialRepository = guiaOficialRepository;
        this.preGuiaRepository = preGuiaRepository;
        this.preGuiaItemRepository = preGuiaItemRepository;
        this.catalogoTussRepository = catalogoTussRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Busca no catálogo TUSS por código ou descrição (máx. 20 resultados). */
    @Transactional(readOnly = true)
    public List<TussResponse> buscarTuss(String busca) {
        if (busca == null || busca.trim().length() < 2) {
            return List.of();
        }
        return catalogoTussRepository.buscar(busca.trim(), PageRequest.of(0, 20))
                .stream()
                .map(c -> new TussResponse(c.getCodTuss(), c.getDescTuss(), c.getTabelaTuss()))
                .toList();
    }


    /**
     * Abre o espelho da guia.
     * Se já existir, retorna o mesmo espelho (1:1).
     */
    @Transactional
    public EspelhoResponse abrir(Long idGuiaOficial) {
        Prestador prestador = prestadorLogado();

        GuiaOficial guia = guiaOficialRepository.findById(idGuiaOficial)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Guia não encontrada: " + idGuiaOficial
                ));

        // Verifica se a guia pertence ao prestador autenticado
        validarGuiaDoPrestador(guia, prestador);

        // Busca um espelho existente
        Espelho espelho = espelhoRepository
                .findByGuiaOficial_Id(idGuiaOficial)
                .orElse(null);

        if (espelho != null) {

            // Confirma que o espelho pertence à mesma OCS
            if (!espelho.getPrestador().getIdPrestador()
                    .equals(prestador.getIdPrestador())) {

                throw new EntityNotFoundException(
                        "Espelho não encontrado para este prestador."
                );
            }

            return toResponse(espelho);
        }

        // Um novo espelho só pode ser criado para guia UTILIZADA
        if (guia.getStatus() != StatusGuia.UTILIZADA) {
            throw new IllegalStateException(
                    "O espelho só pode ser aberto para guias utilizadas."
            );
        }

        // Cria somente quando ainda não existe
        espelho = espelhoRepository.save(
                new Espelho(guia, prestador)
        );

        return toResponse(espelho);
    }


    @Transactional(readOnly = true)
    public EspelhoResponse buscar(Long idEspelho) {
        return toResponse(espelhoDoPrestador(idEspelho));
    }

    @Transactional
    public EspelhoResponse adicionarItem(Long idEspelho, EspelhoItemRequest request) {
        Espelho espelho = espelhoEditavel(idEspelho);
        CatalogoTuss tuss = validarEBuscarTuss(request);

        espelhoItemRepository.save(new EspelhoItem(
                espelho, tuss, request.dataRealizacao(), request.quantidade(), request.valorUnitario()));

        espelho.marcarAtualizado();
        return toResponse(espelho);
    }

    @Transactional
    public EspelhoResponse atualizarItem(Long idEspelho, Long idItem, EspelhoItemRequest request) {
        Espelho espelho = espelhoEditavel(idEspelho);
        CatalogoTuss tuss = validarEBuscarTuss(request);

        EspelhoItem item = espelhoItemRepository
                .findByIdEspelhoItemAndEspelho_IdEspelho(idItem, idEspelho)
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado: " + idItem));

        item.atualizar(tuss, request.dataRealizacao(), request.quantidade(), request.valorUnitario());

        espelho.marcarAtualizado();
        return toResponse(espelho);
    }

    @Transactional
    public EspelhoResponse excluirItem(Long idEspelho, Long idItem) {
        Espelho espelho = espelhoEditavel(idEspelho);

        EspelhoItem item = espelhoItemRepository
                .findByIdEspelhoItemAndEspelho_IdEspelho(idItem, idEspelho)
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado: " + idItem));

        espelhoItemRepository.delete(item);
        espelhoItemRepository.flush();

        espelho.marcarAtualizado();
        return toResponse(espelho);
    }

    @Transactional
    public EspelhoResponse enviar(Long idEspelho) {
        Espelho espelho = espelhoEditavel(idEspelho);

        if (espelhoItemRepository.countByEspelho_IdEspelho(idEspelho) == 0) {
            throw new IllegalArgumentException("O espelho deve possuir pelo menos um item para ser enviado.");
        }

        espelho.enviar();
        return toResponse(espelho);
    }

    // ---------- regras de acesso ----------

    private Espelho espelhoDoPrestador(Long idEspelho) {
        Prestador prestador = prestadorLogado();

        Espelho espelho = espelhoRepository.findById(idEspelho)
                .orElseThrow(() -> new EntityNotFoundException("Espelho não encontrado: " + idEspelho));

        if (!espelho.getPrestador().getIdPrestador().equals(prestador.getIdPrestador())) {
            // Mesmo erro de "não existe", para não revelar espelhos de outras OCS
            throw new EntityNotFoundException("Espelho não encontrado: " + idEspelho);
        }
        return espelho;
    }

    private Espelho espelhoEditavel(Long idEspelho) {
        Espelho espelho = espelhoDoPrestador(idEspelho);
        if (!espelho.isEditavel()) {
            throw new IllegalStateException("O espelho já foi enviado e não pode mais ser alterado.");
        }
        return espelho;
    }

    /** A guia é da OCS se algum item da pré-guia de origem é dela. */
    private void validarGuiaDoPrestador(GuiaOficial guia, Prestador prestador) {
        boolean pertence = preGuiaItemRepository
                .findByPreGuia_IdPreGuia(guia.getSolicitacaoId())
                .stream()
                .anyMatch(i -> i.getPrestador().getIdPrestador().equals(prestador.getIdPrestador()));

        if (!pertence) {
            throw new EntityNotFoundException("Guia não encontrada: " + guia.getId());
        }
    }

    private Prestador prestadorLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuario = usuarioRepository.findByDsEmail(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado: " + login));

        if (usuario.getPrestador() == null) {
            throw new AccessDeniedException("Usuário não está vinculado a um prestador.");
        }
        return usuario.getPrestador();
    }

    // ---------- validação e conversão ----------

    private CatalogoTuss validarEBuscarTuss(EspelhoItemRequest r) {
        if (r == null || r.codTuss() == null || r.codTuss().isBlank()) {
            throw new IllegalArgumentException("O código TUSS é obrigatório.");
        }
        if (r.dataRealizacao() == null) {
            throw new IllegalArgumentException("A data de realização é obrigatória.");
        }
        if (r.quantidade() == null || r.quantidade() <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (r.valorUnitario() == null || r.valorUnitario().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O valor unitário é obrigatório e não pode ser negativo.");
        }
        return catalogoTussRepository.findById(r.codTuss().trim())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Código TUSS não encontrado: " + r.codTuss()));
    }

    private EspelhoResponse toResponse(Espelho espelho) {
        List<EspelhoItemResponse> itens = espelhoItemRepository
                .findByEspelho_IdEspelhoOrderByIdEspelhoItem(espelho.getIdEspelho())
                .stream()
                .map(i -> new EspelhoItemResponse(
                        i.getIdEspelhoItem(),
                        i.getCatalogoTuss().getCodTuss(),
                        i.getCatalogoTuss().getDescTuss(),
                        i.getDtRealizacao(),
                        i.getQuantidade(),
                        i.getValorUnitario(),
                        i.getValorTotal()))
                .toList();

        BigDecimal total = itens.stream()
                .map(EspelhoItemResponse::valorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        GuiaOficial guia = espelho.getGuiaOficial();
        String nomeBeneficiario = preGuiaRepository.findById(guia.getSolicitacaoId())
                .map(p -> p.getUsuario().getNmUsuario())
                .orElse(null);

        return new EspelhoResponse(
                espelho.getIdEspelho(),
                guia.getId(),
                nomeBeneficiario,
                espelho.getStatus().name(),
                espelho.getEnviadoEm(),
                itens,
                total
        );
    }
}