package com.eam.demoAPI.persistence.repository;

import com.eam.demoAPI.persistence.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findByOrganizacionId(Long organizacionId);

    boolean existsByEmail(String email);
}