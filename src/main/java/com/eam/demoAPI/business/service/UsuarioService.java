package com.eam.demoAPI.business.service;

import com.eam.demoAPI.business.dto.UsuarioDTO;
import com.eam.demoAPI.business.dto.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {

    UsuarioResponseDTO createUsuario(UsuarioDTO usuarioDTO);

    UsuarioResponseDTO getUsuarioById(Long id);

    List<UsuarioResponseDTO> getUsuarios();

    UsuarioResponseDTO updateUsuario(Long id, UsuarioDTO usuarioDTO);

    void cambiarEstado(Long id, boolean activo);

    void deleteUsuario(Long id);
}
