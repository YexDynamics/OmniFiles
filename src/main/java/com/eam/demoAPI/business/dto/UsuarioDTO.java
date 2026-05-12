package com.eam.demoAPI.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información del usuario del sistema")
public class UsuarioDTO {

    @Schema(description = "ID único", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre", example = "Juan Pérez", required = true, maxLength = 100)
    private String nombre;

    @Schema(description = "Correo", example = "juan@email.com", required = true, maxLength = 100)
    private String email;

    @Schema(description = "Contraseña en texto plano — el sistema la hashea automáticamente",
            example = "juanito123", required = true, maxLength = 20)
    private String contrasena;

    @Schema(description = "ID del rol", example = "2", required = true)
    private Long rolId;

    @Schema(description = "Indica si el usuario está activo", example = "true")
    private Boolean activo;

    @Schema(description = "Número de teléfono (opcional)", example = "3001234567")
    private String telefono;

    @Schema(description = "Cargo del usuario en la organización (opcional)", example = "Analista de documentos")
    private String cargo;

}
