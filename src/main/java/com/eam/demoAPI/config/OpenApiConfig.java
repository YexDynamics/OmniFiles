package com.eam.demoAPI.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "OmniFiles API",
                version = "0.1.0",
                description = "API REST para la gestión documentaria",
                contact = @Contact(
                        name = "Construccion de Apps Empresariales",
                        email = "dev@eam.edu.co",
                        url = "eam.edu.co"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org"
                )
        ),
        servers = {
                @Server(url = "/omnifiles", description = "Servidor Local")
        },
        // ← Esto hace que Swagger mande el token en TODOS los endpoints
        // sin necesidad de agregarlo manualmente en cada uno
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Pega tu token JWT aquí (sin el prefijo 'Bearer')"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI();
    }
}