package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.request.PreGuiaItemRequest;
import br.com.bughunters.fusexflow.dto.request.PreGuiaRequest;
import br.com.bughunters.fusexflow.dto.response.PreGuiaItemResponse;
import br.com.bughunters.fusexflow.dto.response.PreGuiaResponse;
import br.com.bughunters.fusexflow.entity.*;
import br.com.bughunters.fusexflow.mock.*;
import br.com.bughunters.fusexflow.repositories.ExamePrestadorRepository;
import br.com.bughunters.fusexflow.repositories.ExameRepository;
import br.com.bughunters.fusexflow.repositories.PrestadorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class PreGuiaService {

    private final ArquivoService arquivoService;
    private final PrestadorRepository prestadorRepository;
    private final ExameRepository exameRepository;
    private final ExamePrestadorRepository examePrestadorRepository;

    public PreGuiaService(ArquivoService arquivoService,
                          PrestadorRepository prestadorRepository,
                          ExameRepository exameRepository,
                          ExamePrestadorRepository examePrestadorRepository) {
        this.arquivoService = arquivoService;
        this.prestadorRepository = prestadorRepository;
        this.exameRepository = exameRepository;
        this.examePrestadorRepository = examePrestadorRepository;
    }

    public List<PreGuiaResponse> findAll() {

        return PreGuiaMock.getPreGuias()
                .stream()
                .map(preGuia -> {

                    List<PreGuiaItemResponse> itens =
                            PreGuiaItemMock.findByPreGuiaId(preGuia.getIdPreGuia())
                                    .stream()
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
                            itens
                    );
                })
                .toList();
    }

    public PreGuiaResponse criar(PreGuiaRequest request, MultipartFile arquivo) {

        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("O arquivo é obrigatório.");
        }

        if (request == null || request.itens() == null || request.itens().isEmpty()) {
            throw new IllegalArgumentException("A pré-guia deve possuir pelo menos um item.");
        }

        Usuario usuario = UsuarioMock.getUsuarios().get(0);

        String nomeArquivo;
        try {
            nomeArquivo = arquivoService.salvar(arquivo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar o arquivo.", e);
        }

        PreGuia preGuia = new PreGuia(usuario, nomeArquivo);
        preGuia = PreGuiaMock.save(preGuia);

        for (PreGuiaItemRequest itemRequest : request.itens()) {

            Exame exame = exameRepository.findById(itemRequest.idExame())
                    .orElseThrow(() -> new EntityNotFoundException("Exame não encontrado: " + itemRequest.idExame()));

            Prestador prestador = prestadorRepository.findById(itemRequest.idPrestador())
                    .orElseThrow(() -> new EntityNotFoundException("Prestador não encontrado: " + itemRequest.idPrestador()));

            ExamePrestador examePrestador = examePrestadorRepository
                    .findAtivoByExameIdAndPrestadorId(itemRequest.idExame(), itemRequest.idPrestador(), LocalDate.now())
                    .orElseThrow(() -> new IllegalArgumentException("Exame não possui relação ativa com o prestador informado."));

            PreGuiaItem item = new PreGuiaItem(
                    preGuia,
                    prestador,
                    exame,
                    examePrestador.getValorContratual()
            );

            PreGuiaItemMock.save(item);
        }

        List<PreGuiaItem> itens = PreGuiaItemMock.findByPreGuiaId(preGuia.getIdPreGuia());

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