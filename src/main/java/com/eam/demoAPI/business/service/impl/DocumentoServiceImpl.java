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

        if (estado != null && usuarioId != null) {
            return documentoDAO.findByEstadoAndUsuario(estado, usuarioId);
        }

        if (estado != null) {
            return documentoDAO.findByEstado(estado);
        }

        if (usuarioId != null) {
            return documentoDAO.findByUsuario(usuarioId);
        }

        return documentoDAO.findAll();
    }

    @Override
    public DocumentoDTO updateDocumento(Long id, DocumentoDTO dto) {

        log.info("Actualizando documento ID: {}", id);

        DocumentoDTO existente = getDocumentoById(id);

        // validar usuario si cambia
        if (dto.getUsuarioId() != null) {
            validateUsuario(dto.getUsuarioId());
        }

        DocumentoDTO actualizado = documentoDAO.update(id, dto)
                .orElseThrow(() -> new RuntimeException("Error al actualizar"));

        // historial SOLO si cambia estado
        if (dto.getEstado() != null &&
                !dto.getEstado().equals(existente.getEstado())) {

            saveHistorial(actualizado);
        }

        return actualizado;
    }

    @Override
    public void softDeleteDocumento(Long id) {

        DocumentoDTO doc = getDocumentoById(id);

        doc.setEliminado(true);

        documentoDAO.update(id, doc);
    }

    @Override
    public List<DocumentoDTO> getDocumentosEliminados() {
        return documentoDAO.findEliminados();
    }

    @Override
    public void restoreDocumento(Long id) {

        DocumentoDTO doc = getDocumentoById(id);

        doc.setEliminado(false);

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

        return documentoDAO.getFile(doc.getId());
    }

    @Override
    public List<HistorialDocumentoDTO> getHistorial(Long documentoId) {
        return historialDAO.findByDocumentoId(documentoId);
    }

    /**
     * =========================
     * HELPERS
     * =========================
     */

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