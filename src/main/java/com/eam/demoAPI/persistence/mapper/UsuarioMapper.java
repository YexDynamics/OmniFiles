package com.eam.demoAPI.persistence.mapper;

import com.eam.demoAPI.business.dto.UsuarioDTO;
import com.eam.demoAPI.business.dto.UsuarioResponseDTO;
import com.eam.demoAPI.persistence.entity.Rol;
import com.eam.demoAPI.persistence.entity.Usuario;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface UsuarioMapper {

    @Mapping(target = "rolId", source = "rol.id")
    @Mapping(target = "activo", source = "estado")
    UsuarioDTO toDTO(Usuario entity);

    @Mapping(target = "rolId", source = "rol.id")
    @Mapping(target = "rolNombre", source = "rol.nombre")
    @Mapping(target = "activo", source = "estado")
    UsuarioResponseDTO toResponseDTO(Usuario entity);

    List<UsuarioResponseDTO> toResponseDTOList(List<Usuario> entities);

    List<UsuarioDTO> toDTOList(List<Usuario> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rol", source = "rolId", qualifiedByName = "idToRol")
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "documentos", ignore = true)
    Usuario toEntity(UsuarioDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "documentos", ignore = true)
    void updateEntityFromDTO(UsuarioDTO dto, @MappingTarget Usuario entity);

    @Named("idToRol")
    default Rol idToRol(Long rolId) {
        if (rolId == null) return null;
        Rol rol = new Rol();
        rol.setId(rolId);
        return rol;
    }
}
