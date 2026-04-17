package com.eam.demoAPI.business.service;

import com.eam.demoAPI.business.dto.UsuarioDTO;

import java.util.List;

public interface UsuarioService {

    UsuarioDTO createUsuario(UsuarioDTO usuarioDTO);

    UsuarioDTO getUsuarioById(Long id);

    List<UsuarioDTO> getUsuarios();

    UsuarioDTO updateUsuario(Long id, UsuarioDTO usuarioDTO);

    void cambiarEstado(Long id, boolean activo);

    void deleteUsuario(Long id);
}
