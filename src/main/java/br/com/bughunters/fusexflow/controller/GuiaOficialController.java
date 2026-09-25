package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.entity.GuiaOficial;
import br.com.bughunters.fusexflow.service.GuiaOficialService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/guias")
public class GuiaOficialController {

    private final GuiaOficialService service;

    public GuiaOficialController(GuiaOficialService service) {
        this.service = service;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("solicitacaoId") Long solicitacaoId) {
        try {
            GuiaOficial guia = service.salvar(file, solicitacaoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(guia);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao salvar arquivo.");
        }
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizar(@PathVariable Long id) {
        try {
            GuiaOficial guia = service.finalizar(id);
            return ResponseEntity.ok(guia);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}