package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.response.ExameResponse;
import br.com.bughunters.fusexflow.entity.Exame;
import br.com.bughunters.fusexflow.repositories.ExameRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExameService {
    private final ExameRepository exameRepository;

    public ExameService(ExameRepository exameRepository) {
        this.exameRepository = exameRepository;
    }

    public List<ExameResponse> findAll() {
        return exameRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ExameResponse toResponse(Exame exame) {
        return new ExameResponse(
                exame.getIdExame(),
                exame.getCatalogoTuss().getCodTuss(),
                exame.getDescExame()
        );
    }
}