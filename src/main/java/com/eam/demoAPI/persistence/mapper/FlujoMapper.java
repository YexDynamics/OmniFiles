package com.eam.demoAPI.persistence.mapper;

import com.eam.demoAPI.business.dto.FlujoDTO;
import com.eam.demoAPI.persistence.entity.Flujo;
import com.eam.demoAPI.persistence.entity.TipoDocumento;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = EtapaFlujoMapper.class,
        unmappedTargetPolicy = ReportingPolicy.WARN)
public interface FlujoMapper {

    @Mapping(target = "tipoDocumentoId", source = "tipoDocumento.id")
    @Mapping(target = "etapas",          source = "etapas")
    FlujoDTO toDTO(Flujo entity);

    List<FlujoDTO> toDTOList(List<Flujo> entities);

    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "etapas",         ignore = true)
    @Mapping(target = "tipoDocumento",  source = "tipoDocumentoId", qualifiedByName = "idToTipoDocumento")
    Flujo toEntity(FlujoDTO dto);

    @Named("idToTipoDocumento")
    default TipoDocumento idToTipoDocumento(Long id) {
        if (id == null) return null;
        TipoDocumento t = new TipoDocumento();
        t.setId(id);
        return t;
    }
}
