package com.eam.demoAPI.persistence.repository;

import com.eam.demoAPI.persistence.entity.EtapaFlujo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtapaFlujoRepository extends JpaRepository<EtapaFlujo, Long> {

    List<EtapaFlujo> findByFlujoIdOrderByOrdenAsc(Long flujoId);

    Optional<EtapaFlujo> findByFlujoIdAndOrden(Long flujoId, Integer orden);
}
