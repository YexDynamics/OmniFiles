package com.eam.demoAPI.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registro de cambios de un documento")
public class HistorialDocumentoDTO {

    @Schema(description = "ID único", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID documento", example = "1", required = true)
    private Long documentoId;

    @Schema(description = "ID usuario", example = "2", required = true)
    private Long usuarioId;

    @Schema(description = "Estado resultante", example = "APROBADO", accessMode = Schema.AccessMode.READ_ONLY)
    private String estado;

    @Schema(description = "Acción realizada", example = "CREACION", accessMode = Schema.AccessMode.READ_ONLY)
    private String accion;

    @Schema(description = "Observaciones del revisor/aprobador", example = "Falta firma en página 3")
    private String observaciones;

    @Schema(description = "Fecha del cambio", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaCambio;
}