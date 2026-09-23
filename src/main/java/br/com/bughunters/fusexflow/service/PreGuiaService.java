package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.PreGuiaItemResponse;
import br.com.bughunters.fusexflow.dto.PreGuiaResponse;
import br.com.bughunters.fusexflow.entity.ExamePrestador;
import br.com.bughunters.fusexflow.mock.ExamePrestadorMock;
import br.com.bughunters.fusexflow.mock.PreGuiaItemMock;
import br.com.bughunters.fusexflow.mock.PreGuiaMock;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PreGuiaService {

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
}