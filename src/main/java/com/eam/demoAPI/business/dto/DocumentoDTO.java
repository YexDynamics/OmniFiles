package com.eam.demoAPI.business.dto;

import com.eam.demoAPI.persistence.entity.enums.EstadoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información del documento gestionado en el sistema")
public class DocumentoDTO {

    @Schema(description = "ID único del documento", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre del documento", example = "Contrato", required = true, maxLength = 150)
    private String nombre;

    @Schema(description = "Estado actual del documento", example = "CREADO")
    private EstadoDocumento estado;

    @Schema(description = "ID del usuario responsable", example = "1", required = true)
    private Long usuarioId;

    @Schema(description = "ID del tipo de documento", example = "2")
    private Long tipoDocumentoId;

    @Schema(description = "Indica si el documento está en papelera", example = "false")
    private Boolean eliminado = false;

    @Schema(description = "Fecha de creación", example = "2025-09-07T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de actualización", example = "2025-09-07T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}