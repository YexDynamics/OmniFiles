package com.eam.demoAPI.persistence.repository;

import com.eam.demoAPI.persistence.entity.Tarea;
import com.eam.demoAPI.persistence.entity.enums.EstadoTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByDocumentoId(Long documentoId);

    List<Tarea> findByUsuarioAsignadoId(Long usuarioId);

    List<Tarea> findByUsuarioAsignadoIdAndEstado(Long usuarioId, EstadoTarea estado);
}
