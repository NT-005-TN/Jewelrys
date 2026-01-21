package com.jewelry.workshop.config;

import com.jewelry.workshop.presentation.exception.error.ErrorResponseDTO;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Schema errorSchema = new Schema()
                .addProperty("timestamp", new StringSchema().format("date-time"))
                .addProperty("status", new io.swagger.v3.oas.models.media.IntegerSchema())
                .addProperty("errorCode", new StringSchema())
                .addProperty("message", new StringSchema())
                .addProperty("path", new StringSchema());

        return new OpenAPI()
                .info(new Info()
                        .title("Ювелирная мастерская API")
                        .version("1.0")
                        .description("API для клиентов, продавцов и администраторов"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSchemas("ErrorResponseDTO", errorSchema)
                );
    }
}