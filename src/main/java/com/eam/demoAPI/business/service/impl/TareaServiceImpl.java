package com.eam.demoAPI.business.service.impl;

import com.eam.demoAPI.business.dto.TareaDTO;
import com.eam.demoAPI.business.service.TareaService;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.business.service.EmailService;
import com.eam.demoAPI.persistence.dao.DocumentoDAO;
import com.eam.demoAPI.persistence.dao.UsuarioDAO;
import com.eam.demoAPI.persistence.dao.FlujoDAO;
import com.eam.demoAPI.persistence.dao.TareaDAO;
import com.eam.demoAPI.persistence.entity.Documento;
import com.eam.demoAPI.persistence.repository.DocumentoRepository;

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

    private static final String ESTADO_PENDIENTE  = "PENDIENTE";
    private static final String ESTADO_APROBADO   = "APROBADO";
    private static final String ESTADO_RECHAZADO  = "RECHAZADO";
    private static final String ESTADO_CORRECCION = "CORRECCION";

    private static final String DOC_EN_REVISION = "EN_REVISION";
    private static final String DOC_APROBADO    = "APROBADO";
    private static final String DOC_RECHAZADO   = "RECHAZADO";
    private static final String DOC_CREADO      = "CREADO";

    private final TareaDAO tareaDAO;
    private final DocumentoDAO documentoDAO;
    private final FlujoDAO flujoDAO;                   // NUEVO
    private final DocumentoRepository documentoRepository; // NUEVO
    private final EmailService emailService;              // NUEVO
    private final UsuarioDAO usuarioDAO;                  // NUEVO

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

        // 1. Resolver la tarea actual
        TareaDTO tarea = tareaDAO.resolver(id, ESTADO_APROBADO, observaciones);

        // 2. Buscar la siguiente etapa del flujo
        Documento doc = documentoRepository.findById(tarea.getDocumentoId())
                .orElseThrow(() -> new NotFoundException("Documento no encontrado"));

        if (doc.getTipoDocumento() == null) {
            // Sin plantilla: aprobar directamente
            actualizarEstadoDocumento(tarea.getDocumentoId(), DOC_APROBADO);
            return tarea;
        }

        Long flujoId = doc.getTipoDocumento().getId();
        flujoDAO.findEntityByTipoDocumentoId(flujoId).ifPresentOrElse(flujo -> {

            // Obtener el orden de la etapa actual
            Integer ordenActual = tareaDAO.findEntityById(id)
                    .map(t -> t.getEtapaFlujo().getOrden())
                    .orElse(0);

            flujoDAO.getSiguienteEtapa(flujo.getId(), ordenActual).ifPresentOrElse(
                    siguienteEtapa -> {
                        // Hay siguiente etapa: crear la próxima tarea
                        log.info("Avanzando a etapa {} del flujo", siguienteEtapa.getOrden());
                        tareaDAO.crearDesdeEtapa(siguienteEtapa, doc);
                        actualizarEstadoDocumento(tarea.getDocumentoId(), DOC_EN_REVISION);
                    },
                    () -> {
                        // Era la última etapa: documento aprobado
                        log.info("Flujo completado — documento {} aprobado", doc.getId());
                        actualizarEstadoDocumento(tarea.getDocumentoId(), DOC_APROBADO);
                        // Notificar al creador
                        usuarioDAO.findById((long) doc.getUsuario().getId()).ifPresent(u ->
                                emailService.notificarAprobacion(u.getEmail(), doc.getNombre()));
                    }
            );
        }, () -> actualizarEstadoDocumento(tarea.getDocumentoId(), DOC_APROBADO));

        return tarea;
    }

    @Override
    public TareaDTO rechazar(Long id, String observaciones) {
        log.info("Rechazando tarea ID: {}", id);
        // Rechazar detiene el flujo completamente
        TareaDTO tarea = tareaDAO.resolver(id, ESTADO_RECHAZADO, observaciones);
        actualizarEstadoDocumento(tarea.getDocumentoId(), DOC_RECHAZADO);

        // Notificar al creador del documento
        documentoRepository.findById(tarea.getDocumentoId()).ifPresent(doc ->
                usuarioDAO.findById(doc.getUsuario().getId()).ifPresent(u ->
                        emailService.notificarRechazo(u.getEmail(), doc.getNombre(), observaciones)));

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
