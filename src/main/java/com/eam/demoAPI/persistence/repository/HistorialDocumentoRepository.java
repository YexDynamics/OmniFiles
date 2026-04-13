package com.eam.demoAPI.persistence.repository;

import com.eam.demoAPI.persistence.entity.HistorialDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialDocumentoRepository extends JpaRepository<HistorialDocumento, Long> {

    List<HistorialDocumento> findByDocumentoId(Long documentoId);
}
