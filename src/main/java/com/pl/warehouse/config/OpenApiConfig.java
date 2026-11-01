package com.pl.warehouse.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * STUDY (mid-level): OpenAPI metadata for Swagger UI.
 * Try http://localhost:8080/swagger-ui.html — click Authorize → user/user.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI warehouseOpenApi() {
        final String basic = "basicAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Warehouse CRM API")
                        .description("""
                                Study template: catalog, stock, purchase orders, reservations.
                                GET is public; POST/PUT/PATCH need USER; DELETE needs ADMIN.
                                """)
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(basic))
                .components(new Components().addSecuritySchemes(basic,
                        new SecurityScheme()
                                .name(basic)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")));
    }
}
