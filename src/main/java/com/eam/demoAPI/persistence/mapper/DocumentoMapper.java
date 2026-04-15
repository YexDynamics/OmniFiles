package com.eam.demoAPI.persistence.mapper;

import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.persistence.entity.Documento;
import com.eam.demoAPI.persistence.entity.Usuario;
import com.eam.demoAPI.persistence.entity.TipoDocumento;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface DocumentoMapper {

    // ENTITY -> DTO
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "tipoDocumentoId", source = "tipoDocumento.id")
    @Mapping(target = "createdAt", source = "fechaCreacion")
    @Mapping(target = "updatedAt", source = "fechaActualizacion")
    DocumentoDTO toDTO(Documento entity);

    List<DocumentoDTO> toDTOList(List<Documento> entities);

    // DTO -> ENTITY (CREATE)
    @InheritInverseConfiguration(name = "toDTO")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", source = "usuarioId", qualifiedByName = "idToUsuario")
    @Mapping(target = "tipoDocumento", source = "tipoDocumentoId", qualifiedByName = "idToTipoDocumento")
    @Mapping(target = "rutaArchivo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    Documento toEntity(DocumentoDTO dto);

    // UPDATE
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "tipoDocumento", ignore = true)
    @Mapping(target = "rutaArchivo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    void updateEntityFromDTO(DocumentoDTO dto, @MappingTarget Documento entity);

    @Named("idToUsuario")
    default Usuario idToUsuario(Long usuarioId) {
        if (usuarioId == null) return null;
        Usuario u = new Usuario();
        u.setId(usuarioId);
        return u;
    }

    @Named("idToTipoDocumento")
    default TipoDocumento idToTipoDocumento(Long tipoDocumentoId) {
        if (tipoDocumentoId == null) return null;
        TipoDocumento t = new TipoDocumento();
        t.setId(tipoDocumentoId);
        return t;
    }
}