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

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Gestión de usuarios dentro de organizaciones")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * =========================
     * CREAR USUARIO
     * =========================
     */
    @PostMapping
    @Operation(
            summary = "Crear usuario",
            description = "Crea un nuevo usuario dentro de una organización existente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
                    content = @Content(schema = @Schema(implementation = UsuarioDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o organización no válida")
    })
    public ResponseEntity<?> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del usuario a crear", required = true)
            @RequestBody UsuarioDTO dto) {

        try {
            UsuarioDTO created = usuarioService.createUsuario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * =========================
     * OBTENER POR ID
     * =========================
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener usuario por ID",
            description = "Retorna la información de un usuario específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = UsuarioDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id) {

        try {
            return ResponseEntity.ok(usuarioService.getUsuarioById(id));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
    }

    /**
     * =========================
     * LISTAR USUARIOS
     * =========================
     */
    @GetMapping
    @Operation(
            summary = "Listar usuarios",
            description = "Obtiene todos los usuarios o filtra por organización"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios"),
            @ApiResponse(responseCode = "204", description = "No hay usuarios")
    })
    public ResponseEntity<?> getAll(
            @Parameter(description = "ID de la organización (opcional)", example = "1")
            @RequestParam(required = false) Long organizacionId
    ) {

        List<UsuarioDTO> usuarios = usuarioService.getUsuarios(organizacionId);

        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(usuarios);
    }

    /**
     * =========================
     * ACTUALIZAR USUARIO
     * =========================
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza la información de un usuario existente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado",
                    content = @Content(schema = @Schema(implementation = UsuarioDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<?> update(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id,
            @RequestBody UsuarioDTO dto) {

        try {
            return ResponseEntity.ok(usuarioService.updateUsuario(id, dto));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
    }

    /**
     * =========================
     * ACTIVAR / INACTIVAR
     * =========================
     */
    @PatchMapping("/{id}/estado")
    @Operation(
            summary = "Cambiar estado del usuario",
            description = "Permite activar o desactivar un usuario mediante el parámetro 'activo'"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<?> cambiarEstado(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Estado del usuario (true = activo, false = inactivo)", example = "true")
            @RequestParam boolean activo
    ) {

        try {
            usuarioService.cambiarEstado(id, activo);
            return ResponseEntity.ok("Estado actualizado");

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
    }
}