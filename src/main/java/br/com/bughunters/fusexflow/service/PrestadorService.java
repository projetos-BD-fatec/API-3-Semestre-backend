package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.response.PrestadorResponse;
import br.com.bughunters.fusexflow.entity.Prestador;
import br.com.bughunters.fusexflow.repositories.ExamePrestadorRepository;
import br.com.bughunters.fusexflow.repositories.PrestadorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrestadorService {

    private final PrestadorRepository prestadorRepository;
    private final ExamePrestadorRepository examePrestadorRepository;

    public PrestadorService(PrestadorRepository prestadorRepository,
                            ExamePrestadorRepository examePrestadorRepository) {
        this.prestadorRepository = prestadorRepository;
        this.examePrestadorRepository = examePrestadorRepository;
    }

    public List<PrestadorResponse> findAll() {
        return prestadorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PrestadorResponse> findByExameId(Long exameId) {
        return examePrestadorRepository.findAtivosByExameId(exameId, LocalDate.now())
                .stream()
                .map(ep -> toResponse(ep.getPrestador()))
                .toList();
    }

    private PrestadorResponse toResponse(Prestador prestador) {
        return new PrestadorResponse(
                prestador.getIdPrestador(),
                prestador.getNmPrestador(),
                prestador.getNmFantasia(),
                prestador.getDsLogradouro(),
                prestador.getNrEndereco(),
                prestador.getDsComplemento(),
                prestador.getNmBairro(),
                prestador.getNmCidade(),
                prestador.getSgUf()
        );
    }
}