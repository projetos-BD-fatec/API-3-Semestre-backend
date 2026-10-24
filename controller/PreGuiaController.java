package br.com.bughunters.fusexflow.controller;

import br.com.bughunters.fusexflow.dto.request.PreGuiaRequest;
import br.com.bughunters.fusexflow.dto.response.PreGuiaResponse;
import br.com.bughunters.fusexflow.service.ArquivoDownload;
import br.com.bughunters.fusexflow.service.PreGuiaService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/pre-guias")
public class PreGuiaController {

    private final PreGuiaService preGuiaService;

    public PreGuiaController(PreGuiaService preGuiaService) {
        this.preGuiaService = preGuiaService;
    }

    /**
     * Uso futuro: painel do funcionário FUSEX (todas as solicitações).
     * Ainda sem controle de acesso — não expor no frontend do usuário comum.
     */
    @GetMapping
    public List<PreGuiaResponse> findAll() {
        return preGuiaService.findAll();
    }

    @GetMapping("/me")
    public List<PreGuiaResponse> findMinhas() {
        return preGuiaService.findMinhas();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PreGuiaResponse> criar(@RequestPart("encaminhamento") MultipartFile arquivo,
                                                 @RequestPart("dados") PreGuiaRequest request) {
        PreGuiaResponse response = preGuiaService.criar(request, arquivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Arquivo de encaminhamento anexado à pré-guia (só o dono acessa).
     * O frontend baixa com o token e abre como blob em outra aba.
     */
    @GetMapping("/{id}/arquivo")
    public ResponseEntity<Resource> arquivo(@PathVariable("id") Long id) {

        ArquivoDownload arquivo = preGuiaService.buscarArquivo(id);

        // Só PDF/JPG/PNG são exibidos no navegador; o resto vira download
        boolean exibivel = !MediaType.APPLICATION_OCTET_STREAM.equals(arquivo.tipo());

        ContentDisposition disposicao = ContentDisposition
                .builder(exibivel ? "inline" : "attachment")
                .filename(arquivo.nome())
                .build();

        return ResponseEntity.ok()
                .contentType(arquivo.tipo())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicao.toString())
                .cacheControl(CacheControl.noStore())
                .body(arquivo.recurso());
    }
}