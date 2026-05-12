package com.eam.demoAPI.persistence.dao;

import com.eam.demoAPI.business.dto.TareaDTO;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.entity.*;
import com.eam.demoAPI.persistence.mapper.TareaMapper;
import com.eam.demoAPI.persistence.repository.*;
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
    private final EtapaFlujoRepository etapaFlujoRepository;  // NUEVO
    private final DocumentoRepository documentoRepository;     // NUEVO

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

    /**
     * NUEVO: Crea una tarea directamente desde una EtapaFlujo y un Documento.
     * Asigna al usuario específico de la etapa, o deja usuarioAsignado como el
     * primer usuario del rol si no hay usuario específico.
     */
    public TareaDTO crearDesdeEtapa(EtapaFlujo etapa, Documento documento) {
        Tarea tarea = new Tarea();
        tarea.setDocumento(documento);
        tarea.setEtapaFlujo(etapa);
        tarea.setEstado(resolverEstado("PENDIENTE"));
        tarea.setFechaAsignacion(LocalDateTime.now());

        // Asignar al usuario específico si está definido; si no, al del documento
        if (etapa.getUsuario() != null) {
            tarea.setUsuarioAsignado(etapa.getUsuario());
        } else {
            // Sin usuario específico: asignar al creador del documento como fallback
            // El admin luego puede reasignar manualmente si hace falta
            tarea.setUsuarioAsignado(documento.getUsuario());
        }

        return tareaMapper.toDTO(tareaRepository.save(tarea));
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
                tareaRepository.findByUsuarioAsignadoIdAndEstado(usuarioId, pendiente));
    }

    // ─── RESOLVER ─────────────────────────────────────────────────────────────

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
