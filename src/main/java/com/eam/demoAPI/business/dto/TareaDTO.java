package com.eam.demoAPI.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tarea del flujo de aprobación de un documento")
public class TareaDTO {

    @Schema(description = "ID único", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID del documento", example = "1", required = true)
    private Long documentoId;

    @Schema(description = "ID del usuario asignado", example = "2", required = true)
    private Long usuarioAsignadoId;

    @Schema(description = "Nombre del estado (PENDIENTE, APROBADO, RECHAZADO, CORRECCION)",
            example = "PENDIENTE", accessMode = Schema.AccessMode.READ_ONLY)
    private String estado;

    @Schema(description = "Observaciones o comentario de la decisión", example = "Falta firma")
    private String observaciones;

    @Schema(description = "Fecha de asignación", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaAsignacion;

    @Schema(description = "Fecha de resolución", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaResolucion;
}
