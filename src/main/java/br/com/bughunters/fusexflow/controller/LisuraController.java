package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.request.LisuraDecisaoRequest;
import br.com.bughunters.fusexflow.dto.request.LisuraRevisaoRequest;
import br.com.bughunters.fusexflow.dto.response.LisuraEspelhoResponse;
import br.com.bughunters.fusexflow.dto.response.LisuraResumoResponse;
import br.com.bughunters.fusexflow.service.LisuraService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lisura")
public class LisuraController {

    private final LisuraService lisuraService;

    public LisuraController(LisuraService lisuraService) {
        this.lisuraService = lisuraService;
    }

    /** Fila da Lisura: espelhos enviados pelas OCS que ainda aguardam a decisão. */
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

    /**
     * Decisão final do analista: muda o status da guia para
     * LISURA_APROVADA (aprovada para pagamento) ou LISURA_RECUSADA.
     * Corpo: { "decisao": "APROVAR" } ou { "decisao": "RECUSAR" }
     */
    @PatchMapping("/{guiaId}/decisao")
    public LisuraEspelhoResponse decidir(@PathVariable("guiaId") Long guiaId,
                                         @RequestBody LisuraDecisaoRequest request) {
        return lisuraService.decidir(guiaId, request);
    }

    // Erros de regra voltam com { "message": "..." }, que o frontend mostra ao analista

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
