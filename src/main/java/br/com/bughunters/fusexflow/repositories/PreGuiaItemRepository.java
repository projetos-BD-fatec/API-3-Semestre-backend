package br.com.bughunters.fusexflow.repositories;

import br.com.bughunters.fusexflow.entity.PreGuiaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreGuiaItemRepository extends JpaRepository<PreGuiaItem, Long> {

    List<PreGuiaItem> findByPreGuia_IdPreGuia(Long idPreGuia);
}