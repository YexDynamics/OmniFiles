package com.eam.demoAPI.persistence.dao;

import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.entity.Documento;
import com.eam.demoAPI.persistence.entity.EstadoDocumento;
import com.eam.demoAPI.persistence.mapper.DocumentoMapper;
import com.eam.demoAPI.persistence.repository.DocumentoRepository;
import com.eam.demoAPI.persistence.repository.EstadoDocumentoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DocumentoDAO {

    private final DocumentoRepository documentoRepository;
    private final DocumentoMapper documentoMapper;
    private final EstadoDocumentoRepository estadoDocumentoRepository;

    // ─── helpers ─────────────────────────────────────────────────────────────

    private EstadoDocumento resolverEstado(String nombre) {
        if (nombre == null) return null;
        return estadoDocumentoRepository.findByNombre(nombre)
                .orElseThrow(() -> new NotFoundException("Estado de documento no encontrado: " + nombre));
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────

    public DocumentoDTO save(DocumentoDTO dto) {
        Documento entity = documentoMapper.toEntity(dto);
        entity.setEstado(resolverEstado(dto.getEstado()));
        return documentoMapper.toDTO(documentoRepository.save(entity));
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    public Optional<DocumentoDTO> findById(Long id) {
        return documentoRepository.findById(id).map(documentoMapper::toDTO);
    }

    public List<DocumentoDTO> findAll() {
        return documentoMapper.toDTOList(
                documentoRepository.findAll().stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getEliminado()))
                        .toList()
        );
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    public Optional<DocumentoDTO> update(Long id, DocumentoDTO dto) {
        return documentoRepository.findById(id).map(entity -> {
            documentoMapper.updateEntityFromDTO(dto, entity);
            // Solo actualizamos estado si viene en el DTO
            if (dto.getEstado() != null) {
                entity.setEstado(resolverEstado(dto.getEstado()));
            }
            return documentoMapper.toDTO(documentoRepository.save(entity));
        });
    }

    // ─── FILTROS ──────────────────────────────────────────────────────────────

    public List<DocumentoDTO> findByEstado(String estadoNombre) {
        EstadoDocumento estado = resolverEstado(estadoNombre);
        return documentoMapper.toDTOList(
                documentoRepository.findByEstado(estado).stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getEliminado()))
                        .toList()
        );
    }

    public List<DocumentoDTO> findByUsuario(Long usuarioId) {
        return documentoMapper.toDTOList(
                documentoRepository.findByUsuarioId(usuarioId).stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getEliminado()))
                        .toList()
        );
    }

    public List<DocumentoDTO> findByEstadoAndUsuario(String estadoNombre, Long usuarioId) {
        EstadoDocumento estado = resolverEstado(estadoNombre);
        return documentoMapper.toDTOList(
                documentoRepository.findByEstadoAndUsuarioId(estado, usuarioId).stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getEliminado()))
                        .toList()
        );
    }

    // ─── PAPELERA ────────────────────────────────────────────────────────────

    public List<DocumentoDTO> findEliminados() {
        return documentoMapper.toDTOList(documentoRepository.findByEliminadoTrue());
    }

    // ─── ARCHIVO ─────────────────────────────────────────────────────────────

    public byte[] getFile(Long id) {
        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        if (Boolean.TRUE.equals(doc.getEliminado()))
            throw new RuntimeException("El documento está en papelera");

        if (doc.getRutaArchivo() == null || doc.getRutaArchivo().isEmpty())
            throw new RuntimeException("El documento no tiene ruta de archivo");

        Path path = Path.of(doc.getRutaArchivo());
        if (!Files.exists(path))
            throw new RuntimeException("El archivo físico no existe en la ruta");

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo");
        }
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    public boolean delete(Long id) {
        if (documentoRepository.existsById(id)) {
            documentoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
