package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.PreGuiaResponse;
import br.com.bughunters.fusexflow.service.PreGuiaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pre-guias")
public class PreGuiaController {
    private final PreGuiaService preGuiaService;

    public PreGuiaController(PreGuiaService preGuiaService) {
        this.preGuiaService = preGuiaService;
    }

    @GetMapping
    public List<PreGuiaResponse> findAll() {
        return preGuiaService.findAll();
    }
}
