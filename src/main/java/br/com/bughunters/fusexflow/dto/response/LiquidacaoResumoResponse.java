package br.com.bughunters.fusexflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Resumo dos valores aprovados de uma guia (US16).
 *
 * Serve a dois momentos: antes de liquidar, é a tela de revisão; depois de
 * liquidar, é o comprovante do que foi processado.
 *
 * @param idGuia            guia oficial
 * @param codigoGuia        código da guia
 * @param nomeBeneficiario  quem abriu a solicitação
 * @param nomePrestador     OCS que enviou o espelho
 * @param statusGuia        status atual da guia
 * @param podeLiquidar      se a liquidação pode ser confirmada agora
 * @param impedimento       por que não pode; nulo quando pode
 * @param itens             os procedimentos, com cobrado, aprovado e decisão
 * @param totalItens        quantos procedimentos o espelho tem
 * @param itensRevisados    quantos a Lisura revisou explicitamente
 * @param totalCobrado      soma do que a OCS cobrou
 * @param totalAprovado     soma do que a Lisura aprovou — o valor a liquidar
 * @param totalDiferenca    cobrado - aprovado; o quanto a Lisura glosou
 * @param espelhoEnviadoEm  quando a OCS enviou o espelho
 * @param revisadoPor       analista da última revisão; nulo se não houve
 * @param revisadoEm        data da última revisão; nula se não houve
 * @param liquidada         se a guia já foi liquidada
 * @param liquidadoEm       quando; nulo se não foi
 * @param liquidadoPor      quem; nulo se não foi
 * @param valorLiquidado    valor registrado; nulo se não foi
 */
public record LiquidacaoResumoResponse(
        Long idGuia,
        String codigoGuia,
        String nomeBeneficiario,
        String nomePrestador,
        String statusGuia,
        boolean podeLiquidar,
        String impedimento,
        List<LiquidacaoItemResponse> itens,
        int totalItens,
        int itensRevisados,
        BigDecimal totalCobrado,
        BigDecimal totalAprovado,
        BigDecimal totalDiferenca,
        LocalDateTime espelhoEnviadoEm,
        String revisadoPor,
        LocalDateTime revisadoEm,
        boolean liquidada,
        LocalDateTime liquidadoEm,
        String liquidadoPor,
        BigDecimal valorLiquidado
) {}
