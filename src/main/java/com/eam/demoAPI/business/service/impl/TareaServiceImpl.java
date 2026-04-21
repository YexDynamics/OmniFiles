package com.eam.demoAPI.business.service.impl;

import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.business.dto.TareaDTO;
import com.eam.demoAPI.business.service.TareaService;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.dao.DocumentoDAO;
import com.eam.demoAPI.persistence.dao.TareaDAO;
import com.eam.demoAPI.persistence.entity.Tarea;
import com.eam.demoAPI.persistence.entity.enums.EstadoDocumento;
import com.eam.demoAPI.persistence.entity.enums.EstadoTarea;
import com.eam.demoAPI.persistence.entity.enums.TipoAccion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TareaServiceImpl implements TareaService {

    private final TareaDAO tareaDAO;
    private final DocumentoDAO documentoDAO;

    @Override
    public TareaDTO crearTarea(TareaDTO dto) {
        log.info("Creando tarea para documento ID: {}", dto.getDocumentoId());

        // Validar que el documento existe
        documentoDAO.findById(dto.getDocumentoId())
                .orElseThrow(() -> new NotFoundException("Documento no encontrado"));

        dto.setEstado(EstadoTarea.PENDIENTE);
        dto.setFechaAsignacion(LocalDateTime.now());

        return tareaDAO.save(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public TareaDTO getTareaById(Long id) {
        return tareaDAO.findById(id)
                .orElseThrow(() -> new NotFoundException("Tarea no encontrada con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TareaDTO> getTareasByDocumento(Long documentoId) {
        return tareaDAO.findByDocumentoId(documentoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TareaDTO> getTareasPendientesByUsuario(Long usuarioId) {
        return tareaDAO.findPendientesByUsuario(usuarioId);
    }

    @Override
    public TareaDTO aprobar(Long id, String observaciones) {
        log.info("Aprobando tarea ID: {}", id);
        Tarea tarea = resolverTarea(id, EstadoTarea.APROBADO, observaciones);
        actualizarEstadoDocumento(tarea.getDocumento().getId(), EstadoDocumento.APROBADO, TipoAccion.CAMBIO_ESTADO);
        return tareaDAO.saveEntity(tarea);
    }

    @Override
    public TareaDTO rechazar(Long id, String observaciones) {
        log.info("Rechazando tarea ID: {}", id);
        Tarea tarea = resolverTarea(id, EstadoTarea.RECHAZADO, observaciones);
        actualizarEstadoDocumento(tarea.getDocumento().getId(), EstadoDocumento.RECHAZADO, TipoAccion.CAMBIO_ESTADO);
        return tareaDAO.saveEntity(tarea);
    }

    @Override
    public TareaDTO solicitarCorreccion(Long id, String observaciones) {
        log.info("Solicitando corrección tarea ID: {}", id);
        Tarea tarea = resolverTarea(id, EstadoTarea.CORRECCION, observaciones);
        actualizarEstadoDocumento(tarea.getDocumento().getId(), EstadoDocumento.CREADO, TipoAccion.CAMBIO_ESTADO);
        return tareaDAO.saveEntity(tarea);
    }

    // ─── privados ────────────────────────────────────────────────────────────

    private Tarea resolverTarea(Long id, EstadoTarea nuevoEstado, String observaciones) {
        Tarea tarea = tareaDAO.findEntityById(id)
                .orElseThrow(() -> new NotFoundException("Tarea no encontrada con ID: " + id));

        if (tarea.getEstado() != EstadoTarea.PENDIENTE) {
            throw new IllegalArgumentException("La tarea ya fue resuelta");
        }

        tarea.setEstado(nuevoEstado);
        tarea.setObservaciones(observaciones);
        tarea.setFechaResolucion(LocalDateTime.now());

        return tarea;
    }

    private void actualizarEstadoDocumento(Long documentoId, EstadoDocumento estado, TipoAccion accion) {
        documentoDAO.findById(documentoId).ifPresent(doc -> {
            doc.setEstado(estado);
            doc.setUpdatedAt(LocalDateTime.now());
            documentoDAO.update(documentoId, doc);
        });
    }
}
