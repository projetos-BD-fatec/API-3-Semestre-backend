package br.com.bughunters.fusexflow.mock;

import br.com.bughunters.fusexflow.entity.PreGuia;
import br.com.bughunters.fusexflow.entity.PreGuiaItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PreGuiaItemMock {
    private static final List<PreGuiaItem> preGuiaItens = new ArrayList<>();

    static {
        var exames = ExameMock.getExames();
        var prestadores = PrestadorMock.getPrestadores();
        var preGuias = PreGuiaMock.getPreGuias();

        PreGuiaItem item1 = new PreGuiaItem(
                preGuias.get(0),
                prestadores.get(0),
                exames.get(0),
                new BigDecimal("250.00")
        );

        PreGuiaItem item2 = new PreGuiaItem(
                preGuias.get(0),
                prestadores.get(1),
                exames.get(1),
                new BigDecimal("350.00")
        );

        preGuiaItens.add(item1);
        preGuiaItens.add(item2);
    }

    public static List<PreGuiaItem> getPreGuiaItens() {
        return preGuiaItens;
    }

    public static List<PreGuiaItem> findByPreGuiaId(Long preGuiaId) {
        return preGuiaItens.stream()
                .filter(item ->
                        item.getPreGuia().getIdPreGuia().equals(preGuiaId)
                )
                .toList();
    }
}
