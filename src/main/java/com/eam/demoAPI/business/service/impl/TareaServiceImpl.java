package com.eam.demoAPI.business.service.impl;

import com.eam.demoAPI.business.dto.HistorialDocumentoDTO;
import com.eam.demoAPI.business.dto.TareaDTO;
import com.eam.demoAPI.business.service.TareaService;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.business.service.EmailService;
import com.eam.demoAPI.persistence.dao.DocumentoDAO;
import com.eam.demoAPI.persistence.dao.HistorialDocumentoDAO;
import com.eam.demoAPI.persistence.dao.UsuarioDAO;
import com.eam.demoAPI.persistence.dao.FlujoDAO;
import com.eam.demoAPI.persistence.dao.TareaDAO;
import com.eam.demoAPI.persistence.entity.Documento;
import com.eam.demoAPI.persistence.repository.DocumentoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private static final String ACCION_ETAPA_APROBADA  = "ETAPA_APROBADA";
    private static final String ACCION_ETAPA_RECHAZADA = "ETAPA_RECHAZADA";
    private static final String ACCION_FLUJO_COMPLETO  = "FLUJO_COMPLETO";
    private static final String ACCION_CAMBIO_ESTADO   = "CAMBIO_ESTADO";

    private final TareaDAO tareaDAO;
    private final DocumentoDAO documentoDAO;
    private final HistorialDocumentoDAO historialDAO;      // NUEVO
    private final FlujoDAO flujoDAO;
    private final DocumentoRepository documentoRepository;
    private final EmailService emailService;
    private final UsuarioDAO usuarioDAO;

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

        Documento doc = documentoRepository.findById(tarea.getDocumentoId())
                .orElseThrow(() -> new NotFoundException("Documento no encontrado"));

        if (doc.getTipoDocumento() == null) {
            actualizarEstadoDocumento(tarea.getDocumentoId(), DOC_APROBADO);
            registrarHistorial(doc, DOC_APROBADO, ACCION_FLUJO_COMPLETO);
            return tarea;
        }

        Long flujoId = doc.getTipoDocumento().getId();
        flujoDAO.findEntityByTipoDocumentoId(flujoId).ifPresentOrElse(flujo -> {

            Integer ordenActual = tareaDAO.findEntityById(id)
                    .map(t -> t.getEtapaFlujo().getOrden())
                    .orElse(0);

            flujoDAO.getSiguienteEtapa(flujo.getId(), ordenActual).ifPresentOrElse(
                    siguienteEtapa -> {
                        log.info("Avanzando a etapa {} del flujo", siguienteEtapa.getOrden());
                        tareaDAO.crearDesdeEtapa(siguienteEtapa, doc);
                        actualizarEstadoDocumento(tarea.getDocumentoId(), DOC_EN_REVISION);
                        registrarHistorial(doc, DOC_EN_REVISION, ACCION_ETAPA_APROBADA);
                    },
                    () -> {
                        log.info("Flujo completado — documento {} aprobado", doc.getId());
                        actualizarEstadoDocumento(tarea.getDocumentoId(), DOC_APROBADO);
                        registrarHistorial(doc, DOC_APROBADO, ACCION_FLUJO_COMPLETO);
                        usuarioDAO.findById(doc.getUsuario().getId()).ifPresent(u ->
                                emailService.notificarAprobacion(u.getEmail(), doc.getNombre()));
                    }
            );
        }, () -> {
            actualizarEstadoDocumento(tarea.getDocumentoId(), DOC_APROBADO);
            registrarHistorial(doc, DOC_APROBADO, ACCION_FLUJO_COMPLETO);
        });

        return tarea;
    }

    @Override
    public TareaDTO rechazar(Long id, String observaciones) {
        log.info("Rechazando tarea ID: {}", id);
        TareaDTO tarea = tareaDAO.resolver(id, ESTADO_RECHAZADO, observaciones);

        documentoRepository.findById(tarea.getDocumentoId()).ifPresent(doc -> {
            actualizarEstadoDocumento(tarea.getDocumentoId(), DOC_RECHAZADO);
            registrarHistorial(doc, DOC_RECHAZADO, ACCION_ETAPA_RECHAZADA);
            usuarioDAO.findById(doc.getUsuario().getId()).ifPresent(u ->
                    emailService.notificarRechazo(u.getEmail(), doc.getNombre(), observaciones));
        });

        return tarea;
    }

    @Override
    public TareaDTO solicitarCorreccion(Long id, String observaciones) {
        log.info("Solicitando corrección tarea ID: {}", id);
        TareaDTO tarea = tareaDAO.resolver(id, ESTADO_CORRECCION, observaciones);

        documentoRepository.findById(tarea.getDocumentoId()).ifPresent(doc -> {
            actualizarEstadoDocumento(tarea.getDocumentoId(), DOC_CREADO);
            registrarHistorial(doc, DOC_CREADO, ACCION_CAMBIO_ESTADO);
        });

        return tarea;
    }



    private void actualizarEstadoDocumento(Long documentoId, String nuevoEstado) {
        documentoDAO.findById(documentoId).ifPresent(doc -> {
            doc.setEstado(nuevoEstado);
            doc.setUpdatedAt(LocalDateTime.now());
            documentoDAO.update(documentoId, doc);
        });
    }

    private void registrarHistorial(Documento doc, String estadoNombre, String accionNombre) {
        try {
            Long usuarioId = getUsuarioActualId();
            HistorialDocumentoDTO historial = new HistorialDocumentoDTO();
            historial.setDocumentoId(doc.getId());
            historial.setUsuarioId(usuarioId);
            historial.setEstado(estadoNombre);
            historial.setAccion(accionNombre);
            historial.setFechaCambio(LocalDateTime.now());
            historialDAO.save(historial);
        } catch (Exception e) {
            log.warn("No se pudo registrar historial para documento {}: {}", doc.getId(), e.getMessage());
        }
    }

    private Long getUsuarioActualId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return usuarioDAO.findByEmail(auth.getName())
                .map(u -> u.getId())
                .orElse(null);
    }
}