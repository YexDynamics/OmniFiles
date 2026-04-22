package com.eam.demoAPI.business.service.impl;

import com.eam.demoAPI.business.dto.TipoDocumentoDTO;
import com.eam.demoAPI.business.service.TipoDocumentoService;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.dao.TipoDocumentoDAO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TipoDocumentoServiceImpl implements TipoDocumentoService {

    private final TipoDocumentoDAO tipoDocumentoDAO;

    @Override
    public TipoDocumentoDTO createTipoDocumento(TipoDocumentoDTO dto) {
        log.info("Creando tipo documental: {}", dto.getNombre());
        validateNombre(dto.getNombre());
        return tipoDocumentoDAO.save(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public TipoDocumentoDTO getTipoDocumentoById(Long id) {
        return tipoDocumentoDAO.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo documental no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoDocumentoDTO> getTiposDocumentales() {
        return tipoDocumentoDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoDocumentoDTO> getTiposActivos() {
        return tipoDocumentoDAO.findActivos();
    }

    @Override
    public TipoDocumentoDTO updateTipoDocumento(Long id, TipoDocumentoDTO dto) {
        log.info("Actualizando tipo documental ID: {}", id);
        getTipoDocumentoById(id); // valida que existe
        return tipoDocumentoDAO.update(id, dto)
                .orElseThrow(() -> new RuntimeException("Error al actualizar"));
    }

    @Override
    public void cambiarEstado(Long id, boolean estado) {
        log.info("Cambiando estado tipo documental ID: {} -> {}", id, estado);
        tipoDocumentoDAO.cambiarEstado(id, estado);
    }

    private void validateNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del tipo documental es obligatorio");
        }
    }
}
