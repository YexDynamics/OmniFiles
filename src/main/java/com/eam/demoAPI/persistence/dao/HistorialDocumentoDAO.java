package com.eam.demoAPI.persistence.dao;

import com.eam.demoAPI.business.dto.HistorialDocumentoDTO;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.entity.EstadoDocumento;
import com.eam.demoAPI.persistence.entity.HistorialDocumento;
import com.eam.demoAPI.persistence.entity.TipoAccion;
import com.eam.demoAPI.persistence.mapper.HistorialDocumentoMapper;
import com.eam.demoAPI.persistence.repository.EstadoDocumentoRepository;
import com.eam.demoAPI.persistence.repository.HistorialDocumentoRepository;
import com.eam.demoAPI.persistence.repository.TipoAccionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HistorialDocumentoDAO {

    private final HistorialDocumentoRepository repository;
    private final HistorialDocumentoMapper mapper;
    private final EstadoDocumentoRepository estadoDocumentoRepository;
    private final TipoAccionRepository tipoAccionRepository;

    public HistorialDocumentoDTO save(HistorialDocumentoDTO dto) {
        HistorialDocumento entity = mapper.toEntity(dto);

        // Lookup estado por nombre
        if (dto.getEstado() != null) {
            EstadoDocumento estado = estadoDocumentoRepository.findByNombre(dto.getEstado())
                    .orElseThrow(() -> new NotFoundException("Estado no encontrado: " + dto.getEstado()));
            entity.setEstado(estado);
        }

        // Lookup accion por nombre
        if (dto.getAccion() != null) {
            TipoAccion accion = tipoAccionRepository.findByNombre(dto.getAccion())
                    .orElseThrow(() -> new NotFoundException("Tipo de acción no encontrado: " + dto.getAccion()));
            entity.setAccion(accion);
        }

        entity.setFechaCambio(dto.getFechaCambio());

        return mapper.toDTO(repository.save(entity));
    }

    public List<HistorialDocumentoDTO> findByDocumentoId(Long documentoId) {
        return mapper.toDTOList(repository.findByDocumentoId(documentoId));
    }

    public List<HistorialDocumentoDTO> findAll() {
        return mapper.toDTOList(repository.findAll());
    }
}
