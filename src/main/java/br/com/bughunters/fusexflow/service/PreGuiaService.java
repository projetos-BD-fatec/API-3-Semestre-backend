package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.request.PreGuiaItemRequest;
import br.com.bughunters.fusexflow.dto.request.PreGuiaRequest;
import br.com.bughunters.fusexflow.dto.response.PreGuiaItemResponse;
import br.com.bughunters.fusexflow.dto.response.PreGuiaResponse;
import br.com.bughunters.fusexflow.entity.*;
import br.com.bughunters.fusexflow.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class PreGuiaService {

    private final ArquivoService arquivoService;
    private final UsuarioRepository usuarioRepository;
    private final PrestadorRepository prestadorRepository;
    private final ExameRepository exameRepository;
    private final ExamePrestadorRepository examePrestadorRepository;
    private final PreGuiaRepository preGuiaRepository;
    private final PreGuiaItemRepository preGuiaItemRepository;

    public PreGuiaService(ArquivoService arquivoService,
                          UsuarioRepository usuarioRepository,
                          PrestadorRepository prestadorRepository,
                          ExameRepository exameRepository,
                          ExamePrestadorRepository examePrestadorRepository,
                          PreGuiaRepository preGuiaRepository,
                          PreGuiaItemRepository preGuiaItemRepository) {
        this.arquivoService = arquivoService;
        this.usuarioRepository = usuarioRepository;
        this.prestadorRepository = prestadorRepository;
        this.exameRepository = exameRepository;
        this.examePrestadorRepository = examePrestadorRepository;
        this.preGuiaRepository = preGuiaRepository;
        this.preGuiaItemRepository = preGuiaItemRepository;
    }

    /**
     * Uso futuro: tela do funcionário FUSEX, que precisa ver todas as solicitações.
     * Não usar essa versão na tela do usuário comum.
     */
    public List<PreGuiaResponse> findAll() {
        return preGuiaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Uso na tela do usuário: retorna apenas as pré-guias abertas por ele.
     */
    public List<PreGuiaResponse> findByUsuarioId(Long idUsuario) {
        return preGuiaRepository.findByUsuario_IdUsuario(idUsuario)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PreGuiaResponse criar(PreGuiaRequest request, MultipartFile arquivo) {

        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("O arquivo é obrigatório.");
        }

        if (request == null || request.itens() == null || request.itens().isEmpty()) {
            throw new IllegalArgumentException("A pré-guia deve possuir pelo menos um item.");
        }

        Usuario usuario = usuarioRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("Usuário padrão de teste não encontrado (ID 1)."));

        PreGuia preGuia = new PreGuia(usuario);
        preGuia = preGuiaRepository.save(preGuia);

        for (PreGuiaItemRequest itemRequest : request.itens()) {

            Exame exame = exameRepository.findById(itemRequest.idExame())
                    .orElseThrow(() -> new EntityNotFoundException("Exame não encontrado: " + itemRequest.idExame()));

            Prestador prestador = prestadorRepository.findById(itemRequest.idPrestador())
                    .orElseThrow(() -> new EntityNotFoundException("Prestador não encontrado: " + itemRequest.idPrestador()));

            ExamePrestador examePrestador = examePrestadorRepository
                    .findAtivoByExameIdAndPrestadorId(itemRequest.idExame(), itemRequest.idPrestador(), java.time.LocalDate.now())
                    .orElseThrow(() -> new IllegalArgumentException("Exame não possui relação ativa com o prestador informado."));

            PreGuiaItem item = new PreGuiaItem(
                    preGuia,
                    prestador,
                    exame,
                    examePrestador.getValorContratual()
            );

            preGuiaItemRepository.save(item);
        }

        String nomeArquivo;
        try {
            nomeArquivo = arquivoService.salvar(arquivo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar o arquivo.", e);
        }
        preGuia.setNmAnexo(nomeArquivo);
        List<PreGuiaItem> itens = preGuiaItemRepository.findByPreGuia_IdPreGuia(preGuia.getIdPreGuia());

        return toResponse(preGuia, itens);
    }

    private PreGuiaResponse toResponse(PreGuia preGuia) {
        List<PreGuiaItem> itens = preGuiaItemRepository.findByPreGuia_IdPreGuia(preGuia.getIdPreGuia());
        return toResponse(preGuia, itens);
    }

    private PreGuiaResponse toResponse(PreGuia preGuia, List<PreGuiaItem> itens) {
        List<PreGuiaItemResponse> itensResponse = itens.stream()
                .map(item -> new PreGuiaItemResponse(
                        item.getIdPreGuiaItem(),
                        item.getExame().getDescExame(),
                        item.getPrestador().getNmFantasia(),
                        item.getPrestador().getDsLogradouro(),
                        item.getPrestador().getNrEndereco(),
                        item.getPrestador().getDsComplemento(),
                        item.getPrestador().getNmBairro(),
                        item.getPrestador().getNmCidade(),
                        item.getPrestador().getSgUf(),
                        item.getValor(),
                        item.getStatus().name()
                ))
                .toList();

        return new PreGuiaResponse(
                preGuia.getIdPreGuia(),
                preGuia.getNmAnexo(),
                preGuia.getStatus().name(),
                preGuia.getCriadoEm(),
                itensResponse
        );
    }
}