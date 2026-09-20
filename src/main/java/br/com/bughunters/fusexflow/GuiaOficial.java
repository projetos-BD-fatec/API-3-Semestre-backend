package br.com.bughunters.fusexflow.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "guia_oficial")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GuiaOficial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "caminho_arquivo", nullable = false)
    private String caminhoArquivo;

    @Column(name = "data_upload", nullable = false)
    private LocalDateTime dataUpload;

    // Ajuste para o relacionamento real da US03 (ex.: Solicitacao, Pedido, etc.)
    @Column(name = "solicitacao_id")
    private Long solicitacaoId;
}