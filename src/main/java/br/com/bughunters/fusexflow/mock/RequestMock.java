package br.com.bughunters.fusexflow.mock;

import br.com.bughunters.fusexflow.dto.RequestResponse;

import java.util.List;

public class RequestMock {
    public static List<RequestResponse> findAll() {
        return List.of(
                new RequestResponse(
                    1L,
                    "Ressonância Magnética",
                    "Clínica A",
                    "12/09/2026",
                    "EM_ANALISE"
                ),
                new RequestResponse(
                        2L,
                        "Ultrassonografia",
                        "Clínica B",
                        "08/09/2026",
                        "AUTORIZADA"
                ),

                new RequestResponse(
                        3L,
                        "Tomografia",
                        "Clínica C",
                        "01/09/2026",
                        "NEGADA"
                )
        );
    }
}
