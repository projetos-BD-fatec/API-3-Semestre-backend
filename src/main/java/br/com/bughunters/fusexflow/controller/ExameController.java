package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.ExameResponse;
import br.com.bughunters.fusexflow.service.ExameService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exames")
public class ExameController {

    private final ExameService exameService;

    public ExameController(ExameService exameService) {
        this.exameService = exameService;
    }

    @GetMapping
    public List<ExameResponse> findAll() {
        return exameService.findAll();
    }
}