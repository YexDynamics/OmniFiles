package com.eam.demoAPI.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Flujo de aprobación para documentos")
public class FlujoDTO {

    @Schema(description = "ID único", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID de la plantilla — opcional para flujos genéricos", example = "2")
    private Long tipoDocumentoId;

    @Schema(description = "Nombre del flujo", example = "Flujo de aprobación interna", required = true)
    private String nombre;

    @Schema(description = "Etapas del flujo en orden", accessMode = Schema.AccessMode.READ_ONLY)
    private List<EtapaFlujoDTO> etapas;
}