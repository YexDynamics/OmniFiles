package com.eam.demoAPI.persistence.repository;

import com.eam.demoAPI.persistence.entity.EstadoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoDocumentoRepository extends JpaRepository<EstadoDocumento, Long> {
    Optional<EstadoDocumento> findByNombre(String nombre);
}
