package com.eam.demoAPI.persistence.dao;

import com.eam.demoAPI.business.dto.TareaDTO;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.entity.EstadoTarea;
import com.eam.demoAPI.persistence.entity.Tarea;
import com.eam.demoAPI.persistence.mapper.TareaMapper;
import com.eam.demoAPI.persistence.repository.EstadoTareaRepository;
import com.eam.demoAPI.persistence.repository.TareaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TareaDAO {

    private final TareaRepository tareaRepository;
    private final TareaMapper tareaMapper;
    private final EstadoTareaRepository estadoTareaRepository;

    // ─── helper ───────────────────────────────────────────────────────────────

    private EstadoTarea resolverEstado(String nombre) {
        return estadoTareaRepository.findByNombre(nombre)
                .orElseThrow(() -> new NotFoundException("Estado de tarea no encontrado: " + nombre));
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    public TareaDTO save(TareaDTO dto) {
        Tarea entity = tareaMapper.toEntity(dto);
        entity.setEstado(resolverEstado(dto.getEstado()));
        entity.setFechaAsignacion(dto.getFechaAsignacion());
        return tareaMapper.toDTO(tareaRepository.save(entity));
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    public Optional<TareaDTO> findById(Long id) {
        return tareaRepository.findById(id).map(tareaMapper::toDTO);
    }

    public Optional<Tarea> findEntityById(Long id) {
        return tareaRepository.findById(id);
    }

    public List<TareaDTO> findByDocumentoId(Long documentoId) {
        return tareaMapper.toDTOList(tareaRepository.findByDocumentoId(documentoId));
    }

    public List<TareaDTO> findPendientesByUsuario(Long usuarioId) {
        EstadoTarea pendiente = resolverEstado("PENDIENTE");
        return tareaMapper.toDTOList(
                tareaRepository.findByUsuarioAsignadoIdAndEstado(usuarioId, pendiente)
        );
    }

    // ─── RESOLVER (aprobar / rechazar / corregir) ─────────────────────────────

    /**
     * Resuelve una tarea cambiando su estado. Solo actúa si está PENDIENTE.
     */
    public TareaDTO resolver(Long id, String nuevoEstadoNombre, String observaciones) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tarea no encontrada con ID: " + id));

        if (!"PENDIENTE".equals(tarea.getEstado().getNombre())) {
            throw new IllegalArgumentException("La tarea ya fue resuelta");
        }

        tarea.setEstado(resolverEstado(nuevoEstadoNombre));
        tarea.setObservaciones(observaciones);
        tarea.setFechaResolucion(LocalDateTime.now());

        return tareaMapper.toDTO(tareaRepository.save(tarea));
    }
}
