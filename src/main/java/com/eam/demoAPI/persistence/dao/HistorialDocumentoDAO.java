package com.eam.demoAPI.persistence.dao;

import com.eam.demoAPI.business.dto.HistorialDocumentoDTO;
import com.eam.demoAPI.persistence.entity.HistorialDocumento;
import com.eam.demoAPI.persistence.mapper.HistorialDocumentoMapper;
import com.eam.demoAPI.persistence.repository.HistorialDocumentoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HistorialDocumentoDAO {

    private final HistorialDocumentoRepository repository;
    private final HistorialDocumentoMapper mapper;

    public HistorialDocumentoDTO save(HistorialDocumentoDTO dto) {
        HistorialDocumento entity = mapper.toEntity(dto);
        HistorialDocumento saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    public List<HistorialDocumentoDTO> findByDocumentoId(Long documentoId) {
        return mapper.toDTOList(repository.findByDocumentoId(documentoId));
    }

    public List<HistorialDocumentoDTO> findAll() {
        return mapper.toDTOList(repository.findAll());
    }
}