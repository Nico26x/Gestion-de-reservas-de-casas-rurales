package com.reservas.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración explícita para subida de archivos multipart.
 * Combina MultipartConfigElement bean + propiedades en application.properties para:
 * - Tamaño máximo de archivo: 25MB
 * - Tamaño máximo de request: 30MB  
 * - Threshold: 1MB (cuando se usa temp storage)
 * 
 * Esta clase está anotada con @Configuration para forzar que el bean se registre
 * incluso si Spring Boot pudiera configurarlo automáticamente via properties.
 */
@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement(
            "/tmp",                    // location for temp files
            25 * 1024 * 1024,          // max file size: 25MB
            30 * 1024 * 1024,          // max request size: 30MB
            1024 * 1024                // file size threshold: 1MB
        );
    }
}
