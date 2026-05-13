package com.eam.demoAPI.business.service;

import com.eam.demoAPI.persistence.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limpieza automática de archivos físicos sin actividad reciente.
 *
 * Criterio de limpieza:
 *  - El documento lleva más de N días sin descargarse ni modificarse
 *  - Solo se borra el archivo físico, el registro en BD se conserva
 *  - Se ejecuta una vez al día a medianoche
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileCleanupService {

    private final DocumentoRepository documentoRepository;

    // Días de inactividad antes de borrar el archivo físico (configurable en properties)
    @Value("${omnifiles.cleanup.dias-inactividad:30}")
    private int diasInactividad;

    /**
     * Cron: todos los días a medianoche.
     * Ajustable en properties: omnifiles.cleanup.cron
     */
    @Scheduled(cron = "${omnifiles.cleanup.cron:0 0 0 * * *}")
    @Transactional(readOnly = true)
    public void limpiarArchivosInactivos() {
        log.info("Iniciando limpieza de archivos inactivos (umbral: {} días)", diasInactividad);

        LocalDateTime limite = LocalDateTime.now().minusDays(diasInactividad);
        AtomicInteger borrados = new AtomicInteger(0);
        AtomicInteger errores = new AtomicInteger(0);

        documentoRepository.findAll().stream()
                .filter(doc -> doc.getRutaArchivo() != null && !doc.getRutaArchivo().isBlank())
                .filter(doc -> {
                    // Usar updatedAt como fecha de último acceso; si es null usar fechaCreacion
                    LocalDateTime ultimaActividad = doc.getFechaActualizacion() != null
                            ? doc.getFechaActualizacion()
                            : doc.getFechaCreacion();
                    return ultimaActividad != null && ultimaActividad.isBefore(limite);
                })
                .forEach(doc -> {
                    Path archivo = Path.of(doc.getRutaArchivo());
                    if (Files.exists(archivo)) {
                        try {
                            Files.delete(archivo);
                            borrados.incrementAndGet();
                            log.info("Archivo eliminado por inactividad: documento ID={}, ruta={}",
                                    doc.getId(), doc.getRutaArchivo());
                        } catch (IOException e) {
                            errores.incrementAndGet();
                            log.warn("No se pudo eliminar archivo del documento ID={}: {}",
                                    doc.getId(), e.getMessage());
                        }
                    }
                });

        log.info("Limpieza completada — borrados: {}, errores: {}", borrados.get(), errores.get());
    }
}
