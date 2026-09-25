package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.entity.GuiaOficial;
import br.com.bughunters.fusexflow.enums.StatusGuia;
import br.com.bughunters.fusexflow.repositories.GuiaOficialRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class GuiaOficialService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final GuiaOficialRepository repository;

    public GuiaOficialService(GuiaOficialRepository repository) {
        this.repository = repository;
    }

    public GuiaOficial salvar(MultipartFile file, Long solicitacaoId) throws IOException {

        if (file.isEmpty() || !"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Arquivo deve ser um PDF válido.");
        }

        Path diretorio = Paths.get(uploadDir);
        Files.createDirectories(diretorio);

        String nomeUnico = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path destino = diretorio.resolve(nomeUnico).normalize();

        file.transferTo(destino);

        GuiaOficial guia = GuiaOficial.builder()
                .nomeArquivo(file.getOriginalFilename())
                .caminhoArquivo(destino.toString())
                .dataUpload(LocalDateTime.now())
                .solicitacaoId(solicitacaoId)
                .status(StatusGuia.ATIVA)
                .build();

        return repository.save(guia);
    }

    public GuiaOficial finalizar(Long id) {

        GuiaOficial guia = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Guia não encontrada."));

        if (guia.getStatus() != StatusGuia.ATIVA) {
            throw new IllegalStateException("A guia não está ativa/pendente e não pode ser finalizada.");
        }

        guia.setStatus(StatusGuia.FINALIZADA);

        return repository.save(guia);
    }
}