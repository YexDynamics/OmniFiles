package com.eam.demoAPI.persistence.repository;

import com.eam.demoAPI.persistence.entity.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {

    boolean existsByNombre(String nombre);

    List<TipoDocumento> findByEstadoTrue();
}
