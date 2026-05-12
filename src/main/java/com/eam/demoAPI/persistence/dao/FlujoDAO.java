package com.eam.demoAPI.persistence.dao;

import com.eam.demoAPI.business.dto.EtapaFlujoDTO;
import com.eam.demoAPI.business.dto.FlujoDTO;
import com.eam.demoAPI.exception.ConflictException;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.entity.EtapaFlujo;
import com.eam.demoAPI.persistence.entity.Flujo;
import com.eam.demoAPI.persistence.entity.Rol;
import com.eam.demoAPI.persistence.entity.TipoDocumento;
import com.eam.demoAPI.persistence.entity.Usuario;
import com.eam.demoAPI.persistence.mapper.EtapaFlujoMapper;
import com.eam.demoAPI.persistence.mapper.FlujoMapper;
import com.eam.demoAPI.persistence.repository.EtapaFlujoRepository;
import com.eam.demoAPI.persistence.repository.FlujoRepository;
import com.eam.demoAPI.persistence.repository.RolRepository;
import com.eam.demoAPI.persistence.repository.TipoDocumentoRepository;
import com.eam.demoAPI.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FlujoDAO {

    private final FlujoRepository flujoRepository;
    private final EtapaFlujoRepository etapaFlujoRepository;
    private final FlujoMapper flujoMapper;
    private final EtapaFlujoMapper etapaFlujoMapper;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

    // ─── Flujo ────────────────────────────────────────────────────────────────

    public FlujoDTO crearFlujo(FlujoDTO dto) {
        if (flujoRepository.findByTipoDocumentoId(dto.getTipoDocumentoId()).isPresent()) {
            throw new ConflictException("La plantilla ya tiene un flujo configurado");
        }
        TipoDocumento tipo = tipoDocumentoRepository.findById(dto.getTipoDocumentoId())
                .orElseThrow(() -> new NotFoundException("Tipo de documento no encontrado"));
        Flujo flujo = new Flujo();
        flujo.setNombre(dto.getNombre());
        flujo.setTipoDocumento(tipo);
        return flujoMapper.toDTO(flujoRepository.save(flujo));
    }

    public Optional<FlujoDTO> findByTipoDocumentoId(Long tipoDocumentoId) {
        return flujoRepository.findByTipoDocumentoId(tipoDocumentoId)
                .map(flujoMapper::toDTO);
    }

    public Optional<FlujoDTO> findById(Long id) {
        return flujoRepository.findById(id).map(flujoMapper::toDTO);
    }

    public List<FlujoDTO> findAll() {
        return flujoMapper.toDTOList(flujoRepository.findAll());
    }

    // ─── Etapas ───────────────────────────────────────────────────────────────

    public EtapaFlujoDTO agregarEtapa(Long flujoId, EtapaFlujoDTO dto) {
        Flujo flujo = flujoRepository.findById(flujoId)
                .orElseThrow(() -> new NotFoundException("Flujo no encontrado"));

        if (etapaFlujoRepository.findByFlujoIdAndOrden(flujoId, dto.getOrden()).isPresent()) {
            throw new ConflictException("Ya existe una etapa con orden " + dto.getOrden() + " en este flujo");
        }

        Rol rol = rolRepository.findById(dto.getRolId())
                .orElseThrow(() -> new NotFoundException("Rol no encontrado"));

        EtapaFlujo etapa = new EtapaFlujo();
        etapa.setFlujo(flujo);
        etapa.setOrden(dto.getOrden());
        etapa.setNombre(dto.getNombre());
        etapa.setRol(rol);

        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
            etapa.setUsuario(usuario);
        }

        // Marcar la plantilla como flujo configurado
        flujo.getTipoDocumento().setFlujoConfigurado(true);
        tipoDocumentoRepository.save(flujo.getTipoDocumento());

        return etapaFlujoMapper.toDTO(etapaFlujoRepository.save(etapa));
    }

    public List<EtapaFlujoDTO> getEtapasByFlujo(Long flujoId) {
        return etapaFlujoMapper.toDTOList(
                etapaFlujoRepository.findByFlujoIdOrderByOrdenAsc(flujoId));
    }

    public void eliminarEtapa(Long etapaId) {
        etapaFlujoRepository.deleteById(etapaId);
    }

    /**
     * Devuelve la siguiente etapa dado el flujoId y el orden actual.
     * Retorna empty si era la última etapa.
     */
    public Optional<EtapaFlujo> getSiguienteEtapa(Long flujoId, Integer ordenActual) {
        return etapaFlujoRepository.findByFlujoIdAndOrden(flujoId, ordenActual + 1);
    }

    public Optional<Flujo> findEntityByTipoDocumentoId(Long tipoDocumentoId) {
        return flujoRepository.findByTipoDocumentoId(tipoDocumentoId);
    }
}
