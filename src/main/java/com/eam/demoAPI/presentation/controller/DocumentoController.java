package com.eam.demoAPI.presentation.controller;

import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.business.service.DocumentoService;
import com.eam.demoAPI.exception.NotFoundException;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Controlador REST para gestión completa de documentos
 *
 * ENDPOINTS:
 * - POST   /api/v1/documentos                        - Crear documento
 * - POST   /api/v1/documentos/{id}/upload            - Subir archivo al documento
 * - GET    /api/v1/documentos/{id}                   - Obtener documento por ID
 * - GET    /api/v1/documentos                        - Listar / filtrar documentos
 * - PUT    /api/v1/documentos/{id}                   - Actualizar documento
 * - DELETE /api/v1/documentos/{id}                   - Eliminación lógica (soft delete)
 * - GET    /api/v1/documentos/papelera               - Ver documentos eliminados
 * - PUT    /api/v1/documentos/papelera/{id}/restaurar - Restaurar documento
 * - DELETE /api/v1/documentos/papelera/{id}          - Eliminación permanente
 * - GET    /api/v1/documentos/{id}/download          - Descargar archivo
 * - GET    /api/v1/documentos/{id}/historial         - Ver historial
 */

@RestController
@RequestMapping("/api/v1/documentos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documentos", description = "Gestión de documentos con flujo, estados y trazabilidad")
@CrossOrigin(origins = "*")
public class DocumentoController {

    private final DocumentoService documentoService;

    private static final String UPLOAD_DIR = "./uploads/";

