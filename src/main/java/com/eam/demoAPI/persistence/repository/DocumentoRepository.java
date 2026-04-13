package com.eam.demoAPI.persistence.repository;

import com.eam.demoAPI.persistence.entity.Documento;
import com.eam.demoAPI.persistence.entity.enums.EstadoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByEstado(EstadoDocumento estado);

    List<Documento> findByUsuarioId(Long usuarioId);

    List<Documento> findByEstadoAndUsuarioId(EstadoDocumento estado, Long usuarioId);

    List<Documento> findByEliminadoTrue();
}