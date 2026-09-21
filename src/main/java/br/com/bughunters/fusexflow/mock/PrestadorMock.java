package br.com.bughunters.fusexflow.mock;

import br.com.bughunters.fusexflow.entity.Prestador;

import java.util.ArrayList;
import java.util.List;

public class PrestadorMock {

    private static final List<Prestador> prestadores = new ArrayList<>();

    static {
        Prestador prestador3 = new Prestador(
                "Laboratório São José",
                "Laboratório São José",
                "OCS",
                "CNPJ",
                "12345678000101",
                "Rua Exemplo",
                "100",
                null,
                "Centro",
                "São José dos Campos",
                "SP",
                "12200000"
        );

        Prestador prestador2 = new Prestador(
                "Clínica Vida",
                "Clínica Vida",
                "OCS",
                "CNPJ",
                "23456789000102",
                "Avenida Brasil",
                "500",
                null,
                "Jardim América",
                "São José dos Campos",
                "SP",
                "12230000"
        );

        Prestador prestador1 = new Prestador(
                "Centro Médico Vale",
                "Centro Médico Vale",
                "OCS",
                "CNPJ",
                "34567890000103",
                "Rua das Flores",
                "250",
                null,
                "Centro",
                "Taubaté",
                "SP",
                "12010000"
        );

        prestadores.add(prestador1);
        prestadores.add(prestador2);
        prestadores.add(prestador3);
    }

    public static List<Prestador> getPrestadores() {
        return prestadores;
    }
}