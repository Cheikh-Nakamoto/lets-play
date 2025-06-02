package com._talent.lets_play.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.annotations.info.License;

import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;

import io.swagger.v3.oas.models.security.SecurityRequirement;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "Let's Play API",
                version = "1.0.0",
                description = """
            API de gestion pour l'application Let's Play.
            
            Cette API permet de gérer les utilisateurs, l'authentification et les fonctionnalités de jeu.
            
            ## Authentification
            Cette API utilise l'authentification JWT Bearer Token.
            Pour accéder aux endpoints protégés, vous devez :
            1. Vous connecter via `/api/auth/login`
            2. Utiliser le token JWT retourné dans l'en-tête Authorization: `Bearer <token>`
            
            ## Rôles
            - **USER** : Utilisateur standard avec accès limité à ses propres données
            - **ADMIN** : Administrateur avec accès complet à toutes les fonctionnalités
            """,
                contact = @io.swagger.v3.oas.annotations.info.Contact(
                        name = "Cheikh-Nakamoto",
                        email = "feppdougou@gmail.com",
                        url = "https://letsplay.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8443",
                        description = "Serveur de développement"
                ),
                @Server(
                        url = "https://api.letsplay.com",
                        description = "Serveur de production"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer Token authentication. Format: Bearer <token>"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Let's Play API")
                        .version("1.0.0")
                        .description("""
                            API de gestion pour l'application Let's Play.
                            
                            Cette API permet de gérer les utilisateurs, l'authentification et les fonctionnalités de jeu.
                            
                            ## Authentification
                            Cette API utilise l'authentification JWT Bearer Token.
                            Pour accéder aux endpoints protégés, vous devez :
                            1. Vous connecter via `/api/auth/login`
                            2. Utiliser le token JWT retourné dans l'en-tête Authorization: `Bearer <token>`
                            
                            ## Rôles
                            - **USER** : Utilisateur standard avec accès limité à ses propres données
                            - **ADMIN** : Administrateur avec accès complet à toutes les fonctionnalités
                            """)
                        .contact(new Contact()
                                .name("Équipe de développement Let's Play")
                                .email("dev@letsplay.com")
                                .url("https://letsplay.com"))
                        .license(new io.swagger.v3.oas.models.info.License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new io.swagger.v3.oas.models.servers.Server().url("http://localhost:8443").description("Serveur de développement"),
                        new io.swagger.v3.oas.models.servers.Server().url("https://api.letsplay.com").description("Serveur de production")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new io.swagger.v3.oas.models.security.SecurityScheme()
                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer Token authentication. Format: Bearer <token>")));
    }

    @Bean
    public GroupedOpenApi userManagementApi() {
        return GroupedOpenApi.builder()
                .group("user-management")
                .displayName("User Management")
                .pathsToMatch("/api/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authenticationApi() {
        return GroupedOpenApi.builder()
                .group("authentication")
                .displayName("Authentication")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .displayName("Public APIs")
                .pathsToMatch("/api/public/**")
                .build();
    }

    @Bean
    public GroupedOpenApi productManagementApi() {
        return GroupedOpenApi.builder()
                .group("product-management")
                .displayName("Product Management")
                .pathsToMatch("/api/products/**")
                .build();
    }
}