package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.entity.HistoricoStatus;
import br.com.bughunters.fusexflow.entity.Usuario;
import br.com.bughunters.fusexflow.enums.EtapaFluxo;
import br.com.bughunters.fusexflow.repositories.HistoricoStatusRepository;
import br.com.bughunters.fusexflow.repositories.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Grava a trilha de status do processo (US17 / SCRUM-65).
 *
 * É chamado pelos serviços de cada etapa no momento em que o status muda.
 * Fica fora deles de propósito: a regra de negócio continua em PreGuiaService,
 * GuiaOficialService, EspelhoService e LisuraService; aqui só se registra o
 * que aconteceu.
 *
 * O registro entra na mesma transação de quem chamou, de propósito: ou a
 * mudança de status e o seu registro acontecem juntos, ou nenhum dos dois
 * acontece. Gravar em transação separada deixaria a timeline mostrar uma
 * etapa que o banco desfez no rollback.
 */
@Service
public class HistoricoStatusService {

    private final HistoricoStatusRepository historicoRepository;
    private final UsuarioRepository usuarioRepository;

    public HistoricoStatusService(HistoricoStatusRepository historicoRepository,
                                  UsuarioRepository usuarioRepository) {
        this.historicoRepository = historicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Registra uma mudança de status.
     *
     * @param etapa           etapa do fluxo a que a mudança pertence
     * @param idPreGuia       processo (sempre obrigatório)
     * @param idGuiaOficial   guia, quando já emitida; nulo na solicitação
     * @param statusAnterior  de onde saiu; nulo no primeiro evento da etapa
     * @param statusNovo      para onde foi
     * @param observacao      texto curto que aparece na linha do tempo
     */
    @Transactional
    public void registrar(EtapaFluxo etapa,
                          Long idPreGuia,
                          Long idGuiaOficial,
                          String statusAnterior,
                          String statusNovo,
                          String observacao) {

        if (etapa == null || idPreGuia == null || statusNovo == null) {
            return;
        }

        historicoRepository.save(new HistoricoStatus(
                idPreGuia,
                idGuiaOficial,
                etapa,
                statusAnterior,
                statusNovo,
                usuarioLogadoOuNulo(),
                observacao
        ));
    }

    /**
     * Usuário autenticado, quando houver.
     *
     * Devolve nulo se quem está agindo não for um usuário do FUSEx — o login
     * de prestador autentica um Prestador, que não tem linha em USUARIO. Nesse
     * caso quem fez a ação fica registrado na observação do evento.
     */
    private Usuario usuarioLogadoOuNulo() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || autenticacao.getName() == null) {
            return null;
        }

        return usuarioRepository.findByDsEmail(autenticacao.getName()).orElse(null);
    }
}
