package com.eam.demoAPI.persistence.mapper;

import com.eam.demoAPI.business.dto.EtapaFlujoDTO;
import com.eam.demoAPI.persistence.entity.EtapaFlujo;
import com.eam.demoAPI.persistence.entity.Flujo;
import com.eam.demoAPI.persistence.entity.Rol;
import com.eam.demoAPI.persistence.entity.Usuario;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface EtapaFlujoMapper {

    @Mapping(target = "flujoId",   source = "flujo.id")
    @Mapping(target = "rolId",     source = "rol.id")
    @Mapping(target = "rolNombre", source = "rol.nombre")
    @Mapping(target = "usuarioId", source = "usuario.id")
    EtapaFlujoDTO toDTO(EtapaFlujo entity);

    List<EtapaFlujoDTO> toDTOList(List<EtapaFlujo> entities);

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "flujo",   source = "flujoId",   qualifiedByName = "idToFlujo")
    @Mapping(target = "rol",     source = "rolId",     qualifiedByName = "idToRol")
    @Mapping(target = "usuario", source = "usuarioId", qualifiedByName = "idToUsuario")
    EtapaFlujo toEntity(EtapaFlujoDTO dto);

    @Named("idToFlujo")
    default Flujo idToFlujo(Long id) {
        if (id == null) return null;
        Flujo f = new Flujo();
        f.setId(id);
        return f;
    }

    @Named("idToRol")
    default Rol idToRol(Long id) {
        if (id == null) return null;
        Rol r = new Rol();
        r.setId(id);
        return r;
    }

    @Named("idToUsuario")
    default Usuario idToUsuario(Long id) {
        if (id == null) return null;
        Usuario u = new Usuario();
        u.setId(id);
        return u;
    }
}
