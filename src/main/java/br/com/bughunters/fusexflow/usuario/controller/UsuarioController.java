package br.com.bughunters.fusexflow.usuario.controller;

import br.com.bughunters.fusexflow.usuario.dto.UsuarioCadastroDTO;
import br.com.bughunters.fusexflow.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> cadastrar(
            @Valid @RequestBody UsuarioCadastroDTO dto) {

        service.cadastrar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Usuário cadastrado com sucesso.");
    }
}