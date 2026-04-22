package com.eam.demoAPI.business.service.impl;

import com.eam.demoAPI.business.dto.UsuarioDTO;
import com.eam.demoAPI.business.dto.UsuarioResponseDTO;
import com.eam.demoAPI.business.service.UsuarioService;
import com.eam.demoAPI.exception.ConflictException;
import com.eam.demoAPI.exception.NotFoundException;
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
    public UsuarioResponseDTO createUsuario(UsuarioDTO dto) {
        log.info("Creando usuario: {}", dto.getNombre());
        validateUsuario(dto);

        if (usuarioDAO.existsByEmail(dto.getEmail())) {
            throw new ConflictException("El email ya está en uso");
        }

        dto.setActivo(true);
        return usuarioDAO.save(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO getUsuarioById(Long id) {
        return usuarioDAO.findResponseById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> getUsuarios() {
        return usuarioDAO.findAll();
    }

    @Override
    public UsuarioResponseDTO updateUsuario(Long id, UsuarioDTO dto) {
        log.info("Actualizando usuario ID: {}", id);
        // Verifica existencia
        usuarioDAO.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        return usuarioDAO.update(id, dto)
                .orElseThrow(() -> new RuntimeException("Error al actualizar usuario"));
    }

    @Override
    public void cambiarEstado(Long id, boolean activo) {
        log.info("Cambiando estado usuario ID: {} -> {}", id, activo);
        UsuarioDTO usuario = usuarioDAO.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        usuario.setActivo(activo);
        usuarioDAO.update(id, usuario);
    }

    @Override
    public void deleteUsuario(Long id) {
        log.info("Eliminando usuario ID: {}", id);
        usuarioDAO.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + id));
        boolean deleted = usuarioDAO.delete(id);
        if (!deleted) {
            throw new RuntimeException("No se pudo eliminar el usuario");
        }
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
