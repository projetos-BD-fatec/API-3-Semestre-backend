package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.request.LisuraRevisaoRequest;
import br.com.bughunters.fusexflow.dto.response.LisuraEspelhoResponse;
import br.com.bughunters.fusexflow.dto.response.LisuraResumoResponse;
import br.com.bughunters.fusexflow.service.LisuraService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lisura")
public class LisuraController {

    private final LisuraService lisuraService;

    public LisuraController(LisuraService lisuraService) {
        this.lisuraService = lisuraService;
    }

    /** Lista os espelhos já enviados pelas OCS (entrada da Lisura para o FUSEX). */
    @GetMapping
    public List<LisuraResumoResponse> listar() {
        return lisuraService.listar();
    }

    /**
     * Cruzamento: itens do espelho enviado pela OCS com o valor contratual
     * (EXAME_PRESTADOR), a diferença (cobrado - contrato) e a situação.
     */
    @GetMapping("/{guiaId}")
    public LisuraEspelhoResponse conferir(@PathVariable("guiaId") Long guiaId) {
        return lisuraService.conferir(guiaId);
    }

    /**
     * Grava a decisão / valor corrigido pelo analista da Lisura.
     * Corpo: { "itens": [ { "idItem": 1, "decisao": "VALOR_AJUSTADO", "valorAjustado": 12.50 } ] }
     */
    @PutMapping("/{guiaId}/revisao")
    public LisuraEspelhoResponse revisar(@PathVariable("guiaId") Long guiaId,
                                         @RequestBody LisuraRevisaoRequest request) {
        return lisuraService.revisar(guiaId, request);
    }
}
