package com.mailforge.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Описание API для Swagger. Страница: /swagger-ui/index.html, схема в JSON: /v3/api-docs.
 * Список методов строится автоматически по контроллерам.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mailforgeOpenApi() {
        return new OpenAPI().info(new Info()
                .title("MailForge API")
                .version("0.1")
                .description("API конструктора HTML-рассылок. Формат ошибок описан в docs/api-errors.md."));
    }
}
