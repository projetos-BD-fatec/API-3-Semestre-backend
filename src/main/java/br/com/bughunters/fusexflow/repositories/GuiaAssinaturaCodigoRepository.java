package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.GuiaAssinaturaCodigo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GuiaAssinaturaCodigoRepository extends JpaRepository<GuiaAssinaturaCodigo, Long> {

    /** Códigos ainda não usados/substituídos/bloqueados da guia. */
    List<GuiaAssinaturaCodigo> findByIdGuiaOficialAndInvalidadoEmIsNull(Long idGuiaOficial);

    /** O código vigente da guia: não invalidado e ainda dentro do prazo. */
    Optional<GuiaAssinaturaCodigo> findFirstByIdGuiaOficialAndInvalidadoEmIsNullAndExpiraEmAfterOrderByCriadoEmDesc(
            Long idGuiaOficial, LocalDateTime agora);

    /** Códigos vigentes de várias guias (tela do beneficiário). */
    List<GuiaAssinaturaCodigo> findByIdGuiaOficialInAndInvalidadoEmIsNullAndExpiraEmAfter(
            Collection<Long> idsGuiaOficial, LocalDateTime agora);

    /** Quantos códigos foram pedidos para a guia desde o instante informado (limite por hora). */
    long countByIdGuiaOficialAndCriadoEmAfter(Long idGuiaOficial, LocalDateTime desde);
}
