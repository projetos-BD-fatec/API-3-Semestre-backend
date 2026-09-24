package br.com.bughunters.fusexflow.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ArquivoService {
    private final Path diretorio = Paths.get("uploads/pre-guias");
    public String salvar(MultipartFile arquivo) throws IOException {
        Files.createDirectories(diretorio);

        String extensao = "";

        String nomeOriginal = arquivo.getOriginalFilename();

        if (nomeOriginal != null && nomeOriginal.contains(".")) {
            extensao = nomeOriginal.substring(
                    nomeOriginal.lastIndexOf(".")
            );
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String codigo = UUID.randomUUID().toString().substring(0, 6);

        String nomeArquivo = "PREGUIA_" + "_" + timestamp + "_" + codigo + extensao;

        Path destino = diretorio.resolve(nomeArquivo);

        Files.copy(
                arquivo.getInputStream(),
                destino,
                StandardCopyOption.REPLACE_EXISTING
        );
        return nomeArquivo;
    }
}
