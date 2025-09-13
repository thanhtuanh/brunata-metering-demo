package com.brunata.meteringdemo.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemeType;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger Konfiguration: Titel, Version, Server-URLs und (optionale) Basic-Auth Definition.
 * Die SecurityScheme-Deklaration dokumentiert Basic Auth in Swagger; sie muss nicht zwingend aktiviert sein.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Brunata Metering Demo API", version = "0.1.0",
                description = "API für Messwerte (Readings) und Abrechnung (Billing) – Demo/PoC"),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local"),
                @Server(url = "https://brunata-metering-demo.onrender.com", description = "Render")
        }
)
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class OpenApiConfig { }

