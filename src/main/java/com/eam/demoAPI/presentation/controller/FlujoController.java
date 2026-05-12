package com.eam.demoAPI.presentation.controller;

import com.eam.demoAPI.business.dto.EtapaFlujoDTO;
import com.eam.demoAPI.business.dto.FlujoDTO;
import com.eam.demoAPI.business.service.FlujoService;
import com.eam.demoAPI.exception.ConflictException;
import com.eam.demoAPI.exception.NotFoundException;
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
 * Controlador REST para gestión de flujos de aprobación
 *
 * ENDPOINTS:
 * - POST  /api/v1/flujos                              - Crear flujo para una plantilla
 * - GET   /api/v1/flujos                              - Listar todos los flujos
 * - GET   /api/v1/flujos/tipo-documento/{tipoId}      - Obtener flujo de una plantilla
 * - POST  /api/v1/flujos/{flujoId}/etapas             - Agregar etapa al flujo
 * - GET   /api/v1/flujos/{flujoId}/etapas             - Ver etapas de un flujo
 * - DELETE /api/v1/flujos/etapas/{etapaId}            - Eliminar etapa
 */
@RestController
@RequestMapping("/api/v1/flujos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Flujos", description = "Configuración de flujos de aprobación por plantilla documental")
@CrossOrigin(origins = "*")
public class FlujoController {

    private final FlujoService flujoService;

    /**
     * CREATE - Crear flujo para una plantilla
     */
    @PostMapping
    @Operation(
            summary = "Crear flujo",
            description = "Crea el flujo de aprobación asociado a una plantilla de documento. Cada plantilla solo puede tener un flujo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Flujo creado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FlujoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Plantilla no encontrada"),
            @ApiResponse(responseCode = "409", description = "La plantilla ya tiene un flujo configurado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> crearFlujo(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del flujo", required = true)
            @RequestBody FlujoDTO dto
    ) {
        log.info("POST /api/v1/flujos");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(flujoService.crearFlujo(dto));
        } catch (ConflictException e) {
            log.warn("Conflicto al crear flujo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (NotFoundException e) {
            log.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creando flujo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * READ ALL - Listar todos los flujos
     */
    @GetMapping
    @Operation(summary = "Listar flujos", description = "Obtiene todos los flujos configurados en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida"),
            @ApiResponse(responseCode = "204", description = "Sin flujos configurados")
    })
    public ResponseEntity<?> getFlujos() {
        log.debug("GET /api/v1/flujos");
        List<FlujoDTO> flujos = flujoService.getFlujos();
        if (flujos.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(flujos);
    }

    /**
     * READ BY TIPO DOCUMENTO - Obtener flujo de una plantilla
     */
    @GetMapping("/tipo-documento/{tipoId}")
    @Operation(summary = "Obtener flujo por plantilla", description = "Retorna el flujo configurado para una plantilla específica, incluyendo todas sus etapas en orden")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flujo encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FlujoDTO.class))),
            @ApiResponse(responseCode = "404", description = "La plantilla no tiene flujo configurado")
    })
    public ResponseEntity<?> getFlujoByTipo(
            @Parameter(description = "ID de la plantilla (tipo_documento)", required = true, example = "1")
            @PathVariable Long tipoId
    ) {
        log.debug("GET /api/v1/flujos/tipo-documento/{}", tipoId);
        try {
            return ResponseEntity.ok(flujoService.getFlujoByTipoDocumento(tipoId));
        } catch (NotFoundException e) {
            log.warn("Flujo no encontrado para tipo {}", tipoId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * ADD ETAPA - Agregar etapa al flujo
     */
    @PostMapping("/{flujoId}/etapas")
    @Operation(
            summary = "Agregar etapa al flujo",
            description = "Agrega una etapa al flujo. El campo usuarioId es opcional — si se omite, cualquier usuario con el rol indicado puede resolver la tarea"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Etapa agregada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EtapaFlujoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Flujo, rol o usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ya existe una etapa con ese orden en el flujo"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> agregarEtapa(
            @Parameter(description = "ID del flujo", required = true, example = "1")
            @PathVariable Long flujoId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la etapa", required = true)
            @RequestBody EtapaFlujoDTO dto
    ) {
        log.info("POST /api/v1/flujos/{}/etapas", flujoId);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(flujoService.agregarEtapa(flujoId, dto));
        } catch (ConflictException e) {
            log.warn("Conflicto al agregar etapa: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (NotFoundException e) {
            log.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error agregando etapa: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    /**
     * READ ETAPAS - Ver etapas de un flujo
     */
    @GetMapping("/{flujoId}/etapas")
    @Operation(summary = "Ver etapas del flujo", description = "Lista todas las etapas de un flujo en orden ascendente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de etapas"),
            @ApiResponse(responseCode = "204", description = "El flujo no tiene etapas")
    })
    public ResponseEntity<?> getEtapas(
            @Parameter(description = "ID del flujo", required = true, example = "1")
            @PathVariable Long flujoId
    ) {
        log.debug("GET /api/v1/flujos/{}/etapas", flujoId);
        List<EtapaFlujoDTO> etapas = flujoService.getEtapasByFlujo(flujoId);
        if (etapas.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(etapas);
    }

    /**
     * DELETE ETAPA - Eliminar etapa del flujo
     */
    @DeleteMapping("/etapas/{etapaId}")
    @Operation(summary = "Eliminar etapa", description = "Elimina una etapa del flujo. Si era la única, la plantilla quedará sin flujo configurado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Etapa eliminada"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> eliminarEtapa(
            @Parameter(description = "ID de la etapa", required = true, example = "1")
            @PathVariable Long etapaId
    ) {
        log.info("DELETE /api/v1/flujos/etapas/{}", etapaId);
        try {
            flujoService.eliminarEtapa(etapaId);
            return ResponseEntity.ok("Etapa eliminada");
        } catch (Exception e) {
            log.error("Error eliminando etapa {}: {}", etapaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }
}
