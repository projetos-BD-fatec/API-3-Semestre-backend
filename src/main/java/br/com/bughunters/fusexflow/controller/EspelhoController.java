package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.request.EspelhoItemRequest;
import br.com.bughunters.fusexflow.dto.response.EspelhoResponse;
import br.com.bughunters.fusexflow.dto.response.TussResponse;
import br.com.bughunters.fusexflow.service.EspelhoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EspelhoController {

    private final EspelhoService espelhoService;

    public EspelhoController(EspelhoService espelhoService) {
        this.espelhoService = espelhoService;
    }

    @GetMapping("/catalogo-tuss")
    public List<TussResponse> buscarTuss(@RequestParam("busca") String busca) {
        return espelhoService.buscarTuss(busca);
    }

    /** Abre (ou retorna, se já existir) o espelho de uma guia. */
    @PostMapping("/guias/{idGuia}/espelho")
    public EspelhoResponse abrir(@PathVariable("idGuia") Long idGuia) {
        return espelhoService.abrir(idGuia);
    }

    @GetMapping("/espelhos/{id}")
    public EspelhoResponse buscar(@PathVariable("id") Long id) {
        return espelhoService.buscar(id);
    }

    @PostMapping("/espelhos/{id}/itens")
    public EspelhoResponse adicionarItem(@PathVariable("id") Long id,
                                         @RequestBody EspelhoItemRequest request) {
        return espelhoService.adicionarItem(id, request);
    }

    @PutMapping("/espelhos/{id}/itens/{idItem}")
    public EspelhoResponse atualizarItem(@PathVariable("id") Long id,
                                         @PathVariable("idItem") Long idItem,
                                         @RequestBody EspelhoItemRequest request) {
        return espelhoService.atualizarItem(id, idItem, request);
    }

    @DeleteMapping("/espelhos/{id}/itens/{idItem}")
    public EspelhoResponse excluirItem(@PathVariable("id") Long id,
                                       @PathVariable("idItem") Long idItem) {
        return espelhoService.excluirItem(id, idItem);
    }

    @PostMapping("/espelhos/{id}/enviar")
    public EspelhoResponse enviar(@PathVariable("id") Long id) {
        return espelhoService.enviar(id);
    }
}