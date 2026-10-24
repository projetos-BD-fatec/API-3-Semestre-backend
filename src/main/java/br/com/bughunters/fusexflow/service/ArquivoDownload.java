package br.com.bughunters.fusexflow.service;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

/**
 * Arquivo pronto para ser devolvido pelo controller.
 */
public record ArquivoDownload(
        Resource recurso,
        MediaType tipo,
        String nome
) {
}
