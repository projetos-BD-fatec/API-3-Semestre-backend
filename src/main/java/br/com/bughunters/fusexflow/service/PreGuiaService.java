package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.PreGuiaResponse;
import br.com.bughunters.fusexflow.entity.ExamePrestador;
import br.com.bughunters.fusexflow.mock.ExamePrestadorMock;
import br.com.bughunters.fusexflow.mock.PreGuiaMock;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PreGuiaService {

    public List<PreGuiaResponse> findAll() {

        return PreGuiaMock.getPreGuias()
                .stream()
                .map(preGuia -> {

                    ExamePrestador examePrestador =
                            ExamePrestadorMock.findByExameIdAndPrestadorId(
                                    preGuia.getExame().getIdExame(),
                                    preGuia.getPrestador().getIdPrestador()
                            );

                    return new PreGuiaResponse(
                            preGuia.getIdPreGuia(),
                            preGuia.getExame().getDescExame(),
                            preGuia.getPrestador().getNmFantasia(),
                            preGuia.getPrestador().getDsLogradouro(),
                            preGuia.getPrestador().getNrEndereco(),
                            preGuia.getPrestador().getNmBairro(),
                            preGuia.getPrestador().getNmCidade(),
                            preGuia.getPrestador().getSgUf(),
                            examePrestador.getValorContratual(),
                            preGuia.getStatus().name(),
                            preGuia.getCriadoEm()
                    );
                })
                .toList();
    }
}