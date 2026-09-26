package br.com.bughunters.fusexflow.entity;

import br.com.bughunters.fusexflow.enums.StatusGuia;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "guia_oficial")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GuiaOficial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_guia_oficial")
    private Long id;

    @Column(name = "solicitacao_id", nullable = false)
    private Long solicitacaoId;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "caminho_arquivo", nullable = false)
    private String caminhoArquivo;

    @Column(name = "data_upload", nullable = false)
    private LocalDateTime dataUpload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusGuia status;
}