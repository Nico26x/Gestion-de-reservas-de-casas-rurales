package com.reservas.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class MultipartDiagnostics {

    @Autowired(required = false)
    private MultipartConfigElement multipartConfigElement;

    @EventListener(ApplicationStartedEvent.class)
    public void logMultipartConfig() {
        System.out.println("\n========== MULTIPART CONFIGURATION DIAGNOSTICS ==========");
        
        if (multipartConfigElement != null) {
            long maxFileSize = multipartConfigElement.getMaxFileSize();
            long maxRequestSize = multipartConfigElement.getMaxRequestSize();
            
            System.out.println("✓ Explicit MultipartConfig Bean LOADED");
            System.out.println("Max File Size: " + maxFileSize + " bytes (~" + (maxFileSize / (1024 * 1024)) + "MB)");
            System.out.println("Max Request Size: " + maxRequestSize + " bytes (~" + (maxRequestSize / (1024 * 1024)) + "MB)");
        } else {
            System.out.println("⚠ No explicit MultipartConfigElement bean found");
            System.out.println("Using Spring Boot defaults from application.properties");
            System.out.println("Max File Size: 25MB");
            System.out.println("Max Request Size: 30MB");
        }
        
        System.out.println("========================================================\n");
    }
}
