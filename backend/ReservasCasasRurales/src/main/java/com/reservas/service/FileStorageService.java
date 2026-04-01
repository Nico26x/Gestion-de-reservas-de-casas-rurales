package com.reservas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${upload.dir}")
    private String uploadDir;

    public String guardarArchivo(MultipartFile file) {

        try {
            // Generar nombre único
            String nombreArchivo = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // Ruta completa
            File carpeta = new File(uploadDir);
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            File destino = new File(uploadDir + nombreArchivo);

            // Guardar archivo
            file.transferTo(destino);

            return nombreArchivo;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar archivo");
        }
    }
}
