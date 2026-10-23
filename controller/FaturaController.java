package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.response.FaturaResumoResponse;
import br.com.bughunters.fusexflow.enums.StatusFatura;
import br.com.bughunters.fusexflow.service.FaturaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FaturaController {

    private final FaturaService faturaService;

    public FaturaController(FaturaService faturaService) {
        this.faturaService = faturaService;
    }

    /**
     * Lista as guias que já possuem espelho (visão interna do FUSEX).
     * GET /faturas                    -> todas
     * GET /faturas?status=UTILIZADAS  -> espelho enviado
     * GET /faturas?status=PENDENTES   -> espelho em rascunho
     */
    @GetMapping("/faturas")
    public List<FaturaResumoResponse> listar(
            @RequestParam(value = "status", required = false) StatusFatura status) {
        return faturaService.listar(status);
    }
}
