package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.ExameResponse;
import br.com.bughunters.fusexflow.entity.Exame;
import br.com.bughunters.fusexflow.mock.ExameMock;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExameService {

    public List<ExameResponse> findAll() {

        List<Exame> exames = ExameMock.getExames();

        return exames.stream()
                .map(exame -> new ExameResponse(
                        exame.getIdExame(),
                        exame.getCatalogoTuss().getCodTuss(),
                        exame.getDescExame()
                ))
                .toList();
    }
}