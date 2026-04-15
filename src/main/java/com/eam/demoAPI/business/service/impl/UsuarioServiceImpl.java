package com.eam.demoAPI.business.service.impl;

import com.eam.demoAPI.business.dto.UsuarioDTO;
import com.eam.demoAPI.business.service.UsuarioService;
import com.eam.demoAPI.persistence.dao.UsuarioDAO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioDAO usuarioDAO;

    @Override
    public UsuarioDTO createUsuario(UsuarioDTO dto) {

        log.info("Creando usuario: {}", dto.getNombre());

        validateUsuario(dto);

        if (usuarioDAO.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El email ya está en uso");
        }

        dto.setActivo(true);

        return usuarioDAO.save(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO getUsuarioById(Long id) {
        return usuarioDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> getUsuarios() {
        return usuarioDAO.findAll();
    }

    @Override
    public UsuarioDTO updateUsuario(Long id, UsuarioDTO dto) {

        log.info("Actualizando usuario ID: {}", id);

        UsuarioDTO existing = usuarioDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (dto.getNombre() != null) {
            existing.setNombre(dto.getNombre());
        }

        if (dto.getEmail() != null) {
            existing.setEmail(dto.getEmail());
        }

        if (dto.getRolId() != null) {
            existing.setRolId(dto.getRolId());
        }

        return usuarioDAO.update(id, existing)
                .orElseThrow(() -> new RuntimeException("Error al actualizar usuario"));
    }

    @Override
    public void cambiarEstado(Long id, boolean activo) {

        log.info("Cambiando estado usuario ID: {} -> {}", id, activo);

        UsuarioDTO usuario = usuarioDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setActivo(activo);

        usuarioDAO.update(id, usuario);
    }

    private void validateUsuario(UsuarioDTO dto) {

        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }

        if (dto.getRolId() == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
    }
}