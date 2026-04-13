package com.eam.demoAPI.business.service.impl;

import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.business.dto.HistorialDocumentoDTO;
import com.eam.demoAPI.business.service.DocumentoService;
import com.eam.demoAPI.persistence.dao.DocumentoDAO;
import com.eam.demoAPI.persistence.dao.HistorialDocumentoDAO;
import com.eam.demoAPI.persistence.dao.UsuarioDAO;
import com.eam.demoAPI.persistence.entity.enums.EstadoDocumento;

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
public class DocumentoServiceImpl implements DocumentoService {

    private final DocumentoDAO documentoDAO;
    private final HistorialDocumentoDAO historialDAO;
    private final UsuarioDAO usuarioDAO;

    @Override
    public DocumentoDTO createDocumento(DocumentoDTO dto) {

        log.info("Creando documento: {}", dto.getNombre());

        validateDocumento(dto);

        dto.setEstado(EstadoDocumento.CREADO);
        dto.setEliminado(false); // ✅ aseguramos que nazca activo
        dto.setCreatedAt(LocalDateTime.now());

        DocumentoDTO result = documentoDAO.save(dto);

        saveHistorial(result);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoDTO getDocumentoById(Long id) {
        return documentoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoDTO> getDocumentosFiltrados(String estado, Long usuarioId) {

        EstadoDocumento estadoEnum = null;

        if (estado != null) {
            estadoEnum = EstadoDocumento.valueOf(estado);
        }

        if (estadoEnum != null && usuarioId != null) {
            return documentoDAO.findByEstadoAndUsuario(estadoEnum, usuarioId)
                    .stream().filter(doc -> !doc.getEliminado()).toList(); // ✅ excluye eliminados
        }

        if (estadoEnum != null) {
            return documentoDAO.findByEstado(estadoEnum)
                    .stream().filter(doc -> !doc.getEliminado()).toList();
        }

        if (usuarioId != null) {
            return documentoDAO.findByUsuario(usuarioId)
                    .stream().filter(doc -> !doc.getEliminado()).toList();
        }

        return documentoDAO.findAll()
                .stream().filter(doc -> !doc.getEliminado()).toList();
    }

    @Override
    public DocumentoDTO updateDocumento(Long id, DocumentoDTO dto) {

        log.info("Actualizando documento ID: {}", id);

        DocumentoDTO existente = getDocumentoById(id);

        if (Boolean.TRUE.equals(existente.getEliminado())) {
            throw new RuntimeException("No se puede modificar un documento en papelera"); // ✅ regla
        }

        if (dto.getUsuarioId() != null) {
            validateUsuario(dto.getUsuarioId());
        }

        dto.setUpdatedAt(LocalDateTime.now());

        DocumentoDTO actualizado = documentoDAO.update(id, dto)
                .orElseThrow(() -> new RuntimeException("Error al actualizar"));

        if (dto.getEstado() != null &&
                !dto.getEstado().equals(existente.getEstado())) {

            saveHistorial(actualizado);
        }

        return actualizado;
    }

    @Override
    public void softDeleteDocumento(Long id) {

        DocumentoDTO doc = getDocumentoById(id);

        if (Boolean.TRUE.equals(doc.getEliminado())) {
            return; // ya está eliminado
        }

        doc.setEliminado(true);
        doc.setUpdatedAt(LocalDateTime.now());

        documentoDAO.update(id, doc);
    }

    @Override
    public List<DocumentoDTO> getDocumentosEliminados() {
        return documentoDAO.findEliminados();
    }

    @Override
    public void restoreDocumento(Long id) {

        DocumentoDTO doc = getDocumentoById(id);

        if (!Boolean.TRUE.equals(doc.getEliminado())) {
            return; // no está eliminado
        }

        doc.setEliminado(false);
        doc.setUpdatedAt(LocalDateTime.now());

        documentoDAO.update(id, doc);
    }

    @Override
    public void deletePermanent(Long id) {

        boolean deleted = documentoDAO.delete(id);

        if (!deleted) {
            throw new RuntimeException("Documento no encontrado");
        }
    }

    @Override
    public byte[] downloadDocumento(Long id) {

        DocumentoDTO doc = getDocumentoById(id);

        if (Boolean.TRUE.equals(doc.getEliminado())) {
            throw new RuntimeException("No se puede descargar un documento en papelera");
        }

        return documentoDAO.getFile(doc.getId());
    }

    @Override
    public List<HistorialDocumentoDTO> getHistorial(Long documentoId) {
        return historialDAO.findByDocumentoId(documentoId);
    }

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
            throw new IllegalArgumentException("El usuario no existe");
        }
    }

    private void saveHistorial(DocumentoDTO doc) {

        HistorialDocumentoDTO historial = new HistorialDocumentoDTO();
        historial.setDocumentoId(doc.getId());
        historial.setUsuarioId(doc.getUsuarioId());
        historial.setEstado(doc.getEstado());
        historial.setFechaCambio(LocalDateTime.now());

        historialDAO.save(historial);
    }
}