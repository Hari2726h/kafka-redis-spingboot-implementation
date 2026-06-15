package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  SwaggerConfig                                                ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Configures OpenAPI / Swagger UI documentation for ALL API    ║
 * ║  endpoints. Provides interactive API explorer at              ║
 * ║  http://localhost:8080/swagger-ui.html                        ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - SpringDoc auto-scans all @RestController classes           ║
 * ║  - This bean customizes the API metadata and tag groupings    ║
 * ║  - Tags group endpoints by module in the Swagger UI           ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Configuration Bean                           ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce Backend API")
                        .description("Production-grade REST API for e-commerce with "
                                + "Product Management, User Management, Inventory, "
                                + "Cart, Orders, Payments, Shipments — powered by "
                                + "Spring Boot, MySQL, Redis, Kafka")
                        .version("2.0.0")
                        .contact(new Contact().name("E-Commerce App")))
                .tags(List.of(
                        new Tag().name("Products").description("Product CRUD operations"),
                        new Tag().name("Users").description("User registration and management"),
                        new Tag().name("Inventory").description("Stock management and tracking"),
                        new Tag().name("Cart").description("Shopping cart operations"),
                        new Tag().name("Orders").description("Order lifecycle management"),
                        new Tag().name("Payments").description("Payment processing (simulated)"),
                        new Tag().name("Shipments").description("Shipment tracking and delivery"),
                        new Tag().name("History").description("Audit log / history trail")
                ));
    }
}
