package br.com.bughunters.fusexflow.mock;

import br.com.bughunters.fusexflow.entity.PreGuia;
import br.com.bughunters.fusexflow.enums.StatusPreGuia;

import java.util.ArrayList;
import java.util.List;

public class PreGuiaMock {

    private static final List<PreGuia> preGuias = new ArrayList<>();

    static {
        var usuarios = UsuarioMock.getUsuarios();
        var exames = ExameMock.getExames();
        var prestadores = PrestadorMock.getPrestadores();

        PreGuia primeira = new PreGuia(
                usuarios.get(0),
                "FotoWhatsApp"
        );

        PreGuia segunda = new PreGuia(
                usuarios.get(0),
                "encaminhamento.pdf"
        );

        primeira.setStatus(StatusPreGuia.AUTORIZADA);

        primeira.setIdPreGuia(1L);
        primeira.setIdPreGuia(2L);

        preGuias.add(primeira);
        preGuias.add(segunda);
    }

    public static List<PreGuia> getPreGuias() {
        return preGuias;
    }
}