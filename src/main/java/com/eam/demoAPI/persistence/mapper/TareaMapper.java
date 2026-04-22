package com.eam.demoAPI.persistence.mapper;

import com.eam.demoAPI.business.dto.TareaDTO;
import com.eam.demoAPI.persistence.entity.Documento;
import com.eam.demoAPI.persistence.entity.Tarea;
import com.eam.demoAPI.persistence.entity.Usuario;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface TareaMapper {

    // ENTITY -> DTO: mapea nombre del estado
    @Mapping(target = "documentoId",       source = "documento.id")
    @Mapping(target = "usuarioAsignadoId", source = "usuarioAsignado.id")
    @Mapping(target = "estado",            source = "estado.nombre")
    TareaDTO toDTO(Tarea entity);

    List<TareaDTO> toDTOList(List<Tarea> entities);

    // DTO -> ENTITY: estado lo setea el DAO
    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "estado",           ignore = true)
    @Mapping(target = "fechaAsignacion",  ignore = true)
    @Mapping(target = "fechaResolucion",  ignore = true)
    @Mapping(target = "documento",        source = "documentoId",       qualifiedByName = "idToDocumento")
    @Mapping(target = "usuarioAsignado",  source = "usuarioAsignadoId", qualifiedByName = "idToUsuario")
    Tarea toEntity(TareaDTO dto);

    @Named("idToDocumento")
    default Documento idToDocumento(Long id) {
        if (id == null) return null;
        Documento d = new Documento();
        d.setId(id);
        return d;
    }

    @Named("idToUsuario")
    default Usuario idToUsuario(Long id) {
        if (id == null) return null;
        Usuario u = new Usuario();
        u.setId(id);
        return u;
    }
}
