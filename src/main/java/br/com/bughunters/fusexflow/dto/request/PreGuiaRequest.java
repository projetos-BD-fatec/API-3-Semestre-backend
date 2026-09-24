package br.com.bughunters.fusexflow.dto.request;

import java.util.List;

public record PreGuiaRequest (
        List<PreGuiaItemRequest> itens
) {
}
