package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.PrestadorResponse;
import br.com.bughunters.fusexflow.service.PrestadorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prestadores")
public class PrestadorController {

    private final PrestadorService prestadorService;

    public PrestadorController(PrestadorService prestadorService) {
        this.prestadorService = prestadorService;
    }

    @GetMapping
    public List<PrestadorResponse> findAll() {
        return prestadorService.findAll();
    }

    @GetMapping(params = "exameId")
    public List<PrestadorResponse> findByExameId(
            @RequestParam Long exameId
    ) {
        return prestadorService.findByExameId(exameId);
    }
}