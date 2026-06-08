package com.airesume.middleware.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // "bearerAuth" is just a name we give this security scheme —
        // it's referenced below in SecurityRequirement to apply it globally
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Resume Analyzer API")
                        .description("Backend API for resume parsing, ATS scoring, and job matching")
                        .version("1.0.0"))
                // Adds a global "Authorize" button in Swagger UI where you paste your JWT
                // Once set, every request in Swagger UI automatically sends Authorization: Bearer <token>
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                // bearerFormat is just a hint for documentation — not enforced
                                .bearerFormat("JWT")));
    }
}