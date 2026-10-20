package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.response.LisuraEspelhoResponse;
import br.com.bughunters.fusexflow.service.LisuraService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lisura")
public class LisuraController {

    private final LisuraService lisuraService;

    public LisuraController(LisuraService lisuraService) {
        this.lisuraService = lisuraService;
    }

    /**
     * Cruzamento: itens do espelho enviado pela OCS com o valor contratual
     * (EXAME_PRESTADOR), a diferença (cobrado - contrato) e a situação.
     */
    @GetMapping("/{guiaId}")
    public LisuraEspelhoResponse conferir(@PathVariable("guiaId") Long guiaId) {
        return lisuraService.conferir(guiaId);
    }
}
