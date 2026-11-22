package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.request.OcsCadastroRequest;
import br.com.bughunters.fusexflow.service.OcsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Cadastro público de OCS (tela pages/auth/cadastro-ocs.html).
 * Liberado sem token no SecurityConfig, como o POST /usuarios.
 *
 * Os erros voltam como {"message": "..."}, que é o formato que a tela lê.
 */
@RestController
@RequestMapping("/ocs")
public class OcsController {

    private final OcsService ocsService;

    public OcsController(OcsService ocsService) {
        this.ocsService = ocsService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> cadastrar(@Valid @RequestBody OcsCadastroRequest request) {

        ocsService.cadastrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "OCS cadastrada com sucesso."));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflito(IllegalStateException erro) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", erro.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> invalido(MethodArgumentNotValidException erro) {
        String mensagem = erro.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(campo -> campo.getDefaultMessage())
                .orElse("Dados inválidos.");

        return ResponseEntity
                .badRequest()
                .body(Map.of("message", mensagem));
    }
}
