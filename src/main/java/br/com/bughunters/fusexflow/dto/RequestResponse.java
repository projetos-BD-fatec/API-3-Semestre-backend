package br.com.bughunters.fusexflow.dto;

public record RequestResponse(
        Long id,
        String exam,
        String clinic,
        String date,
        String status
) {

}