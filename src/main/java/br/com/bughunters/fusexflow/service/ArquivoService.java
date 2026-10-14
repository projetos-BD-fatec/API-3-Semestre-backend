package br.com.bughunters.fusexflow.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
public class ArquivoService {

    /**
     * Extensões aceitas e o tipo com que cada uma é devolvida ao navegador.
     * Mesma regra do frontend (PDF, JPG e PNG).
     */
    private static final Map<String, MediaType> TIPOS_PERMITIDOS = Map.of(
            ".pdf", MediaType.APPLICATION_PDF,
            ".jpg", MediaType.IMAGE_JPEG,
            ".jpeg", MediaType.IMAGE_JPEG,
            ".png", MediaType.IMAGE_PNG
    );

    private final Path diretorio = Paths.get("uploads/pre-guias");

    public String salvar(MultipartFile arquivo) throws IOException {

        String extensao = extrairExtensao(arquivo.getOriginalFilename());

        // Também protege contra extensões estranhas vindas do cliente
        // (ex.: "arquivo.x/../../y"), que escapariam da pasta de uploads.
        if (!TIPOS_PERMITIDOS.containsKey(extensao)) {
            throw new IllegalArgumentException("Tipo de arquivo não permitido. Envie PDF, JPG ou PNG.");
        }

        Files.createDirectories(diretorio);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String codigo = UUID.randomUUID().toString().substring(0, 6);

        String nomeArquivo = "PREGUIA" + "_" + timestamp + "_" + codigo + extensao;

        Path destino = diretorio.resolve(nomeArquivo);

        try (InputStream entrada = arquivo.getInputStream()) {
            Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
        }

        return nomeArquivo;
    }

    /**
     * Carrega um arquivo salvo anteriormente.
     * Rejeita nomes que tentem sair da pasta de uploads.
     */
    public Resource carregar(String nomeArquivo) {

        Path base = diretorio.toAbsolutePath().normalize();
        Path arquivo = base.resolve(nomeArquivo).normalize();

        if (!arquivo.startsWith(base)) {
            throw new IllegalArgumentException("Nome de arquivo inválido.");
        }

        try {
            Resource recurso = new UrlResource(arquivo.toUri());

            if (!recurso.exists() || !recurso.isReadable()) {
                throw new EntityNotFoundException("Arquivo não encontrado: " + nomeArquivo);
            }

            return recurso;

        } catch (MalformedURLException e) {
            throw new IllegalStateException("Não foi possível acessar o arquivo.", e);
        }
    }

    /**
     * Tipo de conteúdo pela extensão. Desconhecido vira octet-stream
     * (o controller devolve como download, não como exibição).
     */
    public MediaType tipoDoArquivo(String nomeArquivo) {
        return TIPOS_PERMITIDOS.getOrDefault(
                extrairExtensao(nomeArquivo),
                MediaType.APPLICATION_OCTET_STREAM
        );
    }

    private String extrairExtensao(String nome) {
        if (nome == null || !nome.contains(".")) {
            return "";
        }

        return nome.substring(nome.lastIndexOf(".")).toLowerCase();
    }
}
