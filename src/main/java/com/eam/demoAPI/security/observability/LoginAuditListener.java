package com.eam.demoAPI.security.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Auditor de logins: registra intentos exitosos y fallidos.
 * No registra contraseñas ni tokens.
 */
@Component
public class LoginAuditListener {

    private static final Logger log = LoggerFactory.getLogger(LoginAuditListener.class);

    private final AtomicLong failedLoginCount = new AtomicLong();

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = safeUsername(event.getAuthentication().getName());
        failedLoginCount.incrementAndGet();
        log.warn("Intento de login fallido para usuario={}", username);
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = safeUsername(event.getAuthentication().getName());
        log.info("Login exitoso para usuario={}", username);
    }

    private static String safeUsername(String name) {
        if (name == null || name.isBlank()) return "(desconocido)";
        return name.length() > 128 ? name.substring(0, 128) + "..." : name;
    }

    public long getFailedLoginCount() {
        return failedLoginCount.get();
    }
}
