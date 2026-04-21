package com.eam.demoAPI.business.service.impl;

import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.business.dto.TareaDTO;
import com.eam.demoAPI.business.service.TareaService;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.dao.DocumentoDAO;
import com.eam.demoAPI.persistence.dao.TareaDAO;

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

    // ─── Constantes de estados (leídos desde BD) ─────────────────────────────
    private static final String ESTADO_PENDIENTE  = "PENDIENTE";
    private static final String ESTADO_APROBADO   = "APROBADO";
    private static final String ESTADO_RECHAZADO  = "RECHAZADO";
    private static final String ESTADO_CORRECCION = "CORRECCION";

    // ─── Estados de documento que se actualizan al resolver una tarea ─────────
    private static final String DOC_APROBADO  = "APROBADO";
    private static final String DOC_RECHAZADO = "RECHAZADO";
    private static final String DOC_CREADO    = "CREADO";

    private final TareaDAO tareaDAO;
    private final DocumentoDAO documentoDAO;

    @Override
    public TareaDTO crearTarea(TareaDTO dto) {
        log.info("Creando tarea para documento ID: {}", dto.getDocumentoId());

        documentoDAO.findById(dto.getDocumentoId())
                .orElseThrow(() -> new NotFoundException("Documento no encontrado"));

        dto.setEstado(ESTADO_PENDIENTE);
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
        TareaDTO tarea = tareaDAO.resolver(id, ESTADO_APROBADO, observaciones);
        actualizarEstadoDocumento(tarea.getDocumentoId(), DOC_APROBADO);
        return tarea;
    }

    @Override
    public TareaDTO rechazar(Long id, String observaciones) {
        log.info("Rechazando tarea ID: {}", id);
        TareaDTO tarea = tareaDAO.resolver(id, ESTADO_RECHAZADO, observaciones);
        actualizarEstadoDocumento(tarea.getDocumentoId(), DOC_RECHAZADO);
        return tarea;
    }

    @Override
    public TareaDTO solicitarCorreccion(Long id, String observaciones) {
        log.info("Solicitando corrección tarea ID: {}", id);
        TareaDTO tarea = tareaDAO.resolver(id, ESTADO_CORRECCION, observaciones);
        actualizarEstadoDocumento(tarea.getDocumentoId(), DOC_CREADO);
        return tarea;
    }

    // ─── privados ─────────────────────────────────────────────────────────────

    private void actualizarEstadoDocumento(Long documentoId, String nuevoEstado) {
        documentoDAO.findById(documentoId).ifPresent(doc -> {
            doc.setEstado(nuevoEstado);
            doc.setUpdatedAt(LocalDateTime.now());
            documentoDAO.update(documentoId, doc);
        });
    }
}
