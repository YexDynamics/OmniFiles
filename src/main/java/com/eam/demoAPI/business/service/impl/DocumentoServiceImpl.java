package com.eam.demoAPI.business.service.impl;

import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.business.dto.HistorialDocumentoDTO;
import com.eam.demoAPI.business.dto.UsuarioDTO;
import com.eam.demoAPI.business.service.DocumentoService;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.dao.DocumentoDAO;
import com.eam.demoAPI.persistence.dao.HistorialDocumentoDAO;
import com.eam.demoAPI.business.service.EmailService;
import com.eam.demoAPI.persistence.dao.FlujoDAO;
import com.eam.demoAPI.persistence.dao.TareaDAO;
import com.eam.demoAPI.persistence.dao.UsuarioDAO;
import com.eam.demoAPI.persistence.entity.Documento;
import com.eam.demoAPI.persistence.entity.EtapaFlujo;
import com.eam.demoAPI.persistence.repository.DocumentoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DocumentoServiceImpl implements DocumentoService {

    // ─── Constantes de estados (leídos desde BD, referenciados como String) ──
    private static final String ESTADO_CREADO    = "CREADO";

    // ─── Constantes de acciones ───────────────────────────────────────────────
    private static final String ACCION_CREACION      = "CREACION";
    private static final String ACCION_ACTUALIZACION = "ACTUALIZACION";
    private static final String ACCION_CAMBIO_ESTADO = "CAMBIO_ESTADO";
    private static final String ACCION_ELIMINACION   = "ELIMINACION";
    private static final String ACCION_RESTAURACION  = "RESTAURACION";
    private static final String ACCION_DESCARGA      = "DESCARGA";

    private final DocumentoDAO documentoDAO;
    private final HistorialDocumentoDAO historialDAO;
    private final UsuarioDAO usuarioDAO;
    private final EmailService emailService;         // NUEVO
    private final FlujoDAO flujoDAO;               // NUEVO
    private final TareaDAO tareaDAO;               // NUEVO
    private final DocumentoRepository documentoRepository; // NUEVO

    @Override
    public DocumentoDTO createDocumento(DocumentoDTO dto) {
        log.info("Creando documento: {}", dto.getNombre());
        validateDocumento(dto);

        dto.setEstado(ESTADO_CREADO);
        dto.setEliminado(false);
        dto.setCreatedAt(LocalDateTime.now());

        DocumentoDTO result = documentoDAO.save(dto);
        registrarAccion(result, ACCION_CREACION);

        // Notificar al creador por email
        usuarioDAO.findById(result.getUsuarioId()).ifPresent(u ->
                emailService.notificarCreacion(u.getEmail(), result.getNombre()));

        // NUEVO: si la plantilla tiene flujo configurado, generar la primera tarea
        if (dto.getTipoDocumentoId() != null) {
            flujoDAO.findEntityByTipoDocumentoId(dto.getTipoDocumentoId())
                    .ifPresent(flujo -> {
                        if (flujo.getEtapas() != null && !flujo.getEtapas().isEmpty()) {
                            EtapaFlujo primeraEtapa = flujo.getEtapas().get(0);
                            Documento docEntity = documentoRepository.findById(result.getId())
                                    .orElseThrow();
                            tareaDAO.crearDesdeEtapa(primeraEtapa, docEntity);
                            // Cambiar estado a EN_REVISION porque ya entró al flujo
                            result.setEstado("EN_REVISION");
                            documentoDAO.update(result.getId(), result);
                            log.info("Primera tarea creada para documento {} en etapa '{}'",
                                    result.getId(), primeraEtapa.getNombre());
                        }
                    });
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoDTO getDocumentoById(Long id) {
        return documentoDAO.findById(id)
                .orElseThrow(() -> new NotFoundException("Documento no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoDTO> getDocumentosFiltrados(String estado, Long usuarioId,
                                                      Long tipoDocumentoId,
                                                      LocalDateTime fechaDesde,
                                                      LocalDateTime fechaHasta) {
        List<DocumentoDTO> resultado;

        if (estado != null && usuarioId != null) {
            resultado = documentoDAO.findByEstadoAndUsuario(estado.toUpperCase(), usuarioId);
        } else if (estado != null) {
            resultado = documentoDAO.findByEstado(estado.toUpperCase());
        } else if (usuarioId != null) {
            resultado = documentoDAO.findByUsuario(usuarioId);
        } else {
            resultado = documentoDAO.findAll();
        }

        Stream<DocumentoDTO> stream = resultado.stream()
                .filter(doc -> !Boolean.TRUE.equals(doc.getEliminado()));

        if (tipoDocumentoId != null)
            stream = stream.filter(doc -> tipoDocumentoId.equals(doc.getTipoDocumentoId()));

        if (fechaDesde != null)
            stream = stream.filter(doc -> doc.getCreatedAt() != null
                    && !doc.getCreatedAt().isBefore(fechaDesde));

        if (fechaHasta != null)
            stream = stream.filter(doc -> doc.getCreatedAt() != null
                    && !doc.getCreatedAt().isAfter(fechaHasta));

        return stream.toList();
    }

    @Override
    public DocumentoDTO updateDocumento(Long id, DocumentoDTO dto) {
        log.info("Actualizando documento ID: {}", id);
        DocumentoDTO existente = getDocumentoById(id);

        if (Boolean.TRUE.equals(existente.getEliminado()))
            throw new IllegalArgumentException("No se puede modificar un documento en papelera");

        if (dto.getUsuarioId() != null)
            validateUsuario(dto.getUsuarioId());

        dto.setUpdatedAt(LocalDateTime.now());

        DocumentoDTO actualizado = documentoDAO.update(id, dto)
                .orElseThrow(() -> new RuntimeException("Error al actualizar"));

        if (dto.getEstado() != null && !dto.getEstado().equals(existente.getEstado())) {
            registrarAccion(actualizado, ACCION_CAMBIO_ESTADO);
        } else {
            registrarAccion(actualizado, ACCION_ACTUALIZACION);
        }

        return actualizado;
    }

    @Override
    public void softDeleteDocumento(Long id) {
        DocumentoDTO doc = getDocumentoById(id);
        if (Boolean.TRUE.equals(doc.getEliminado())) return;

        doc.setEliminado(true);
        doc.setUpdatedAt(LocalDateTime.now());
        documentoDAO.update(id, doc);
        registrarAccion(doc, ACCION_ELIMINACION);
    }

    @Override
    public List<DocumentoDTO> getDocumentosEliminados() {
        return documentoDAO.findEliminados();
    }

    @Override
    public void restoreDocumento(Long id) {
        DocumentoDTO doc = getDocumentoById(id);
        if (!Boolean.TRUE.equals(doc.getEliminado())) return;

        doc.setEliminado(false);
        doc.setUpdatedAt(LocalDateTime.now());
        documentoDAO.update(id, doc);
        registrarAccion(doc, ACCION_RESTAURACION);
    }

    @Override
    public void deletePermanent(Long id) {
        DocumentoDTO doc = getDocumentoById(id);
        boolean deleted = documentoDAO.delete(id);
        if (!deleted)
            throw new NotFoundException("Documento no encontrado con ID: " + id);
        registrarAccion(doc, ACCION_ELIMINACION);
    }

    @Override
    public byte[] downloadDocumento(Long id) {
        DocumentoDTO doc = getDocumentoById(id);
        if (Boolean.TRUE.equals(doc.getEliminado()))
            throw new IllegalArgumentException("No se puede descargar un documento en papelera");

        // Actualizar fecha de último acceso para que el limpiador no lo borre
        doc.setUpdatedAt(LocalDateTime.now());
        documentoDAO.update(id, doc);

        registrarAccion(doc, ACCION_DESCARGA);
        return documentoDAO.getFile(doc.getId());
    }

    @Override
    public List<HistorialDocumentoDTO> getHistorial(Long documentoId) {
        return historialDAO.findByDocumentoId(documentoId);
    }

    // ─── privados ─────────────────────────────────────────────────────────────

    private void validateDocumento(DocumentoDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty())
            throw new IllegalArgumentException("Nombre obligatorio");
        if (dto.getUsuarioId() == null)
            throw new IllegalArgumentException("Usuario obligatorio");
        validateUsuario(dto.getUsuarioId());
    }

    private void validateUsuario(Long usuarioId) {
        if (usuarioDAO.findById(usuarioId).isEmpty())
            throw new NotFoundException("El usuario con ID " + usuarioId + " no existe");
    }

    private Long getUsuarioActualId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            throw new RuntimeException("Usuario no autenticado");

        UsuarioDTO usuario = usuarioDAO.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuario.getId();
    }

    private void registrarAccion(DocumentoDTO doc, String accionNombre) {
        HistorialDocumentoDTO historial = new HistorialDocumentoDTO();
        historial.setDocumentoId(doc.getId());
        historial.setUsuarioId(getUsuarioActualId());
        historial.setEstado(doc.getEstado());
        historial.setAccion(accionNombre);
        historial.setFechaCambio(LocalDateTime.now());
        historialDAO.save(historial);
    }
}
