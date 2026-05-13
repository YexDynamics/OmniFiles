package com.eam.demoAPI.auth;

import com.eam.demoAPI.auth.dto.AuthRequest;
import com.eam.demoAPI.auth.dto.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación.
 *
 * ENDPOINTS:
 * - POST /auth/login — Iniciar sesión y obtener token JWT
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login y gestión de tokens JWT")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * LOGIN - Iniciar sesión
     */
    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica al usuario con email y contraseña. Retorna el token JWT con el tiempo de expiración y los roles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso — retorna token JWT"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o campos vacíos"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "403", description = "Usuario inactivo")
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest authRequest
    ) {
        return ResponseEntity.ok(authService.login(authRequest));
    }
}
