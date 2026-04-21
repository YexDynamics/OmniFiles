package com.eam.demoAPI.business.service.impl;

import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.business.dto.HistorialDocumentoDTO;
import com.eam.demoAPI.business.dto.UsuarioDTO;
import com.eam.demoAPI.business.service.DocumentoService;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.dao.DocumentoDAO;
import com.eam.demoAPI.persistence.dao.HistorialDocumentoDAO;
import com.eam.demoAPI.persistence.dao.UsuarioDAO;
import com.eam.demoAPI.persistence.entity.enums.EstadoDocumento;
import com.eam.demoAPI.persistence.entity.enums.TipoAccion;

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

    private final DocumentoDAO documentoDAO;
    private final HistorialDocumentoDAO historialDAO;
    private final UsuarioDAO usuarioDAO;

    @Override
    public DocumentoDTO createDocumento(DocumentoDTO dto) {
        log.info("Creando documento: {}", dto.getNombre());
        validateDocumento(dto);

        dto.setEstado(EstadoDocumento.CREADO);
        dto.setEliminado(false);
        dto.setCreatedAt(LocalDateTime.now());

        DocumentoDTO result = documentoDAO.save(dto);
        registrarAccion(result, TipoAccion.CREACION);
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
        EstadoDocumento estadoEnum = null;
        if (estado != null) {
            try {
                estadoEnum = EstadoDocumento.valueOf(estado.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Estado inválido: " + estado);
            }
        }

        // Obtenemos la lista base según combinación de filtros principales
        List<DocumentoDTO> resultado;

        if (estadoEnum != null && usuarioId != null) {
            resultado = documentoDAO.findByEstadoAndUsuario(estadoEnum, usuarioId);
        } else if (estadoEnum != null) {
            resultado = documentoDAO.findByEstado(estadoEnum);
        } else if (usuarioId != null) {
            resultado = documentoDAO.findByUsuario(usuarioId);
        } else {
            resultado = documentoDAO.findAll();
        }

        // Aplicamos filtros adicionales en memoria
        Stream<DocumentoDTO> stream = resultado.stream()
                .filter(doc -> !Boolean.TRUE.equals(doc.getEliminado()));

        if (tipoDocumentoId != null) {
            stream = stream.filter(doc -> tipoDocumentoId.equals(doc.getTipoDocumentoId()));
        }

        if (fechaDesde != null) {
            stream = stream.filter(doc -> doc.getCreatedAt() != null
                    && !doc.getCreatedAt().isBefore(fechaDesde));
        }

        if (fechaHasta != null) {
            stream = stream.filter(doc -> doc.getCreatedAt() != null
                    && !doc.getCreatedAt().isAfter(fechaHasta));
        }

        return stream.toList();
    }

    @Override
    public DocumentoDTO updateDocumento(Long id, DocumentoDTO dto) {
        log.info("Actualizando documento ID: {}", id);
        DocumentoDTO existente = getDocumentoById(id);

        if (Boolean.TRUE.equals(existente.getEliminado())) {
            throw new IllegalArgumentException("No se puede modificar un documento en papelera");
        }

        if (dto.getUsuarioId() != null) {
            validateUsuario(dto.getUsuarioId());
        }

        dto.setUpdatedAt(LocalDateTime.now());

        DocumentoDTO actualizado = documentoDAO.update(id, dto)
                .orElseThrow(() -> new RuntimeException("Error al actualizar"));

        if (dto.getEstado() != null && !dto.getEstado().equals(existente.getEstado())) {
            registrarAccion(actualizado, TipoAccion.CAMBIO_ESTADO);
        } else {
            registrarAccion(actualizado, TipoAccion.ACTUALIZACION);
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
        registrarAccion(doc, TipoAccion.ELIMINACION);
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
        registrarAccion(doc, TipoAccion.RESTAURACION);
    }

    @Override
    public void deletePermanent(Long id) {
        DocumentoDTO doc = getDocumentoById(id);
        boolean deleted = documentoDAO.delete(id);
        if (!deleted) {
            throw new NotFoundException("Documento no encontrado con ID: " + id);
        }
        registrarAccion(doc, TipoAccion.ELIMINACION);
    }

    @Override
    public byte[] downloadDocumento(Long id) {
        DocumentoDTO doc = getDocumentoById(id);
        if (Boolean.TRUE.equals(doc.getEliminado())) {
            throw new IllegalArgumentException("No se puede descargar un documento en papelera");
        }
        registrarAccion(doc, TipoAccion.DESCARGA);
        return documentoDAO.getFile(doc.getId());
    }

    @Override
    public List<HistorialDocumentoDTO> getHistorial(Long documentoId) {
        return historialDAO.findByDocumentoId(documentoId);
    }

    // ─── privados ────────────────────────────────────────────────────────────

    private void validateDocumento(DocumentoDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre obligatorio");
        }
        if (dto.getUsuarioId() == null) {
            throw new IllegalArgumentException("Usuario obligatorio");
        }
        validateUsuario(dto.getUsuarioId());
    }

    private void validateUsuario(Long usuarioId) {
        boolean exists = usuarioDAO.findById(usuarioId).isPresent();
        if (!exists) {
            throw new NotFoundException("El usuario con ID " + usuarioId + " no existe");
        }
    }

    private Long getUsuarioActualId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }
        String correo = auth.getName();
        UsuarioDTO usuario = usuarioDAO.findByEmail(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuario.getId();
    }

    private void registrarAccion(DocumentoDTO doc, TipoAccion accion) {
        HistorialDocumentoDTO historial = new HistorialDocumentoDTO();
        historial.setDocumentoId(doc.getId());
        historial.setUsuarioId(getUsuarioActualId());
        historial.setEstado(doc.getEstado());
        historial.setAccion(accion);
        historial.setFechaCambio(LocalDateTime.now());
        historialDAO.save(historial);
    }
}
