package com.eam.demoAPI.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Etapa individual dentro de un flujo de aprobación")
public class EtapaFlujoDTO {

    @Schema(description = "ID único", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID del flujo al que pertenece", example = "1", required = true)
    private Long flujoId;

    @Schema(description = "Orden de ejecución (1, 2, 3...)", example = "1", required = true)
    private Integer orden;

    @Schema(description = "Nombre de la etapa", example = "Revisión inicial", required = true)
    private String nombre;

    @Schema(description = "ID del rol responsable", example = "3", required = true)
    private Long rolId;

    @Schema(description = "Nombre del rol responsable", example = "REVISOR", accessMode = Schema.AccessMode.READ_ONLY)
    private String rolNombre;

    @Schema(description = "ID del usuario específico (null = cualquier usuario con el rol)", example = "5")
    private Long usuarioId;
}
