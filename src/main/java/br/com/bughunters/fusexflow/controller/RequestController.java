package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.RequestResponse;
import br.com.bughunters.fusexflow.service.RequestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/solicitacoes")
public class RequestController {
    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService =requestService;
    }

    @GetMapping
    public List<RequestResponse> findAll() {
        return requestService.findAll();
    }
}
