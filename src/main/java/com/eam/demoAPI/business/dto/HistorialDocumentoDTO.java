package com.eam.demoAPI.business.dto;


import com.eam.demoAPI.persistence.entity.enums.EstadoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registro de cambios de estado de un documento")
public class HistorialDocumentoDTO {

    @Schema(description = "ID único", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID documento", example = "1", required = true)
    private Long documentoId;

    @Schema(description = "ID usuario", example = "2", required = true)
    private Long usuarioId;

    @Schema(description = "Estado asignado", example = "APROBADO", accessMode = Schema.AccessMode.READ_ONLY)
    private EstadoDocumento estado;

    @Schema(description = "Fecha del cambio", example = "2025-09-07T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaCambio;
}