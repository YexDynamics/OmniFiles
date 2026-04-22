package com.eam.demoAPI.persistence.mapper;

import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.persistence.entity.Documento;
import com.eam.demoAPI.persistence.entity.TipoDocumento;
import com.eam.demoAPI.persistence.entity.Usuario;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface DocumentoMapper {

    // ENTITY -> DTO: estado.nombre → estado (String)
    @Mapping(target = "usuarioId",        source = "usuario.id")
    @Mapping(target = "tipoDocumentoId",  source = "tipoDocumento.id")
    @Mapping(target = "createdAt",        source = "fechaCreacion")
    @Mapping(target = "updatedAt",        source = "fechaActualizacion")
    @Mapping(target = "estado",           source = "estado.nombre")
    DocumentoDTO toDTO(Documento entity);

    List<DocumentoDTO> toDTOList(List<Documento> entities);

    // DTO -> ENTITY: estado lo setea el DAO (lookup por nombre)
    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "estado",           ignore = true)
    @Mapping(target = "rutaArchivo",      ignore = true)
    @Mapping(target = "fechaCreacion",    ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "usuario",          source = "usuarioId",        qualifiedByName = "idToUsuario")
    @Mapping(target = "tipoDocumento",    source = "tipoDocumentoId",  qualifiedByName = "idToTipoDocumento")
    Documento toEntity(DocumentoDTO dto);

    // UPDATE: ignora relaciones y fechas
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "estado",           ignore = true)
    @Mapping(target = "usuario",          ignore = true)
    @Mapping(target = "tipoDocumento",    ignore = true)
    @Mapping(target = "rutaArchivo",      ignore = true)
    @Mapping(target = "fechaCreacion",    ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    void updateEntityFromDTO(DocumentoDTO dto, @MappingTarget Documento entity);

    @Named("idToUsuario")
    default Usuario idToUsuario(Long id) {
        if (id == null) return null;
        Usuario u = new Usuario();
        u.setId(id);
        return u;
    }

    @Named("idToTipoDocumento")
    default TipoDocumento idToTipoDocumento(Long id) {
        if (id == null) return null;
        TipoDocumento t = new TipoDocumento();
        t.setId(id);
        return t;
    }
}
