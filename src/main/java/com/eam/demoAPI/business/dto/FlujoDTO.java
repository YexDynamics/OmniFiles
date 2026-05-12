package com.eam.demoAPI.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Flujo de aprobación asociado a una plantilla de documento")
public class FlujoDTO {

    @Schema(description = "ID único", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID de la plantilla (tipo_documento)", example = "2", required = true)
    private Long tipoDocumentoId;

    @Schema(description = "Nombre del flujo", example = "Flujo de aprobación de contratos", required = true)
    private String nombre;

    @Schema(description = "Etapas del flujo en orden", accessMode = Schema.AccessMode.READ_ONLY)
    private List<EtapaFlujoDTO> etapas;
}
