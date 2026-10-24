
package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.request.PrestadorLoginRequest;
import br.com.bughunters.fusexflow.dto.response.LoginResponse;
import br.com.bughunters.fusexflow.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/prestador")
public class PrestadorAuthController {

    private final AuthService authService;

    public PrestadorAuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Autentica uma OCS pelo CNPJ e senha.
     *
     * POST /prestador/login
     */
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody PrestadorLoginRequest request
    ) {
        return authService.loginPrestador(request);
    }

    /**
     * Retorna HTTP 401 quando as credenciais são inválidas.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(
            BadCredentialsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("CNPJ ou senha inválidos");
    }
}
