package com.eam.demoAPI.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de usuario (sin contraseña)")
public class UsuarioResponseDTO {

    @Schema(description = "ID único", example = "1")
    private Long id;

    @Schema(description = "Nombre", example = "Juan Pérez")
    private String nombre;

    @Schema(description = "Correo", example = "juan@email.com")
    private String email;

    @Schema(description = "ID del rol", example = "2")
    private Long rolId;

    @Schema(description = "Nombre del rol", example = "ADMIN")
    private String rolNombre;

    @Schema(description = "Indica si el usuario está activo", example = "true")
    private Boolean activo;
}
