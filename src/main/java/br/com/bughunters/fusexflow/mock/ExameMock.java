package br.com.bughunters.fusexflow.mock;

import br.com.bughunters.fusexflow.entity.CatalogoTuss;
import br.com.bughunters.fusexflow.entity.Exame;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExameMock {

    private static final List<Exame> exames = new ArrayList<>();

    static {
        CatalogoTuss tuss1 = new CatalogoTuss(
                "40301010",
                "Hemograma completo",
                "Laboratório",
                "TUSS",
                LocalDate.of(2024, 1, 1),
                null
        );

        CatalogoTuss tuss2 = new CatalogoTuss(
                "40901115",
                "Ultrassonografia de abdômen total",
                "Imagem",
                "TUSS",
                LocalDate.of(2024, 1, 1),
                null
        );

        CatalogoTuss tuss3 = new CatalogoTuss(
                "40808033",
                "Ressonância magnética de crânio",
                "Imagem",
                "TUSS",
                LocalDate.of(2024, 1, 1),
                null
        );

        Exame exame1 = new Exame(tuss1, "Hemograma completo");
        exame1.setIdExame(1L);

        Exame exame2 = new Exame(tuss2, "Ultrassonografia de abdômen total");
        exame2.setIdExame(2L);

        Exame exame3 = new Exame(tuss3, "Ressonância magnética de crânio");
        exame3.setIdExame(3L);

        exames.add(exame1);
        exames.add(exame2);
        exames.add(exame3);
    }

    public static List<Exame> getExames() {
        return exames;
    }
}