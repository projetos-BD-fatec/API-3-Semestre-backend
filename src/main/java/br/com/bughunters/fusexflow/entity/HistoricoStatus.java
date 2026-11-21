package br.com.bughunters.fusexflow.entity;

import br.com.bughunters.fusexflow.enums.EtapaFluxo;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Um evento da trilha de status do processo (US17 / SCRUM-65).
 *
 * A trilha é ancorada na pré-guia, e não na guia oficial, porque a primeira
 * etapa (solicitação) acontece antes de a guia existir. Da emissão em diante
 * os eventos também carregam o id da guia.
 *
 * O registro é só de leitura depois de criado: status mudou, ficou gravado.
 */
@Entity
@Table(name = "HISTORICO_STATUS")
public class HistoricoStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HISTORICO")
    private Long idHistorico;

    @Column(name = "ID_PRE_GUIA", nullable = false, updatable = false)
    private Long idPreGuia;

    @Column(name = "ID_GUIA_OFICIAL", updatable = false)
    private Long idGuiaOficial;

    @Enumerated(EnumType.STRING)
    @Column(name = "ETAPA", length = 20, nullable = false, updatable = false)
    private EtapaFluxo etapa;

    @Column(name = "STATUS_ANTERIOR", length = 20, updatable = false)
    private String statusAnterior;

    @Column(name = "STATUS_NOVO", length = 20, nullable = false, updatable = false)
    private String statusNovo;

    /** Quem fez a mudança. Nulo quando a ação não parte de um usuário do FUSEx. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USUARIO")
    private Usuario usuario;

    @Column(name = "OBSERVACAO", length = 400, updatable = false)
    private String observacao;

    @Column(name = "REGISTRADO_EM", nullable = false, updatable = false)
    private LocalDateTime registradoEm;

    protected HistoricoStatus() {
    }

    public HistoricoStatus(Long idPreGuia,
                           Long idGuiaOficial,
                           EtapaFluxo etapa,
                           String statusAnterior,
                           String statusNovo,
                           Usuario usuario,
                           String observacao) {

        this.idPreGuia = idPreGuia;
        this.idGuiaOficial = idGuiaOficial;
        this.etapa = etapa;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.usuario = usuario;
        this.observacao = observacao;
        this.registradoEm = LocalDateTime.now();
    }

    public Long getIdHistorico() {
        return idHistorico;
    }

    public Long getIdPreGuia() {
        return idPreGuia;
    }

    public Long getIdGuiaOficial() {
        return idGuiaOficial;
    }

    public EtapaFluxo getEtapa() {
        return etapa;
    }

    public String getStatusAnterior() {
        return statusAnterior;
    }

    public String getStatusNovo() {
        return statusNovo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getObservacao() {
        return observacao;
    }

    public LocalDateTime getRegistradoEm() {
        return registradoEm;
    }
}
