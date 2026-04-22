package com.eam.demoAPI.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Respuesta del login")
public class LoginResponseDTO {

    @Schema(description = "Token JWT")
    private String token;

    @Schema(description = "Información del usuario autenticado")
    private UsuarioResponseDTO usuario;
}
