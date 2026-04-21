package com.eam.demoAPI.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tipo documental del sistema")
public class TipoDocumentoDTO {

    @Schema(description = "ID único", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre del tipo", example = "Contrato", required = true)
    private String nombre;

    @Schema(description = "Descripción", example = "Contratos comerciales")
    private String descripcion;

    @Schema(description = "Indica si está activo", example = "true")
    private Boolean estado;

    @Schema(description = "Indica si tiene flujo de aprobación configurado", example = "true")
    private Boolean flujoConfigurado;
}
