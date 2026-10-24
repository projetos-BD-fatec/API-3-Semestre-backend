
package br.com.bughunters.fusexflow.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PrestadorLoginRequest(

        @NotBlank(message = "O CNPJ é obrigatório")
        String cnpj,

        @NotBlank(message = "A senha é obrigatória")
        String senha

) {
}
