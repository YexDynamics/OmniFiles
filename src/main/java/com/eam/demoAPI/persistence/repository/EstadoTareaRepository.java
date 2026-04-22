package com.eam.demoAPI.persistence.repository;

import com.eam.demoAPI.persistence.entity.EstadoTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoTareaRepository extends JpaRepository<EstadoTarea, Long> {
    Optional<EstadoTarea> findByNombre(String nombre);
}
