package br.com.bughunters.fusexflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(

        @NotBlank(message = "O nome de usuário é obrigatório")
        @Size(max = 150, message = "O nome de usuário deve ter no máximo 150 caracteres")
        String nomeUsuario,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Informe um e-mail válido")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(
                regexp = "\\d{11}",
                message = "O CPF deve conter exatamente 11 números"
        )
        String cpf,

        @NotBlank(message = "O PREC/CP é obrigatório")
        @Size(max = 15, message = "O PREC/CP deve ter no máximo 30 caracteres")
        String precCp,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
        String telefone,

        @NotBlank(message = "A senha é obrigatória")
        @Size(
                min = 6,
                max = 100,
                message = "A senha deve possuir entre 6 e 100 caracteres"
        )
        String senha
) {
}
