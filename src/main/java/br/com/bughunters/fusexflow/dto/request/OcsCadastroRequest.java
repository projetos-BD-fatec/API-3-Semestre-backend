package br.com.bughunters.fusexflow.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cadastro de uma OCS pela tela pages/auth/cadastro-ocs.html.
 * Cria o PRESTADOR (dados da OCS) e o USUARIO responsável pelo acesso,
 * que faz login pelo CNPJ.
 */
public record OcsCadastroRequest(

        @NotNull(message = "Os dados da OCS são obrigatórios")
        @Valid
        Prestador prestador,

        @NotNull(message = "Os dados do responsável são obrigatórios")
        @Valid
        Responsavel responsavel
) {

    public record Prestador(

            @NotBlank(message = "A razão social é obrigatória")
            @Size(max = 200, message = "A razão social deve ter no máximo 200 caracteres")
            String nomePrestador,

            @Size(max = 200, message = "O nome fantasia deve ter no máximo 200 caracteres")
            String nomeFantasia,

            @NotBlank(message = "O CNPJ é obrigatório")
            @Pattern(regexp = "\\d{14}", message = "O CNPJ deve conter exatamente 14 números")
            String cnpj,

            @NotNull(message = "O endereço é obrigatório")
            @Valid
            Endereco endereco
    ) {
    }

    public record Endereco(

            @NotBlank(message = "O CEP é obrigatório")
            @Pattern(regexp = "\\d{8}", message = "O CEP deve conter exatamente 8 números")
            String cep,

            @NotBlank(message = "O logradouro é obrigatório")
            @Size(max = 200, message = "O logradouro deve ter no máximo 200 caracteres")
            String logradouro,

            @NotBlank(message = "O número é obrigatório")
            @Size(max = 20, message = "O número deve ter no máximo 20 caracteres")
            String numero,

            @Size(max = 100, message = "O complemento deve ter no máximo 100 caracteres")
            String complemento,

            @NotBlank(message = "O bairro é obrigatório")
            @Size(max = 100, message = "O bairro deve ter no máximo 100 caracteres")
            String bairro,

            @NotBlank(message = "A cidade é obrigatória")
            @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
            String cidade,

            @NotBlank(message = "A UF é obrigatória")
            @Pattern(regexp = "[A-Za-z]{2}", message = "A UF deve ter 2 letras")
            String uf
    ) {
    }

    public record Responsavel(

            @NotBlank(message = "O nome do responsável é obrigatório")
            @Size(max = 150, message = "O nome do responsável deve ter no máximo 150 caracteres")
            String nomeUsuario,

            @NotBlank(message = "O CPF do responsável é obrigatório")
            @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 números")
            String cpf,

            @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
            String telefone,

            @NotBlank(message = "O e-mail é obrigatório")
            @Email(message = "Informe um e-mail válido")
            @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
            String email,

            @NotBlank(message = "A senha é obrigatória")
            @Size(min = 6, max = 100, message = "A senha deve possuir entre 6 e 100 caracteres")
            String senha
    ) {
    }
}
