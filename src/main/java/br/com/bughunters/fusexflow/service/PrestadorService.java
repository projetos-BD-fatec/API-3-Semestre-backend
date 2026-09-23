package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.PrestadorResponse;
import br.com.bughunters.fusexflow.entity.Prestador;
import br.com.bughunters.fusexflow.mock.ExamePrestadorMock;
import br.com.bughunters.fusexflow.mock.PrestadorMock;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrestadorService {

    public List<PrestadorResponse> findAll() {

        List<Prestador> prestadores = PrestadorMock.getPrestadores();

        return prestadores.stream()
                .map(prestador -> new PrestadorResponse(
                        prestador.getIdPrestador(),
                        prestador.getNmPrestador(),
                        prestador.getNmFantasia(),
                        prestador.getDsLogradouro(),
                        prestador.getNrEndereco(),
                        prestador.getDsComplemento(),
                        prestador.getNmBairro(),
                        prestador.getNmCidade(),
                        prestador.getSgUf()
                ))
                .toList();
    }

    public List<PrestadorResponse> findByExameId(Long exameId) {

        return ExamePrestadorMock.getExamesPrestadores()
                .stream()
                .filter(examePrestador ->
                        examePrestador.getExame().getIdExame().equals(exameId)
                )
                .map(examePrestador -> {

                    Prestador prestador = examePrestador.getPrestador();

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
                })
                .toList();
    }
}