package com.eam.demoAPI.persistence.dao;

import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.persistence.entity.Documento;
import com.eam.demoAPI.persistence.entity.enums.EstadoDocumento;
import com.eam.demoAPI.persistence.mapper.DocumentoMapper;
import com.eam.demoAPI.persistence.repository.DocumentoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

@Repository
@RequiredArgsConstructor
public class DocumentoDAO {

    private final DocumentoRepository documentoRepository;
    private final DocumentoMapper documentoMapper;

    // CREATE
    public DocumentoDTO save(DocumentoDTO dto) {
        Documento entity = documentoMapper.toEntity(dto);
        Documento saved = documentoRepository.save(entity);
        return documentoMapper.toDTO(saved);
    }

    // READ by ID
    public Optional<DocumentoDTO> findById(Long id) {
        return documentoRepository.findById(id)
                .map(documentoMapper::toDTO);
    }

    // READ ALL → solo activos
    public List<DocumentoDTO> findAll() {
        return documentoMapper.toDTOList(
                documentoRepository.findAll()
                        .stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getEliminado()))
                        .toList()
        );
    }

    // UPDATE
    public Optional<DocumentoDTO> update(Long id, DocumentoDTO dto) {
        return documentoRepository.findById(id)
                .map(entity -> {
                    documentoMapper.updateEntityFromDTO(dto, entity);
                    Documento updated = documentoRepository.save(entity);
                    return documentoMapper.toDTO(updated);
                });
    }

    // FILTROS (también deben respetar eliminado)

    public List<DocumentoDTO> findByEstado(EstadoDocumento estado) {
        return documentoMapper.toDTOList(
                documentoRepository.findByEstado(estado)
                        .stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getEliminado()))
                        .toList()
        );
    }

    public List<DocumentoDTO> findByUsuario(Long usuarioId) {
        return documentoMapper.toDTOList(
                documentoRepository.findByUsuarioId(usuarioId)
                        .stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getEliminado()))
                        .toList()
        );
    }

    public List<DocumentoDTO> findByEstadoAndUsuario(EstadoDocumento estado, Long usuarioId) {
        return documentoMapper.toDTOList(
                documentoRepository.findByEstadoAndUsuarioId(estado, usuarioId)
                        .stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getEliminado()))
                        .toList()
        );
    }

    // PAPELERA → solo eliminados
    public List<DocumentoDTO> findEliminados() {
        return documentoMapper.toDTOList(
                documentoRepository.findByEliminadoTrue()
        );
    }

    // ARCHIVO
    public byte[] getFile(Long id) {

        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        if (Boolean.TRUE.equals(doc.getEliminado())) {
            throw new RuntimeException("El documento está en papelera");
        }

        if (doc.getRutaArchivo() == null || doc.getRutaArchivo().isEmpty()) {
            throw new RuntimeException("El documento no tiene ruta de archivo");
        }

        Path path = Path.of(doc.getRutaArchivo());

        if (!Files.exists(path)) {
            throw new RuntimeException("El archivo físico no existe en la ruta");
        }

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo");
        }
    }

    // DELETE real
    public boolean delete(Long id) {
        if (documentoRepository.existsById(id)) {
            documentoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}