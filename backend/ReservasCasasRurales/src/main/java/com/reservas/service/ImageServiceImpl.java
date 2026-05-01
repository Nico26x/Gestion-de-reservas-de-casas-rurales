package com.reservas.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;

@Service
public class ImageServiceImpl {

    private final Cloudinary cloudinary;

    public ImageServiceImpl(){
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dwx8zyl00");
        config.put("api_key", "658545764521793");
        config.put("api_secret", "u1ZUyZIB9PWufDX9tWx0Q57w1ek");
        cloudinary = new Cloudinary(config);
    }

    public Map upload(MultipartFile image) throws Exception {
        File file = convert(image);

        try {
            Map<String, Object> options = new HashMap<>();
            options.put("folder", "casas_rurales");
            options.put("resource_type", "image");

            return cloudinary.uploader().upload(file, options);

        } finally {
            file.delete();
        }
    }

    private File convert(MultipartFile image) throws IOException {
        File file = File.createTempFile(image.getOriginalFilename(), null);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(image.getBytes());
        fos.close();
        return file;
    }
}