    // Extensiones permitidas
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(".pdf", ".docx", ".txt");

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
            @Parameter(description = "Datos del documento a crear", required = true)
            @RequestBody DocumentoDTO dto
    ) {
        log.info("POST /api/v1/documentos");

        try {
            DocumentoDTO created = documentoService.createDocumento(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (NotFoundException e) {
            log.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("Error creando documento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * UPLOAD - Subir archivo al documento
     */
    @PostMapping("/{id}/upload")
    @Operation(
            summary = "Subir archivo al documento",
            description = "Carga el archivo físico (PDF, DOCX, etc.) y lo asocia al documento existente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo subido correctamente"),
            @ApiResponse(responseCode = "400", description = "Archivo vacío o inválido"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno al guardar el archivo")
    })
    public ResponseEntity<?> uploadArchivo(
            @Parameter(description = "ID del documento", required = true, example = "1")
            @PathVariable Long id,

            @Parameter(description = "Archivo a subir (PDF, DOCX, etc.)", required = true)
            @RequestParam("archivo") MultipartFile archivo
    ) {
        log.info("POST /api/v1/documentos/{}/upload", id);

        try {
            if (archivo.isEmpty()) {
                return ResponseEntity.badRequest().body("El archivo no puede estar vacío");
            }

            // Validar extensión — solo PDF, DOCX y TXT
            String originalFilename = archivo.getOriginalFilename() != null
                    ? archivo.getOriginalFilename().toLowerCase() : "";
            boolean extensionValida = EXTENSIONES_PERMITIDAS.stream()
                    .anyMatch(originalFilename::endsWith);
            if (!extensionValida) {
                return ResponseEntity.badRequest()
                        .body("Formato no permitido. Solo se aceptan: PDF, DOCX, TXT");
            }

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String nombreArchivo = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
            Path rutaDestino = uploadPath.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

            DocumentoDTO doc = documentoService.getDocumentoById(id);
            doc.setRutaArchivo(rutaDestino.toString());
            documentoService.updateDocumento(id, doc);

            return ResponseEntity.ok("Archivo subido correctamente: " + nombreArchivo);

        } catch (NotFoundException e) {
            log.warn("Documento no encontrado ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IOException e) {
            log.error("Error subiendo archivo para documento {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir el archivo");
        }
    }

    /**
     * READ - Obtener documento por ID
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener documento por ID",
            description = "Retorna la información de un documento específico que no esté en papelera"
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

        } catch (NotFoundException e) {
            log.warn("Documento no encontrado ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * READ ALL - Listar / filtrar documentos
     */
    @GetMapping
    @Operation(
            summary = "Listar documentos",
            description = "Permite listar documentos activos con filtros opcionales por estado, usuario, tipo documental y rango de fechas"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de documentos obtenida"),
            @ApiResponse(responseCode = "204", description = "Sin resultados para los filtros aplicados"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> getAll(
            @Parameter(description = "Estado del documento (CREADO, EN_REVISION, APROBADO, RECHAZADO)", example = "CREADO")
            @RequestParam(required = false) String estado,

            @Parameter(description = "ID del usuario propietario", example = "1")
            @RequestParam(required = false) Long usuarioId,

            @Parameter(description = "ID del tipo documental", example = "2")
            @RequestParam(required = false) Long tipoDocumentoId,

            @Parameter(description = "Fecha inicio del rango (ISO: 2025-01-01T00:00:00)", example = "2025-01-01T00:00:00")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,

            @Parameter(description = "Fecha fin del rango (ISO: 2025-12-31T23:59:59)", example = "2025-12-31T23:59:59")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta
    ) {
        try {
            List<DocumentoDTO> docs = documentoService.getDocumentosFiltrados(
                    estado, usuarioId, tipoDocumentoId, fechaDesde, fechaHasta);

            if (docs.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(docs);

        } catch (IllegalArgumentException e) {
            log.warn("Parámetros inválidos en filtro: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("Error listando documentos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * UPDATE - Actualizar documento
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar documento",
            description = "Actualiza los metadatos de un documento existente que no esté en papelera"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Documento actualizado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DocumentoDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o documento en papelera"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> update(
            @Parameter(description = "ID del documento", required = true, example = "1")
            @PathVariable Long id,

            @Parameter(description = "Datos a actualizar", required = true)
            @RequestBody DocumentoDTO dto
    ) {
        log.info("PUT /api/v1/documentos/{}", id);

        try {
            return ResponseEntity.ok(documentoService.updateDocumento(id, dto));

        } catch (NotFoundException e) {
            log.warn("Documento no encontrado ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al actualizar documento {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("Error actualizando documento {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * DELETE - Eliminación lógica (soft delete)
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar documento (soft delete)",
            description = "Marca el documento como eliminado enviándolo a la papelera, sin borrarlo físicamente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento enviado a papelera"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "ID del documento", required = true, example = "1")
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/documentos/{}", id);

        try {
            documentoService.softDeleteDocumento(id);
            return ResponseEntity.ok("Documento eliminado");

        } catch (NotFoundException e) {
            log.warn("Documento no encontrado ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            log.error("Error eliminando documento {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * PAPELERA - Listar documentos eliminados
     */
    @GetMapping("/papelera")
    @Operation(
            summary = "Ver papelera",
            description = "Lista todos los documentos que han sido eliminados lógicamente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de documentos en papelera"),
            @ApiResponse(responseCode = "204", description = "Papelera vacía")
    })
    public ResponseEntity<?> getPapelera() {
        log.debug("GET /api/v1/documentos/papelera");

        List<DocumentoDTO> docs = documentoService.getDocumentosEliminados();

        if (docs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(docs);
    }

    /**
     * RESTORE - Restaurar documento desde papelera
     */
    @PutMapping("/papelera/{id}/restaurar")
    @Operation(
            summary = "Restaurar documento",
            description = "Recupera un documento de la papelera devolviéndolo a su estado activo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento restaurado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado en papelera")
    })
    public ResponseEntity<?> restore(
            @Parameter(description = "ID del documento a restaurar", required = true, example = "1")
            @PathVariable Long id
    ) {
        log.info("PUT /api/v1/documentos/papelera/{}/restaurar", id);

        try {
            documentoService.restoreDocumento(id);
            return ResponseEntity.ok("Documento restaurado");

        } catch (NotFoundException e) {
            log.warn("Documento no encontrado para restaurar ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * DELETE PERMANENTE - Eliminar definitivamente
     */
    @DeleteMapping("/papelera/{id}")
    @Operation(
            summary = "Eliminar permanentemente",
            description = "Elimina el documento de forma definitiva e irreversible del sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento eliminado permanentemente"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    public ResponseEntity<?> deletePermanent(
            @Parameter(description = "ID del documento a eliminar", required = true, example = "1")
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/documentos/papelera/{}", id);

        try {
            documentoService.deletePermanent(id);
            return ResponseEntity.ok("Documento eliminado permanentemente");

        } catch (NotFoundException e) {
            log.warn("Documento no encontrado para eliminación permanente ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * DOWNLOAD - Descargar archivo del documento
     */
    @GetMapping("/{id}/download")
    @Operation(
            summary = "Descargar archivo del documento",
            description = "Descarga el archivo físico asociado al documento"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo descargado correctamente"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error al leer el archivo")
    })
    public ResponseEntity<?> download(
            @Parameter(description = "ID del documento", required = true, example = "1")
            @PathVariable Long id
    ) {
        log.info("GET /api/v1/documentos/{}/download", id);

        try {
            DocumentoDTO doc = documentoService.getDocumentoById(id);
            byte[] file = documentoService.downloadDocumento(id);
            String filename = doc.getNombre() != null ? doc.getNombre() : "documento";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(file);

        } catch (NotFoundException e) {
            log.warn("Documento no encontrado para descarga ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (RuntimeException e) {
            log.error("Error descargando documento {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * HISTORIAL - Consultar historial de un documento
     */
    @GetMapping("/{id}/historial")
    @Operation(
            summary = "Historial del documento",
            description = "Retorna el registro completo de todas las acciones realizadas sobre el documento"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial obtenido"),
            @ApiResponse(responseCode = "204", description = "Sin registros en el historial"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    public ResponseEntity<?> historial(
            @Parameter(description = "ID del documento", required = true, example = "1")
            @PathVariable Long id
    ) {
        log.debug("GET /api/v1/documentos/{}/historial", id);

        try {
            List<?> historial = documentoService.getHistorial(id);

            if (historial.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(historial);

        } catch (NotFoundException e) {
            log.warn("Documento no encontrado para historial ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
