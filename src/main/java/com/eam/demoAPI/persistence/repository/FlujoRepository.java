package com.eam.demoAPI.persistence.repository;

import com.eam.demoAPI.persistence.entity.Flujo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlujoRepository extends JpaRepository<Flujo, Long> {

    Optional<Flujo> findByTipoDocumentoId(Long tipoDocumentoId);
}
