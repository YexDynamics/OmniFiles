package com.eam.demoAPI.persistence.repository;

import com.eam.demoAPI.persistence.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByEstado(String estado);

    List<Documento> findByUsuarioId(Long usuarioId);

    List<Documento> findByEstadoAndUsuarioId(String estado, Long usuarioId);

    List<Documento> findByEliminadoTrue();
}