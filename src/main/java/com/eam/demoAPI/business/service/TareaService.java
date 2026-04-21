package com.eam.demoAPI.business.service;

import com.eam.demoAPI.business.dto.TareaDTO;

import java.util.List;

public interface TareaService {

    TareaDTO crearTarea(TareaDTO dto);

    TareaDTO getTareaById(Long id);

    List<TareaDTO> getTareasByDocumento(Long documentoId);

    List<TareaDTO> getTareasPendientesByUsuario(Long usuarioId);

    TareaDTO aprobar(Long id, String observaciones);

    TareaDTO rechazar(Long id, String observaciones);

    TareaDTO solicitarCorreccion(Long id, String observaciones);
}
