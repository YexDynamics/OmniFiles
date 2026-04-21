package com.eam.demoAPI.persistence.mapper;

import com.eam.demoAPI.business.dto.TipoDocumentoDTO;
import com.eam.demoAPI.persistence.entity.TipoDocumento;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface TipoDocumentoMapper {

    TipoDocumentoDTO toDTO(TipoDocumento entity);

    List<TipoDocumentoDTO> toDTOList(List<TipoDocumento> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documentos", ignore = true)
    TipoDocumento toEntity(TipoDocumentoDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documentos", ignore = true)
    void updateEntityFromDTO(TipoDocumentoDTO dto, @MappingTarget TipoDocumento entity);
}
