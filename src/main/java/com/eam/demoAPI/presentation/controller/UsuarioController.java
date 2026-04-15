package com.eam.demoAPI.presentation.controller;

import com.eam.demoAPI.business.dto.UsuarioDTO;
import com.eam.demoAPI.business.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de usuarios
 *
 * ENDPOINTS:
 * - POST /api/v1/usuarios - Crear usuario
 * - GET /api/v1/usuarios/{id} - Obtener usuario por ID
 * - GET /api/v1/usuarios - Listar usuarios
 * - PUT /api/v1/usuarios/{id} - Actualizar usuario
 * - PATCH /api/v1/usuarios/{id}/estado - Activar/Inactivar usuario
 */

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Gestión de usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * CREATE - Crear usuario
     */
    @PostMapping
    @Operation(
            summary = "Crear usuario",
            description = "Crea un nuevo usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del usuario a crear",
                    required = true
            )
            @RequestBody UsuarioDTO dto
    ) {
        log.info("POST /api/v1/usuarios");

        try {
            UsuarioDTO created = usuarioService.createUsuario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
            log.error("Error creando usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * READ - Obtener usuario por ID
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener usuario por ID",
            description = "Retorna la información de un usuario específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id
    ) {
        log.debug("GET /api/v1/usuarios/{}", id);

        try {
            return ResponseEntity.ok(usuarioService.getUsuarioById(id));

        } catch (RuntimeException e) {
            log.warn("Usuario no encontrado ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
    }

    /**
     * READ ALL - Listar usuarios
     */
    @GetMapping
    @Operation(
            summary = "Listar usuarios",
            description = "Obtiene todos los usuarios"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios"),
            @ApiResponse(responseCode = "204", description = "No hay usuarios"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> getAll() {
        log.debug("GET /api/v1/usuarios");

        try {
            List<UsuarioDTO> usuarios = usuarioService.getUsuarios();

            if (usuarios.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(usuarios);

        } catch (Exception e) {
            log.error("Error listando usuarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * UPDATE - Actualizar usuario
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza la información de un usuario existente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> update(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Datos a actualizar", required = true)
            @RequestBody UsuarioDTO dto
    ) {
        log.info("PUT /api/v1/usuarios/{}", id);

        try {
            return ResponseEntity.ok(usuarioService.updateUsuario(id, dto));

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
            log.warn("Usuario no encontrado ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");

        } catch (Exception e) {
            log.error("Error actualizando usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * PATCH - Cambiar estado del usuario
     */
    @PatchMapping("/{id}/estado")
    @Operation(
            summary = "Cambiar estado del usuario",
            description = "Permite activar o desactivar un usuario mediante el parámetro 'activo'"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> cambiarEstado(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Estado del usuario (true = activo, false = inactivo)", example = "true")
            @RequestParam boolean activo
    ) {
        log.info("PATCH /api/v1/usuarios/{}/estado -> {}", id, activo);

        try {
            usuarioService.cambiarEstado(id, activo);
            return ResponseEntity.ok("Estado actualizado");

        } catch (RuntimeException e) {
            log.warn("Usuario no encontrado ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");

        } catch (Exception e) {
            log.error("Error cambiando estado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }
}