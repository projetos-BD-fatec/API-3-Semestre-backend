package br.com.bughunters.fusexflow.dto;

import java.math.BigDecimal;

public record PreGuiaItemResponse(Long idPreGuiaItem,
                                  String nmExame,
                                  String nmPrestador,
                                  String dsLogradouro,
                                  String nrEndereco,
                                  String dsComplemento,
                                  String nmBairro,
                                  String nmCidade,
                                  String sgUf,
                                  BigDecimal valor,
                                  String status) {

}
