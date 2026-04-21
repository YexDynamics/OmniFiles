package com.eam.demoAPI.persistence.dao;

import com.eam.demoAPI.business.dto.TareaDTO;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.entity.Tarea;
import com.eam.demoAPI.persistence.entity.enums.EstadoTarea;
import com.eam.demoAPI.persistence.mapper.TareaMapper;
import com.eam.demoAPI.persistence.repository.TareaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TareaDAO {

    private final TareaRepository tareaRepository;
    private final TareaMapper tareaMapper;

    public TareaDTO save(TareaDTO dto) {
        Tarea entity = tareaMapper.toEntity(dto);
        return tareaMapper.toDTO(tareaRepository.save(entity));
    }

    public Optional<TareaDTO> findById(Long id) {
        return tareaRepository.findById(id).map(tareaMapper::toDTO);
    }

    public List<TareaDTO> findByDocumentoId(Long documentoId) {
        return tareaMapper.toDTOList(tareaRepository.findByDocumentoId(documentoId));
    }

    public List<TareaDTO> findPendientesByUsuario(Long usuarioId) {
        return tareaMapper.toDTOList(
                tareaRepository.findByUsuarioAsignadoIdAndEstado(usuarioId, EstadoTarea.PENDIENTE)
        );
    }

    public Optional<Tarea> findEntityById(Long id) {
        return tareaRepository.findById(id);
    }

    public TareaDTO saveEntity(Tarea tarea) {
        return tareaMapper.toDTO(tareaRepository.save(tarea));
    }
}
