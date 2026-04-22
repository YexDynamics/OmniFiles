package com.eam.demoAPI.persistence.dao;

import com.eam.demoAPI.business.dto.UsuarioDTO;
import com.eam.demoAPI.business.dto.UsuarioResponseDTO;
import com.eam.demoAPI.persistence.entity.Rol;
import com.eam.demoAPI.persistence.entity.Usuario;
import com.eam.demoAPI.persistence.mapper.UsuarioMapper;
import com.eam.demoAPI.persistence.repository.RolRepository;
import com.eam.demoAPI.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsuarioDAO {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioResponseDTO save(UsuarioDTO dto) {
        Usuario entity = usuarioMapper.toEntity(dto);
        if (dto.getRolId() != null) {
            Rol rol = rolRepository.findById(dto.getRolId())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            entity.setRol(rol);
        }
        entity.setEstado(true);
        return usuarioMapper.toResponseDTO(usuarioRepository.save(entity));
    }

    public Optional<UsuarioDTO> findById(Long id) {
        return usuarioRepository.findById(id).map(usuarioMapper::toDTO);
    }

    public Optional<UsuarioResponseDTO> findResponseById(Long id) {
        return usuarioRepository.findById(id).map(usuarioMapper::toResponseDTO);
    }

    public List<UsuarioResponseDTO> findAll() {
        return usuarioMapper.toResponseDTOList(usuarioRepository.findAll());
    }

    public Optional<UsuarioDTO> findByEmail(String email) {
        return usuarioRepository.findByEmail(email).map(usuarioMapper::toDTO);
    }

    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public Optional<UsuarioResponseDTO> update(Long id, UsuarioDTO dto) {
        return usuarioRepository.findById(id).map(existing -> {
            usuarioMapper.updateEntityFromDTO(dto, existing);
            if (dto.getRolId() != null) {
                Rol rol = rolRepository.findById(dto.getRolId())
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
                existing.setRol(rol);
            }
            return usuarioMapper.toResponseDTO(usuarioRepository.save(existing));
        });
    }

    public boolean delete(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
