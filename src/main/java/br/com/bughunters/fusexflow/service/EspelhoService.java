package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.request.EnvioLoteRequest;
import br.com.bughunters.fusexflow.dto.request.EspelhoItemRequest;
import br.com.bughunters.fusexflow.dto.response.EnvioLoteResponse;
import br.com.bughunters.fusexflow.dto.response.EspelhoItemResponse;
import br.com.bughunters.fusexflow.dto.response.EspelhoResponse;
import br.com.bughunters.fusexflow.dto.response.EspelhoSalvoResponse;
import br.com.bughunters.fusexflow.dto.response.TussResponse;
import br.com.bughunters.fusexflow.entity.*;
import br.com.bughunters.fusexflow.enums.EtapaFluxo;
import br.com.bughunters.fusexflow.enums.StatusEspelho;
import br.com.bughunters.fusexflow.enums.StatusGuia;
import br.com.bughunters.fusexflow.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EspelhoService {

    private final EspelhoRepository espelhoRepository;
    private final EspelhoItemRepository espelhoItemRepository;
    private final GuiaOficialRepository guiaOficialRepository;
    private final PreGuiaRepository preGuiaRepository;
    private final PreGuiaItemRepository preGuiaItemRepository;
    private final CatalogoTussRepository catalogoTussRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoStatusService historicoStatusService;

    public EspelhoService(EspelhoRepository espelhoRepository,
                          EspelhoItemRepository espelhoItemRepository,
                          GuiaOficialRepository guiaOficialRepository,
                          PreGuiaRepository preGuiaRepository,
                          PreGuiaItemRepository preGuiaItemRepository,
                          CatalogoTussRepository catalogoTussRepository,
                          UsuarioRepository usuarioRepository,
                          HistoricoStatusService historicoStatusService) {
        this.espelhoRepository = espelhoRepository;
        this.espelhoItemRepository = espelhoItemRepository;
        this.guiaOficialRepository = guiaOficialRepository;
        this.preGuiaRepository = preGuiaRepository;
        this.preGuiaItemRepository = preGuiaItemRepository;
        this.catalogoTussRepository = catalogoTussRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoStatusService = historicoStatusService;
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

        // O beneficiário precisa ter assinado (confirmado) a utilização da guia
        if (!guia.isAssinada()) {
            throw new IllegalStateException(
                    "A guia ainda não foi assinada pelo beneficiário. " +
                    "Colete a assinatura na tela da guia antes de abrir o espelho."
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

        // Um espelho salvo sempre tem itens: se esvaziou, volta a ser rascunho
        if (espelho.isSalvo() && espelhoItemRepository.countByEspelho_IdEspelho(idEspelho) == 0) {
            espelho.voltarParaRascunho();
        }

        espelho.marcarAtualizado();
        return toResponse(espelho);
    }

    /**
     * Conclui o espelho SEM enviar ao FUSEx: ele passa a SALVO e continua
     * com a OCS, que decide depois quando enviá-lo (em lote ou individualmente).
     * Salvar de novo um espelho já salvo não muda nada.
     */
    @Transactional
    public EspelhoResponse salvar(Long idEspelho) {
        Espelho espelho = espelhoEditavel(idEspelho);

        if (!espelho.getGuiaOficial().isAssinada()) {
            throw new IllegalStateException(
                    "A guia ainda não foi assinada pelo beneficiário. Colete a assinatura antes de salvar o espelho.");
        }

        if (espelhoItemRepository.countByEspelho_IdEspelho(idEspelho) == 0) {
            throw new IllegalArgumentException("O espelho deve possuir pelo menos um item para ser salvo.");
        }

        if (!espelho.isSalvo()) {
            espelho.salvar();
        }

        return toResponse(espelho);
    }

    /** Espelhos salvos da OCS logada que ainda não foram enviados ao FUSEx. */
    @Transactional(readOnly = true)
    public List<EspelhoSalvoResponse> listarSalvos() {
        Prestador prestador = prestadorLogado();

        List<Espelho> espelhos = espelhoRepository
                .findByPrestador_IdPrestadorAndStatusOrderBySalvoEmDesc(
                        prestador.getIdPrestador(), StatusEspelho.SALVO);

        if (espelhos.isEmpty()) {
            return List.of();
        }

        // Itens e beneficiários buscados de uma vez (sem consulta por linha)
        Map<Long, List<EspelhoItem>> itensPorEspelho = espelhoItemRepository
                .findByEspelho_IdEspelhoIn(espelhos.stream().map(Espelho::getIdEspelho).toList())
                .stream()
                .collect(Collectors.groupingBy(i -> i.getEspelho().getIdEspelho()));

        Map<Long, PreGuia> preGuias = preGuiaRepository
                .findAllById(espelhos.stream().map(e -> e.getGuiaOficial().getSolicitacaoId()).distinct().toList())
                .stream()
                .collect(Collectors.toMap(PreGuia::getIdPreGuia, Function.identity()));

        return espelhos.stream().map(e -> {
            List<EspelhoItem> itens = itensPorEspelho.getOrDefault(e.getIdEspelho(), List.of());
            BigDecimal total = itens.stream()
                    .map(EspelhoItem::getValorTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            GuiaOficial guia = e.getGuiaOficial();
            PreGuia preGuia = preGuias.get(guia.getSolicitacaoId());
            Usuario beneficiario = preGuia != null ? preGuia.getUsuario() : null;

            return new EspelhoSalvoResponse(
                    e.getIdEspelho(),
                    guia.getId(),
                    guia.getCodigoGuia(),
                    beneficiario != null ? beneficiario.getNmUsuario() : null,
                    beneficiario != null ? beneficiario.getNrPrecCp() : null,
                    e.getSalvoEm(),
                    itens.size(),
                    total
            );
        }).toList();
    }

    /**
     * Envia ao FUSEx os espelhos salvos escolhidos pela OCS (um ou vários, de uma vez).
     * Tudo ou nada: se algum espelho não puder ser enviado, nenhum é enviado.
     */
    @Transactional
    public EnvioLoteResponse enviarLote(EnvioLoteRequest request) {
        if (request == null || request.idsEspelho() == null || request.idsEspelho().isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos um espelho para enviar.");
        }
        if (request.idsEspelho().stream().anyMatch(id -> id == null)) {
            throw new IllegalArgumentException("Identificador de espelho inválido.");
        }

        Prestador prestador = prestadorLogado();
        Set<Long> ids = new LinkedHashSet<>(request.idsEspelho());

        Map<Long, Espelho> encontrados = espelhoRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Espelho::getIdEspelho, Function.identity()));

        List<Espelho> lote = new ArrayList<>();
        for (Long id : ids) {
            Espelho espelho = encontrados.get(id);

            // Espelho de outra OCS responde igual a "não existe", para não revelá-lo
            if (espelho == null
                    || !espelho.getPrestador().getIdPrestador().equals(prestador.getIdPrestador())) {
                throw new EntityNotFoundException("Espelho não encontrado: " + id);
            }
            lote.add(espelho);
        }

        for (Espelho espelho : lote) {
            String guia = espelho.getGuiaOficial().getCodigoGuia() != null
                    ? espelho.getGuiaOficial().getCodigoGuia()
                    : String.valueOf(espelho.getGuiaOficial().getId());

            if (espelho.isEnviado()) {
                throw new IllegalStateException("O espelho da guia " + guia + " já foi enviado ao FUSEx.");
            }
            if (!espelho.isSalvo()) {
                throw new IllegalStateException(
                        "O espelho da guia " + guia + " ainda não foi salvo. Salve-o antes de enviar.");
            }
            if (espelhoItemRepository.countByEspelho_IdEspelho(espelho.getIdEspelho()) == 0) {
                throw new IllegalStateException("O espelho da guia " + guia + " não possui itens.");
            }
        }

        LocalDateTime agora = LocalDateTime.now();
        lote.forEach(e -> e.enviar(agora));

        // US17: o envio ao FUSEx conclui a etapa de faturamento de cada guia do lote.
        for (Espelho espelho : lote) {
            GuiaOficial guia = espelho.getGuiaOficial();
            historicoStatusService.registrar(
                    EtapaFluxo.FATURAMENTO,
                    guia.getSolicitacaoId(),
                    guia.getId(),
                    StatusEspelho.SALVO.name(),
                    StatusEspelho.ENVIADO.name(),
                    "Espelho enviado por " + espelho.getPrestador().getNomeExibicao() + "."
            );
        }

        BigDecimal total = espelhoItemRepository
                .findByEspelho_IdEspelhoIn(lote.stream().map(Espelho::getIdEspelho).toList())
                .stream()
                .map(EspelhoItem::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new EnvioLoteResponse(
                lote.size(),
                total,
                agora,
                lote.stream().map(Espelho::getIdEspelho).toList()
        );
    }

    /** Envio individual de um espelho já salvo (o mesmo caminho do lote, com um só espelho). */
    @Transactional
    public EspelhoResponse enviar(Long idEspelho) {
        enviarLote(new EnvioLoteRequest(List.of(idEspelho)));
        return toResponse(espelhoDoPrestador(idEspelho));
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
            throw new IllegalStateException("O espelho já foi enviado ao FUSEx e não pode mais ser alterado.");
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
                espelho.getSalvoEm(),
                espelho.getEnviadoEm(),
                itens,
                total
        );
    }
}