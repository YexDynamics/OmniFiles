package com.eam.demoAPI.presentation.controller;

import com.eam.demoAPI.business.dto.TipoDocumentoDTO;
import com.eam.demoAPI.business.service.TipoDocumentoService;
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
 * Controlador REST para gestión de tipos documentales
 * ENDPOINTS:
 * - POST  /api/v1/tipos-documentales              - Crear tipo documental
 * - GET   /api/v1/tipos-documentales/{id}         - Obtener tipo documental por ID
 * - GET   /api/v1/tipos-documentales              - Listar todos los tipos documentales
 * - GET   /api/v1/tipos-documentales/activos      - Listar solo los activos
 * - PUT   /api/v1/tipos-documentales/{id}         - Actualizar tipo documental
 * - PATCH /api/v1/tipos-documentales/{id}/estado  - Activar / Desactivar tipo documental
 */

@RestController
@RequestMapping("/api/v1/tipos-documentales")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tipos Documentales", description = "Gestión de tipos documentales y configuración de flujos")
@CrossOrigin(origins = "*")
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    /**
     * CREATE - Crear tipo documental
     */
    @PostMapping
    @Operation(
            summary = "Crear tipo documental",
            description = "Crea un nuevo tipo documental con nombre único. El flujo de aprobación se configura después"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Tipo documental creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TipoDocumentoDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Nombre inválido o faltante"),
            @ApiResponse(responseCode = "409", description = "Ya existe un tipo documental con ese nombre"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del tipo documental a crear",
                    required = true
            )
            @RequestBody TipoDocumentoDTO dto
    ) {
        log.info("POST /api/v1/tipos-documentales");

        try {
            TipoDocumentoDTO created = tipoDocumentoService.createTipoDocumento(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (ConflictException e) {
            log.warn("Conflicto al crear tipo documental: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al crear tipo documental: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("Error creando tipo documental: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * READ - Obtener tipo documental por ID
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener tipo documental por ID",
            description = "Retorna la información de un tipo documental específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tipo documental encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TipoDocumentoDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Tipo documental no encontrado")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "ID del tipo documental", required = true, example = "1")
            @PathVariable Long id
    ) {
        log.debug("GET /api/v1/tipos-documentales/{}", id);

        try {
            return ResponseEntity.ok(tipoDocumentoService.getTipoDocumentoById(id));

        } catch (NotFoundException e) {
            log.warn("Tipo documental no encontrado ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * READ ALL - Listar todos los tipos documentales
     */
    @GetMapping
    @Operation(
            summary = "Listar tipos documentales",
            description = "Obtiene todos los tipos documentales registrados, incluyendo activos e inactivos"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida"),
            @ApiResponse(responseCode = "204", description = "Sin tipos documentales registrados")
    })
    public ResponseEntity<?> getAll() {
        log.debug("GET /api/v1/tipos-documentales");

        List<TipoDocumentoDTO> tipos = tipoDocumentoService.getTiposDocumentales();

        if (tipos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(tipos);
    }

    /**
     * READ ACTIVOS - Listar solo tipos documentales activos
     */
    @GetMapping("/activos")
    @Operation(
            summary = "Listar tipos documentales activos",
            description = "Retorna únicamente los tipos documentales habilitados para crear documentos"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tipos activos"),
            @ApiResponse(responseCode = "204", description = "No hay tipos documentales activos")
    })
    public ResponseEntity<?> getActivos() {
        log.debug("GET /api/v1/tipos-documentales/activos");

        List<TipoDocumentoDTO> tipos = tipoDocumentoService.getTiposActivos();

        if (tipos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(tipos);
    }

    /**
     * UPDATE - Actualizar tipo documental
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar tipo documental",
            description = "Modifica el nombre, descripción o configuración de flujo de un tipo documental existente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tipo documental actualizado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TipoDocumentoDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Tipo documental no encontrado"),
            @ApiResponse(responseCode = "409", description = "El nombre ya está en uso por otro tipo"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> update(
            @Parameter(description = "ID del tipo documental", required = true, example = "1")
            @PathVariable Long id,

            @Parameter(description = "Datos a actualizar", required = true)
            @RequestBody TipoDocumentoDTO dto
    ) {
        log.info("PUT /api/v1/tipos-documentales/{}", id);

        try {
            return ResponseEntity.ok(tipoDocumentoService.updateTipoDocumento(id, dto));

        } catch (NotFoundException e) {
            log.warn("Tipo documental no encontrado ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (ConflictException e) {
            log.warn("Conflicto al actualizar tipo documental {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al actualizar tipo documental {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("Error actualizando tipo documental {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * PATCH - Activar o desactivar tipo documental
     */
    @PatchMapping("/{id}/estado")
    @Operation(
            summary = "Cambiar estado del tipo documental",
            description = "Activa o desactiva un tipo documental. Un tipo inactivo no puede usarse para crear documentos"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Tipo documental no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> cambiarEstado(
            @Parameter(description = "ID del tipo documental", required = true, example = "1")
            @PathVariable Long id,

            @Parameter(description = "Estado (true = activo, false = inactivo)", required = true, example = "false")
            @RequestParam boolean activo
    ) {
        log.info("PATCH /api/v1/tipos-documentales/{}/estado -> {}", id, activo);

        try {
            tipoDocumentoService.cambiarEstado(id, activo);
            return ResponseEntity.ok("Estado actualizado");

        } catch (NotFoundException e) {
            log.warn("Tipo documental no encontrado ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            log.error("Error cambiando estado tipo documental {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }
}
