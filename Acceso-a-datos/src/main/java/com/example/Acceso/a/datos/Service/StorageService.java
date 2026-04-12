package com.example.Acceso.a.datos.Service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class StorageService {
    // Carpeta donde se guardarán las fotos de los entrenamientos
    private final Path rootLocation = Paths.get("upload-dir");

    public void init() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectory(rootLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el almacenamiento", e);
        }
    }

    public String store(MultipartFile file) {
        try {
            // Generamos un nombre único para evitar que se sobrescriban
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.rootLocation.resolve(filename));
            return filename;
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar archivo", e);
        }
    }
    public void delete(String filename) throws IOException {
        Path file = rootLocation.resolve(filename);
        // Borra el archivo solo si existe
        Files.deleteIfExists(file);
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) return resource;
            else throw new RuntimeException("No se pudo leer el archivo");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error en la URL del archivo", e);
        }
    }
}