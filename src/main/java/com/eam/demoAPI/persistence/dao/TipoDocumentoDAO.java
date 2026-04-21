package com.eam.demoAPI.persistence.dao;

import com.eam.demoAPI.business.dto.TipoDocumentoDTO;
import com.eam.demoAPI.exception.ConflictException;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.entity.TipoDocumento;
import com.eam.demoAPI.persistence.mapper.TipoDocumentoMapper;
import com.eam.demoAPI.persistence.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TipoDocumentoDAO {

    private final TipoDocumentoRepository repository;
    private final TipoDocumentoMapper mapper;

    public TipoDocumentoDTO save(TipoDocumentoDTO dto) {
        if (repository.existsByNombre(dto.getNombre())) {
            throw new ConflictException("Ya existe un tipo documental con ese nombre");
        }
        TipoDocumento entity = mapper.toEntity(dto);
        entity.setEstado(true);
        entity.setFlujoConfigurado(false);
        return mapper.toDTO(repository.save(entity));
    }

    public Optional<TipoDocumentoDTO> findById(Long id) {
        return repository.findById(id).map(mapper::toDTO);
    }

    public List<TipoDocumentoDTO> findAll() {
        return mapper.toDTOList(repository.findAll());
    }

    public List<TipoDocumentoDTO> findActivos() {
        return mapper.toDTOList(repository.findByEstadoTrue());
    }

    public Optional<TipoDocumentoDTO> update(Long id, TipoDocumentoDTO dto) {
        return repository.findById(id).map(entity -> {
            mapper.updateEntityFromDTO(dto, entity);
            return mapper.toDTO(repository.save(entity));
        });
    }

    public void cambiarEstado(Long id, boolean estado) {
        TipoDocumento entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo documental no encontrado"));
        entity.setEstado(estado);
        repository.save(entity);
    }

    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
