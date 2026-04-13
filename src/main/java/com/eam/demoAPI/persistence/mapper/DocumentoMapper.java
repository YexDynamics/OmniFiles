package com.eam.demoAPI.persistence.mapper;

import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.persistence.entity.Documento;
import com.eam.demoAPI.persistence.entity.Usuario;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface DocumentoMapper {

    // ENTITY -> DTO
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "eliminado", source = "eliminado")
    DocumentoDTO toDTO(Documento entity);

    List<DocumentoDTO> toDTOList(List<Documento> entities);

    // DTO -> ENTITY (CREATE)
    @InheritInverseConfiguration(name = "toDTO")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", source = "usuarioId", qualifiedByName = "idToUsuario")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "estado", ignore = true) // lo maneja service
    Documento toEntity(DocumentoDTO dto);

    // UPDATE
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(DocumentoDTO dto, @MappingTarget Documento entity);

    @Named("idToUsuario")
    default Usuario idToUsuario(Long usuarioId) {
        if (usuarioId == null) return null;
        Usuario u = new Usuario();
        u.setId(usuarioId);
        return u;
    }
}