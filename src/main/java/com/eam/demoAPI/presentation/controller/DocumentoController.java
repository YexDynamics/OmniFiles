package com.eam.demoAPI.presentation.controller;

import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.business.service.DocumentoService;
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
@RequestMapping("/api/v1/documentos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documentos", description = "Gestión de documentos con flujo, estados y trazabilidad")
@CrossOrigin(origins = "*")
public class DocumentoController {

    private final DocumentoService documentoService;

    /**
     * =========================
     * CREAR DOCUMENTO
     * =========================
     */
    @PostMapping
    @Operation(
            summary = "Crear documento",
            description = "Crea un nuevo documento e inicia automáticamente su flujo de aprobación"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Documento creado exitosamente",
                    content = @Content(schema = @Schema(implementation = DocumentoDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<?> createDocumento(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del documento a crear", required = true)
            @RequestBody DocumentoDTO dto) {

        try {
            DocumentoDTO created = documentoService.createDocumento(dto);
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
            summary = "Obtener documento por ID",
            description = "Retorna un documento específico según su identificador"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento encontrado",
                    content = @Content(schema = @Schema(implementation = DocumentoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "ID del documento", example = "1")
            @PathVariable Long id) {

        try {
            return ResponseEntity.ok(documentoService.getDocumentoById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        }
    }

    /**
     * =========================
     * LISTAR / FILTRAR
     * =========================
     */
    @GetMapping
    @Operation(
            summary = "Listar documentos",
            description = "Permite obtener todos los documentos o filtrarlos por estado y/o usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de documentos"),
            @ApiResponse(responseCode = "204", description = "No hay documentos")
    })
    public ResponseEntity<?> getAll(
            @Parameter(description = "Estado del documento (opcional)", example = "APROBADO")
            @RequestParam(required = false) String estado,

            @Parameter(description = "ID del usuario creador (opcional)", example = "2")
            @RequestParam(required = false) Long usuarioId
    ) {

        List<DocumentoDTO> docs = documentoService.getDocumentosFiltrados(estado, usuarioId);

        if (docs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(docs);
    }

    /**
     * =========================
     * ACTUALIZAR
     * =========================
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar documento",
            description = "Actualiza un documento existente. Se validan permisos del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento actualizado",
                    content = @Content(schema = @Schema(implementation = DocumentoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    public ResponseEntity<?> update(
            @Parameter(description = "ID del documento", example = "1")
            @PathVariable Long id,
            @RequestBody DocumentoDTO dto) {

        try {
            return ResponseEntity.ok(documentoService.updateDocumento(id, dto));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Sin permisos");

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        }
    }

    /**
     * =========================
     * ELIMINAR (SOFT DELETE)
     * =========================
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar documento",
            description = "Realiza un eliminado lógico del documento (envía a papelera)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Documento eliminado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "ID del documento", example = "1")
            @PathVariable Long id) {

        try {
            documentoService.softDeleteDocumento(id);
            return ResponseEntity.noContent().build();

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Sin permisos");

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        }
    }

    /**
     * =========================
     * PAPELERA
     * =========================
     */
    @GetMapping("/papelera")
    @Operation(
            summary = "Ver papelera",
            description = "Obtiene todos los documentos eliminados (soft delete)"
    )
    public ResponseEntity<?> getPapelera() {

        List<DocumentoDTO> docs = documentoService.getDocumentosEliminados();

        if (docs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(docs);
    }

    @PutMapping("/papelera/{id}/restaurar")
    @Operation(
            summary = "Restaurar documento",
            description = "Restaura un documento eliminado desde la papelera"
    )
    public ResponseEntity<?> restore(
            @Parameter(description = "ID del documento", example = "1")
            @PathVariable Long id) {

        try {
            documentoService.restoreDocumento(id);
            return ResponseEntity.ok("Documento restaurado");

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        }
    }

    @DeleteMapping("/papelera/{id}")
    @Operation(
            summary = "Eliminar permanentemente",
            description = "Elimina un documento de forma permanente desde la papelera"
    )
    public ResponseEntity<?> deletePermanent(
            @Parameter(description = "ID del documento", example = "1")
            @PathVariable Long id) {

        try {
            documentoService.deletePermanent(id);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        }
    }

    /**
     * =========================
     * DESCARGAR
     * =========================
     */
    @GetMapping("/{id}/download")
    @Operation(
            summary = "Descargar documento",
            description = "Descarga el archivo asociado al documento"
    )
    public ResponseEntity<?> download(
            @Parameter(description = "ID del documento", example = "1")
            @PathVariable Long id) {

        try {
            byte[] file = documentoService.downloadDocumento(id);
            return ResponseEntity.ok(file);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        }
    }

    /**
     * =========================
     * HISTORIAL
     * =========================
     */
    @GetMapping("/{id}/historial")
    @Operation(
            summary = "Historial del documento",
            description = "Obtiene la trazabilidad completa del documento (cambios de estado, acciones, etc.)"
    )
    public ResponseEntity<?> historial(
            @Parameter(description = "ID del documento", example = "1")
            @PathVariable Long id) {

        try {
            return ResponseEntity.ok(documentoService.getHistorial(id));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        }
    }
}