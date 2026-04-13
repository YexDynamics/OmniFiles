package com.eam.demoAPI.persistence.dao;

import com.eam.demoAPI.business.dto.UsuarioDTO;
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

    public UsuarioDTO save(UsuarioDTO dto) {

        Usuario entity = usuarioMapper.toEntity(dto);

        Rol rol = rolRepository.findById(dto.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        entity.setRol(rol);
        entity.setEstado(true);

        Usuario saved = usuarioRepository.save(entity);

        return usuarioMapper.toDTO(saved);
    }

    public Optional<UsuarioDTO> findById(Long id) {
        return usuarioRepository.findById(id)
                .map(usuarioMapper::toDTO);
    }

    public List<UsuarioDTO> findAll() {
        return usuarioMapper.toDTOList(usuarioRepository.findAll());
    }

    public List<UsuarioDTO> findByOrganizacionId(Long organizacionId) {
        return usuarioMapper.toDTOList(
                usuarioRepository.findByOrganizacionId(organizacionId)
        );
    }

    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public Optional<UsuarioDTO> update(Long id, UsuarioDTO dto) {
        return usuarioRepository.findById(id)
                .map(existing -> {

                    usuarioMapper.updateEntityFromDTO(dto, existing);

                    if (dto.getRolId() != null) {
                        Rol rol = rolRepository.findById(dto.getRolId())
                                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
                        existing.setRol(rol);
                    }

                    Usuario updated = usuarioRepository.save(existing);
                    return usuarioMapper.toDTO(updated);
                });
    }

    // pero NO usar codigo viejo
    public boolean delete(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}