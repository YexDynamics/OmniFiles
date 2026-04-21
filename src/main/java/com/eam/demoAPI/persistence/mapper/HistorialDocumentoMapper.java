package com.eam.demoAPI.persistence.mapper;

import com.eam.demoAPI.business.dto.HistorialDocumentoDTO;
import com.eam.demoAPI.persistence.entity.Documento;
import com.eam.demoAPI.persistence.entity.HistorialDocumento;
import com.eam.demoAPI.persistence.entity.Usuario;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface HistorialDocumentoMapper {

    // ENTITY -> DTO: mapea nombre de estado y acción
    @Mapping(target = "documentoId", source = "documento.id")
    @Mapping(target = "usuarioId",   source = "usuario.id")
    @Mapping(target = "estado",      source = "estado.nombre")
    @Mapping(target = "accion",      source = "accion.nombre")
    HistorialDocumentoDTO toDTO(HistorialDocumento entity);

    List<HistorialDocumentoDTO> toDTOList(List<HistorialDocumento> entities);

    // DTO -> ENTITY: estado y accion los setea el DAO (lookup por nombre)
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "estado",      ignore = true)
    @Mapping(target = "accion",      ignore = true)
    @Mapping(target = "fechaCambio", ignore = true)
    @Mapping(target = "documento",   source = "documentoId", qualifiedByName = "idToDocumento")
    @Mapping(target = "usuario",     source = "usuarioId",   qualifiedByName = "idToUsuario")
    HistorialDocumento toEntity(HistorialDocumentoDTO dto);

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
