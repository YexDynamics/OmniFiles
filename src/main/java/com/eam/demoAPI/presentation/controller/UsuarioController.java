package com.eam.demoAPI.presentation.controller;

import com.eam.demoAPI.business.dto.UsuarioDTO;
import com.eam.demoAPI.business.dto.UsuarioResponseDTO;
import com.eam.demoAPI.business.service.UsuarioService;
import com.eam.demoAPI.exception.ConflictException;
import com.eam.demoAPI.exception.NotFoundException;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
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
 * - POST  /api/v1/usuarios                 - Crear usuario
 * - GET   /api/v1/usuarios/{id}            - Obtener usuario por ID
 * - GET   /api/v1/usuarios                 - Listar usuarios
 * - PUT   /api/v1/usuarios/{id}            - Actualizar usuario
 * - PATCH /api/v1/usuarios/{id}/estado     - Activar / Inactivar usuario
 * - DELETE /api/v1/usuarios/{id}           - Eliminar usuario
 */

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * CREATE - Crear usuario
     */
    @PostMapping
    @Operation(
            summary = "Crear usuario",
            description = "Crea un nuevo usuario en el sistema con nombre, email, contraseña y rol"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o campos obligatorios faltantes"),
            @ApiResponse(responseCode = "409", description = "El email ya está en uso"),
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
            UsuarioResponseDTO created = usuarioService.createUsuario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (ConflictException e) {
            log.warn("Conflicto al crear usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al crear usuario: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
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
            description = "Retorna la información de un usuario específico sin exponer la contraseña"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long id
    ) {
        log.debug("GET /api/v1/usuarios/{}", id);

        try {
            return ResponseEntity.ok(usuarioService.getUsuarioById(id));

        } catch (NotFoundException e) {
            log.warn("Usuario no encontrado ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * READ ALL - Listar usuarios
     */
    @GetMapping
    @Operation(
            summary = "Listar usuarios",
            description = "Obtiene todos los usuarios registrados en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios"),
            @ApiResponse(responseCode = "204", description = "No hay usuarios registrados"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> getAll() {
        log.debug("GET /api/v1/usuarios");

        try {
            List<UsuarioResponseDTO> usuarios = usuarioService.getUsuarios();

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
            description = "Actualiza la información de un usuario existente (nombre, email o rol)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> update(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long id,

            @Parameter(description = "Datos a actualizar", required = true)
            @RequestBody UsuarioDTO dto
    ) {
        log.info("PUT /api/v1/usuarios/{}", id);

        try {
            return ResponseEntity.ok(usuarioService.updateUsuario(id, dto));

        } catch (NotFoundException e) {
            log.warn("Usuario no encontrado ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al actualizar usuario {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("Error actualizando usuario {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * PATCH - Cambiar estado del usuario (activar / inactivar)
     */
    @PatchMapping("/{id}/estado")
    @Operation(
            summary = "Cambiar estado del usuario",
            description = "Permite activar o desactivar un usuario. Un usuario inactivo no puede iniciar sesión"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> cambiarEstado(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long id,

            @Parameter(description = "Estado del usuario (true = activo, false = inactivo)", required = true, example = "false")
            @RequestParam boolean activo
    ) {
        log.info("PATCH /api/v1/usuarios/{}/estado -> {}", id, activo);

        try {
            usuarioService.cambiarEstado(id, activo);
            return ResponseEntity.ok("Estado actualizado");

        } catch (NotFoundException e) {
            log.warn("Usuario no encontrado ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            log.error("Error cambiando estado del usuario {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * DELETE - Eliminar usuario
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina un usuario del sistema de forma permanente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno al eliminar")
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "ID del usuario a eliminar", required = true, example = "1")
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/usuarios/{}", id);

        try {
            usuarioService.deleteUsuario(id);
            return ResponseEntity.ok("Usuario eliminado");

        } catch (NotFoundException e) {
            log.warn("Usuario no encontrado ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            log.error("Error eliminando usuario {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }
}
