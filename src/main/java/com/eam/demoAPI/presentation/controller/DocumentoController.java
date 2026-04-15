package com.eam.demoAPI.presentation.controller;

import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.business.service.DocumentoService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión completa de documentos
 *
 * ENDPOINTS:
 * - POST /api/v1/documentos - Crear documento
 * - GET /api/v1/documentos/{id} - Obtener documento por ID
 * - GET /api/v1/documentos - Listar / filtrar documentos
 * - PUT /api/v1/documentos/{id} - Actualizar documento
 * - DELETE /api/v1/documentos/{id} - Eliminación lógica (soft delete)
 * - GET /api/v1/documentos/papelera - Ver documentos eliminados
 * - PUT /api/v1/documentos/papelera/{id}/restaurar - Restaurar documento
 * - DELETE /api/v1/documentos/papelera/{id} - Eliminación permanente
 * - GET /api/v1/documentos/{id}/download - Descargar documento
 * - GET /api/v1/documentos/{id}/historial - Ver historial
 */

@RestController
@RequestMapping("/api/v1/documentos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documentos", description = "Gestión de documentos con flujo, estados y trazabilidad")
@CrossOrigin(origins = "*")
public class DocumentoController {

    private final DocumentoService documentoService;

    /**
     * CREATE - Crear documento
     */
    @PostMapping
    @Operation(
            summary = "Crear documento",
            description = "Crea un nuevo documento asociado a un usuario y tipo documental existente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Documento creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DocumentoDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario o tipo documental no existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> createDocumento(
            @Parameter(description = "Datos del documento", required = true)
            @RequestBody DocumentoDTO dto
    ) {
        log.info("POST /api/v1/documentos");

        try {
            DocumentoDTO created = documentoService.createDocumento(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
            log.error("Error creando documento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * READ - Obtener documento por ID
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener documento por ID",
            description = "Retorna la información de un documento específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Documento encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DocumentoDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "ID del documento", required = true, example = "1")
            @PathVariable Long id
    ) {
        log.debug("GET /api/v1/documentos/{}", id);

        try {
            DocumentoDTO doc = documentoService.getDocumentoById(id);

            if (Boolean.TRUE.equals(doc.getEliminado())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
            }

            return ResponseEntity.ok(doc);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        }
    }

    /**
     * READ ALL - Listar / filtrar documentos
     */
    @GetMapping
    @Operation(
            summary = "Listar documentos",
            description = "Permite listar documentos y filtrarlos por estado o usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida"),
            @ApiResponse(responseCode = "204", description = "Sin resultados"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> getAll(
            @Parameter(description = "Estado del documento", example = "CREADO")
            @RequestParam(required = false) String estado,

            @Parameter(description = "ID del usuario", example = "1")
            @RequestParam(required = false) Long usuarioId
    ) {
        try {
            List<DocumentoDTO> docs = documentoService.getDocumentosFiltrados(estado, usuarioId);

            if (docs.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(docs);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Parámetros inválidos");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * UPDATE - Actualizar documento
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar documento",
            description = "Actualiza un documento existente. Los campos null pueden ser ignorados según lógica de negocio"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> update(
            @Parameter(description = "ID del documento", required = true)
            @PathVariable Long id,

            @Parameter(description = "Datos a actualizar", required = true)
            @RequestBody DocumentoDTO dto
    ) {
        try {
            return ResponseEntity.ok(documentoService.updateDocumento(id, dto));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Sin permisos");

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * DELETE - Eliminación lógica
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar documento (soft delete)",
            description = "Marca el documento como eliminado sin borrarlo físicamente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Documento eliminado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "ID del documento", required = true)
            @PathVariable Long id
    ) {
        try {
            documentoService.softDeleteDocumento(id);
            return ResponseEntity.noContent().build();

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Sin permisos");

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * PAPELERA - Listar eliminados
     */
    @GetMapping("/papelera")
    @Operation(summary = "Ver papelera", description = "Lista documentos eliminados (soft delete)")
    public ResponseEntity<?> getPapelera() {

        List<DocumentoDTO> docs = documentoService.getDocumentosEliminados();

        if (docs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(docs);
    }

    /**
     * RESTORE - Restaurar documento
     */
    @PutMapping("/papelera/{id}/restaurar")
    @Operation(summary = "Restaurar documento", description = "Restaura un documento eliminado")
    public ResponseEntity<?> restore(
            @Parameter(description = "ID del documento")
            @PathVariable Long id
    ) {
        try {
            documentoService.restoreDocumento(id);
            return ResponseEntity.ok("Documento restaurado");

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        }
    }

    /**
     * DELETE PERMANENTE
     */
    @DeleteMapping("/papelera/{id}")
    @Operation(summary = "Eliminar permanentemente", description = "Elimina el documento definitivamente")
    public ResponseEntity<?> deletePermanent(
            @Parameter(description = "ID del documento")
            @PathVariable Long id
    ) {
        try {
            documentoService.deletePermanent(id);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        }
    }

    /**
     * DOWNLOAD - Descargar documento
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "Descargar documento")
    public ResponseEntity<?> download(
            @Parameter(description = "ID del documento")
            @PathVariable Long id
    ) {
        try {
            byte[] file = documentoService.downloadDocumento(id);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=documento")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(file);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        }
    }

    /**
     * HISTORIAL
     */
    @GetMapping("/{id}/historial")
    @Operation(summary = "Historial del documento")
    public ResponseEntity<?> historial(
            @Parameter(description = "ID del documento")
            @PathVariable Long id
    ) {
        try {
            List<?> historial = documentoService.getHistorial(id);

            if (historial.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(historial);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        }
    }
}