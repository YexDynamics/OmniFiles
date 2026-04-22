package com.eam.demoAPI.presentation.controller;

import com.eam.demoAPI.business.dto.TareaDTO;
import com.eam.demoAPI.business.service.TareaService;
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
import java.util.Map;

/**
 * Controlador REST para gestión del flujo de revisión y aprobación de documentos
 *
 * ENDPOINTS:
 * - POST  /api/v1/tareas                                  - Crear tarea de revisión
 * - GET   /api/v1/tareas/{id}                             - Obtener tarea por ID
 * - GET   /api/v1/tareas/documento/{documentoId}          - Ver tareas de un documento
 * - GET   /api/v1/tareas/pendientes/usuario/{usuarioId}   - Ver tareas pendientes de un usuario
 * - PATCH /api/v1/tareas/{id}/aprobar                     - Aprobar documento
 * - PATCH /api/v1/tareas/{id}/rechazar                    - Rechazar documento
 * - PATCH /api/v1/tareas/{id}/correccion                  - Solicitar corrección
 */

@RestController
@RequestMapping("/api/v1/tareas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tareas", description = "Flujo de revisión y aprobación de documentos")
@CrossOrigin(origins = "*")
public class TareaController {

    private final TareaService tareaService;

    /**
     * CREATE - Crear tarea de revisión para un documento
     */
    @PostMapping
    @Operation(
            summary = "Crear tarea de revisión",
            description = "Asigna una tarea de revisión a un usuario responsable para un documento específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Tarea creada y asignada correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TareaDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Documento o usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la tarea: documentoId y usuarioAsignadoId son obligatorios",
                    required = true
            )
            @RequestBody TareaDTO dto
    ) {
        log.info("POST /api/v1/tareas");

        try {
            TareaDTO created = tareaService.crearTarea(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (NotFoundException e) {
            log.warn("Recurso no encontrado al crear tarea: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al crear tarea: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("Error creando tarea: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * READ - Obtener tarea por ID
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener tarea por ID",
            description = "Retorna la información detallada de una tarea específica"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarea encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TareaDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "ID de la tarea", required = true, example = "1")
            @PathVariable Long id
    ) {
        log.debug("GET /api/v1/tareas/{}", id);

        try {
            return ResponseEntity.ok(tareaService.getTareaById(id));

        } catch (NotFoundException e) {
            log.warn("Tarea no encontrada ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * READ BY DOCUMENTO - Ver todas las tareas de un documento
     */
    @GetMapping("/documento/{documentoId}")
    @Operation(
            summary = "Ver tareas de un documento",
            description = "Lista todas las tareas del historial de revisión asociadas a un documento"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tareas del documento"),
            @ApiResponse(responseCode = "204", description = "El documento no tiene tareas asociadas")
    })
    public ResponseEntity<?> getByDocumento(
            @Parameter(description = "ID del documento", required = true, example = "1")
            @PathVariable Long documentoId
    ) {
        log.debug("GET /api/v1/tareas/documento/{}", documentoId);

        List<TareaDTO> tareas = tareaService.getTareasByDocumento(documentoId);

        if (tareas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(tareas);
    }

    /**
     * READ PENDIENTES - Ver tareas pendientes de un usuario
     */
    @GetMapping("/pendientes/usuario/{usuarioId}")
    @Operation(
            summary = "Ver tareas pendientes de un usuario",
            description = "Lista las tareas en estado PENDIENTE asignadas a un usuario, es decir, documentos que aún requieren su revisión"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tareas pendientes"),
            @ApiResponse(responseCode = "204", description = "El usuario no tiene tareas pendientes")
    })
    public ResponseEntity<?> getPendientesByUsuario(
            @Parameter(description = "ID del usuario", required = true, example = "2")
            @PathVariable Long usuarioId
    ) {
        log.debug("GET /api/v1/tareas/pendientes/usuario/{}", usuarioId);

        List<TareaDTO> tareas = tareaService.getTareasPendientesByUsuario(usuarioId);

        if (tareas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(tareas);
    }

    /**
     * APROBAR - Aprobar documento asociado a la tarea
     */
    @PatchMapping("/{id}/aprobar")
    @Operation(
            summary = "Aprobar documento",
            description = "Marca la tarea como aprobada y cambia el estado del documento a APROBADO"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarea aprobada y documento actualizado a APROBADO",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TareaDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "La tarea ya fue resuelta anteriormente"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> aprobar(
            @Parameter(description = "ID de la tarea", required = true, example = "1")
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Observaciones opcionales sobre la aprobación"
            )
            @RequestBody(required = false) Map<String, String> body
    ) {
        log.info("PATCH /api/v1/tareas/{}/aprobar", id);

        try {
            String obs = body != null ? body.get("observaciones") : null;
            return ResponseEntity.ok(tareaService.aprobar(id, obs));

        } catch (NotFoundException e) {
            log.warn("Tarea no encontrada ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            log.warn("No se puede aprobar tarea {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("Error aprobando tarea {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * RECHAZAR - Rechazar documento asociado a la tarea
     */
    @PatchMapping("/{id}/rechazar")
    @Operation(
            summary = "Rechazar documento",
            description = "Marca la tarea como rechazada y cambia el estado del documento a RECHAZADO"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarea rechazada y documento actualizado a RECHAZADO",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TareaDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "La tarea ya fue resuelta anteriormente"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> rechazar(
            @Parameter(description = "ID de la tarea", required = true, example = "1")
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Observaciones obligatorias sobre el rechazo"
            )
            @RequestBody(required = false) Map<String, String> body
    ) {
        log.info("PATCH /api/v1/tareas/{}/rechazar", id);

        try {
            String obs = body != null ? body.get("observaciones") : null;
            return ResponseEntity.ok(tareaService.rechazar(id, obs));

        } catch (NotFoundException e) {
            log.warn("Tarea no encontrada ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            log.warn("No se puede rechazar tarea {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("Error rechazando tarea {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * CORRECCIÓN - Solicitar correcciones al creador del documento
     */
    @PatchMapping("/{id}/correccion")
    @Operation(
            summary = "Solicitar corrección",
            description = "Devuelve el documento al estado CREADO para que el autor realice correcciones"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Corrección solicitada y documento devuelto a CREADO",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TareaDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "La tarea ya fue resuelta anteriormente"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> solicitarCorreccion(
            @Parameter(description = "ID de la tarea", required = true, example = "1")
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Observaciones con las correcciones requeridas"
            )
            @RequestBody(required = false) Map<String, String> body
    ) {
        log.info("PATCH /api/v1/tareas/{}/correccion", id);

        try {
            String obs = body != null ? body.get("observaciones") : null;
            return ResponseEntity.ok(tareaService.solicitarCorreccion(id, obs));

        } catch (NotFoundException e) {
            log.warn("Tarea no encontrada ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            log.warn("No se puede solicitar corrección en tarea {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("Error solicitando corrección en tarea {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }
}
