package br.com.bughunters.fusexflow.dto.request;

import java.util.List;

/** Espelhos salvos que a OCS escolheu enviar ao FUSEx. */
public record EnvioLoteRequest(List<Long> idsEspelho) {}
