package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.request.AssinaturaConfirmarRequest;
import br.com.bughunters.fusexflow.dto.response.AssinaturaConfirmadaResponse;
import br.com.bughunters.fusexflow.dto.response.AssinaturaPendenteResponse;
import br.com.bughunters.fusexflow.dto.response.AssinaturaSolicitadaResponse;
import br.com.bughunters.fusexflow.service.AssinaturaGuiaService;
import br.com.bughunters.fusexflow.service.CodigoAssinaturaInvalidoException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Assinatura do beneficiário na guia utilizada.
 *
 *  OCS:          POST /api/guias/{id}/assinatura/solicitar
 *                POST /api/guias/{id}/assinatura/confirmar   { "codigo": "123456" }
 *  Beneficiário: GET  /api/beneficiario/assinaturas-pendentes
 */
@RestController
public class AssinaturaGuiaController {

    private final AssinaturaGuiaService service;

    public AssinaturaGuiaController(AssinaturaGuiaService service) {
        this.service = service;
    }

    /** A OCS pede um código novo. O código não vem na resposta: só o beneficiário o vê. */
    @PostMapping("/api/guias/{id}/assinatura/solicitar")
    public AssinaturaSolicitadaResponse solicitar(@PathVariable("id") Long id) {
        return service.solicitarCodigo(id);
    }

    /** A OCS informa o código que o beneficiário disse. */
    @PostMapping("/api/guias/{id}/assinatura/confirmar")
    public AssinaturaConfirmadaResponse confirmar(@PathVariable("id") Long id,
                                                  @RequestBody AssinaturaConfirmarRequest request) {
        return service.confirmar(id, request == null ? null : request.codigo());
    }

    /** Guias do beneficiário logado aguardando assinatura, com o código vigente. */
    @GetMapping("/api/beneficiario/assinaturas-pendentes")
    public List<AssinaturaPendenteResponse> pendentes() {
        return service.listarPendentes();
    }

    // Erros voltam como { "message": "..." }, que o frontend mostra na tela

    @ExceptionHandler({IllegalArgumentException.class, CodigoAssinaturaInvalidoException.class})
    public ResponseEntity<Map<String, String>> handleRequisicaoInvalida(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflito(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler({NoSuchElementException.class, EntityNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNaoEncontrado(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(ResponseStatusException e) {
        String mensagem = e.getReason() != null ? e.getReason() : "Operação não permitida.";
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", mensagem));
    }
}
