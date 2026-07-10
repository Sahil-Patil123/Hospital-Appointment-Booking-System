package com.sahil.hospital_appointment.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Swagger/OpenAPI documentation. Springdoc auto-scans every
 * @RestController and generates the docs - this class only supplies
 * metadata (title, description) and the JWT "Authorize" button setup.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hospitalAppointmentOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Hospital Appointment Booking System API")
                        .description("REST API for managing hospital appointments - "
                                + "supports ADMIN, DOCTOR, and PATIENT roles with JWT authentication. "
                                + "Covers doctor scheduling, slot availability, appointment booking, "
                                + "status lifecycle management, and medical records.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Sahil Patil")
                                .url("https://github.com/Sahil-Patil123")))
                // Registers the "Authorize" button in Swagger UI - lets you
                // paste a JWT once and have it applied to every try-it-out call
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}