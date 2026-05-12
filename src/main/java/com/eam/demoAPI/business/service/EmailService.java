package com.eam.demoAPI.business.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Servicio de notificaciones por email usando la API de Resend.
 * Envía correos al creador del documento en 3 momentos:
 *  - Cuando el documento es creado
 *  - Cuando el documento es aprobado (flujo completado)
 *  - Cuando el documento es rechazado (en cualquier etapa)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String RESEND_URL = "https://api.resend.com/emails";

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    private final RestTemplate restTemplate;

    /**
     * Notifica al creador que su documento fue recibido y está en proceso.
     */
    @Async
    public void notificarCreacion(String emailDestino, String nombreDocumento) {
        String asunto = "OmniFiles — Documento creado: " + nombreDocumento;
        String cuerpo = construirCuerpo(
                "Documento creado",
                "Tu documento <strong>" + nombreDocumento + "</strong> fue registrado correctamente " +
                        "en el sistema y ha iniciado su proceso de aprobación.",
                "#1E40AF"
        );
        enviar(emailDestino, asunto, cuerpo);
    }

    /**
     * Notifica al creador que su documento fue aprobado en todas las etapas.
     */
    @Async
    public void notificarAprobacion(String emailDestino, String nombreDocumento) {
        String asunto = "OmniFiles — Documento aprobado: " + nombreDocumento;
        String cuerpo = construirCuerpo(
                "Documento aprobado ✓",
                "Tu documento <strong>" + nombreDocumento + "</strong> fue aprobado " +
                        "en todas las etapas del flujo. Ya está disponible como documento finalizado.",
                "#16A34A"
        );
        enviar(emailDestino, asunto, cuerpo);
    }

    /**
     * Notifica al creador que su documento fue rechazado.
     */
    @Async
    public void notificarRechazo(String emailDestino, String nombreDocumento, String observaciones) {
        String asunto = "OmniFiles — Documento rechazado: " + nombreDocumento;
        String obs = (observaciones != null && !observaciones.isBlank())
                ? "<br><br><strong>Motivo:</strong> " + observaciones
                : "";
        String cuerpo = construirCuerpo(
                "Documento rechazado",
                "Tu documento <strong>" + nombreDocumento + "</strong> fue rechazado " +
                        "durante el proceso de revisión." + obs,
                "#B91C1C"
        );
        enviar(emailDestino, asunto, cuerpo);
    }


    private void enviar(String destino, String asunto, String cuerpoHtml) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "from",    fromEmail,
                    "to",      List.of(destino),
                    "subject", asunto,
                    "html",    cuerpoHtml
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(RESEND_URL, request, String.class);

            log.info("Email enviado a {} — {}", destino, asunto);
        } catch (Exception e) {
            // No interrumpir el flujo principal si el email falla
            log.error("Error enviando email a {}: {}", destino, e.getMessage());
        }
    }

    private String construirCuerpo(String titulo, String mensaje, String color) {
        return """
            <div style="font-family: Inter, Arial, sans-serif; max-width: 520px; margin: 0 auto; padding: 32px;">
              <div style="border-left: 4px solid %s; padding-left: 16px; margin-bottom: 24px;">
                <h2 style="color: %s; margin: 0 0 8px;">%s</h2>
              </div>
              <p style="color: #475569; font-size: 15px; line-height: 1.6;">%s</p>
              <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;">
              <p style="color: #94A3B8; font-size: 12px;">
                OmniFiles — Sistema de Gestión Documental<br>
                Este es un mensaje automático, no respondas este correo.
              </p>
            </div>
            """.formatted(color, color, titulo, mensaje);
    }
}