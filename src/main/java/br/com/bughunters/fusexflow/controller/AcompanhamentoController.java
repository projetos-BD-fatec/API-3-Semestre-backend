package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.response.AcompanhamentoGuiaResponse;
import br.com.bughunters.fusexflow.dto.response.AcompanhamentoResumoResponse;
import br.com.bughunters.fusexflow.enums.EtapaFluxo;
import br.com.bughunters.fusexflow.service.AcompanhamentoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Acompanhamento do fluxo das guias (US17 / SCRUM-65).
 * Visão interna do FUSEx.
 */
@RestController
@RequestMapping("/acompanhamento")
public class AcompanhamentoController {

    private final AcompanhamentoService acompanhamentoService;

    public AcompanhamentoController(AcompanhamentoService acompanhamentoService) {
        this.acompanhamentoService = acompanhamentoService;
    }

    /**
     * Guias com a etapa do fluxo em que cada uma está.
     * Filtro opcional: ?etapa=SOLICITACAO|EMISSAO|REALIZACAO|FATURAMENTO|LISURA|LIQUIDACAO
     */
    @GetMapping
    public List<AcompanhamentoResumoResponse> listar(
            @RequestParam(value = "etapa", required = false) String etapa) {

        return acompanhamentoService.listar(EtapaFluxo.doFiltro(etapa));
    }

    /** Linha do tempo da guia: etapas concluídas, com data, e as que faltam. */
    @GetMapping("/{guiaId}")
    public AcompanhamentoGuiaResponse acompanhar(@PathVariable("guiaId") Long guiaId) {
        return acompanhamentoService.acompanhar(guiaId);
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
