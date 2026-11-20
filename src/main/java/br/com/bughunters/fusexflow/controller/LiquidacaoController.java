package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.response.LiquidacaoResumoResponse;
import br.com.bughunters.fusexflow.service.LiquidacaoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Liquidação da guia (US16 / SCRUM-63).
 * Visão interna do FUSEx.
 */
@RestController
@RequestMapping("/liquidacao")
public class LiquidacaoController {

    private final LiquidacaoService liquidacaoService;

    public LiquidacaoController(LiquidacaoService liquidacaoService) {
        this.liquidacaoService = liquidacaoService;
    }

    /**
     * Resumo dos valores aprovados da guia: o que a OCS cobrou, o que a
     * Lisura aprovou e o total a liquidar. É o que a tela de revisão mostra
     * antes da confirmação, e o comprovante depois dela.
     */
    @GetMapping("/{guiaId}")
    public LiquidacaoResumoResponse resumir(@PathVariable("guiaId") Long guiaId) {
        return liquidacaoService.resumir(guiaId);
    }

    /**
     * Confirma a liquidação da guia.
     * Devolve o resumo atualizado, no mesmo formato do GET.
     */
    @PatchMapping("/{guiaId}")
    public LiquidacaoResumoResponse liquidar(@PathVariable("guiaId") Long guiaId) {
        return liquidacaoService.liquidar(guiaId);
    }

    // Erros de regra voltam com { "message": "..." }, que o frontend mostra ao usuário

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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAcessoNegado(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    }
}
