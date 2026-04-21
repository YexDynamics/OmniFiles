package com.eam.demoAPI.persistence.repository;

import com.eam.demoAPI.persistence.entity.TipoAccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoAccionRepository extends JpaRepository<TipoAccion, Long> {
    Optional<TipoAccion> findByNombre(String nombre);
}
