package br.com.bughunters.fusexflow.mock;

import br.com.bughunters.fusexflow.entity.ExamePrestador;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ExamePrestadorMock {

    private static final List<ExamePrestador> examesPrestadores = new ArrayList<>();

    static {
        var exames = ExameMock.getExames();
        var prestadores = PrestadorMock.getPrestadores();

        examesPrestadores.add(new ExamePrestador(
                exames.get(0),
                prestadores.get(0),
                new BigDecimal("150.00")
        ));

        examesPrestadores.add(new ExamePrestador(
                exames.get(0),
                prestadores.get(1),
                new BigDecimal("170.00")
        ));

        examesPrestadores.add(new ExamePrestador(
                exames.get(1),
                prestadores.get(1),
                new BigDecimal("250.00")
        ));

        examesPrestadores.add(new ExamePrestador(
                exames.get(1),
                prestadores.get(2),
                new BigDecimal("280.00")
        ));

        examesPrestadores.add(new ExamePrestador(
                exames.get(2),
                prestadores.get(2),
                new BigDecimal("900.00")
        ));
    }

    public static List<ExamePrestador> getExamesPrestadores() {
        return examesPrestadores;
    }

    public static ExamePrestador findByExameIdAndPrestadorId(
            Long exameId,
            Long prestadorId
    ) {
        return examesPrestadores.stream()
                .filter(examePrestador ->
                        examePrestador.getExame().getIdExame().equals(exameId)
                                && examePrestador.getPrestador().getIdPrestador().equals(prestadorId)
                )
                .findFirst()
                .orElse(null);
    }
}