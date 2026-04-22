package com.eam.demoAPI.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
                        @Server(
                                url = "/omnifiles",
                                description = "Servidor Local"
                        )
        }
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa tu token JWT")
                        )
                );
    }
}
