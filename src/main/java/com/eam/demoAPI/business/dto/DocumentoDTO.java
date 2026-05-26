package com.eam.demoAPI.business.dto;

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

    @Schema(description = "Nombre del documento", example = "Contrato", required = true)
    private String nombre;

    @Schema(description = "Ruta del archivo en el servidor", accessMode = Schema.AccessMode.READ_ONLY)
    private String rutaArchivo;

    @Schema(description = "Nombre del estado actual", example = "CREADO")
    private String estado;

    @Schema(description = "ID del usuario responsable", example = "1", required = true)
    private Long usuarioId;

    @Schema(description = "ID del tipo documental", example = "2")
    private Long tipoDocumentoId;

    @Schema(description = "ID del flujo de aprobación seleccionado", example = "1")
    private Long flujoId;

    @Schema(description = "Indica si el documento está en papelera", example = "false")
    private Boolean eliminado = false;

    @Schema(description = "Fecha de creación", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de actualización", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}