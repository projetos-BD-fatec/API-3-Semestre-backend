package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.request.EnvioLoteRequest;
import br.com.bughunters.fusexflow.dto.request.EspelhoItemRequest;
import br.com.bughunters.fusexflow.dto.response.EnvioLoteResponse;
import br.com.bughunters.fusexflow.dto.response.EspelhoResponse;
import br.com.bughunters.fusexflow.dto.response.EspelhoSalvoResponse;
import br.com.bughunters.fusexflow.dto.response.TussResponse;
import br.com.bughunters.fusexflow.service.EspelhoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    /** Espelhos salvos da OCS logada, ainda não enviados ao FUSEx. */
    @GetMapping("/espelhos/salvos")
    public List<EspelhoSalvoResponse> listarSalvos() {
        return espelhoService.listarSalvos();
    }

    /** Envia ao FUSEx os espelhos salvos escolhidos. Corpo: { "idsEspelho": [1, 2, 3] } */
    @PostMapping("/espelhos/enviar-lote")
    public EnvioLoteResponse enviarLote(@RequestBody EnvioLoteRequest request) {
        return espelhoService.enviarLote(request);
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

    /** Conclui o espelho sem enviar: ele fica salvo, em posse da OCS. */
    @PostMapping("/espelhos/{id}/salvar")
    public EspelhoResponse salvar(@PathVariable("id") Long id) {
        return espelhoService.salvar(id);
    }

    /** Envio individual de um espelho já salvo (o envio em lote é /espelhos/enviar-lote). */
    @PostMapping("/espelhos/{id}/enviar")
    public EspelhoResponse enviar(@PathVariable("id") Long id) {
        return espelhoService.enviar(id);
    }

    // Erros de regra voltam com { "message": "..." }, que o frontend mostra à OCS

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleRequisicaoInvalida(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflito(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNaoEncontrado(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }
}