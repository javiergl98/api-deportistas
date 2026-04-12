package com.example.Acceso.a.datos.Controller;

import com.example.Acceso.a.datos.Collections.Deportista;
import com.example.Acceso.a.datos.Collections.Entrenamiento;
import com.example.Acceso.a.datos.DTOs.EntrenamientoDTO;
import com.example.Acceso.a.datos.Repository.DeportistaRepository;
import com.example.Acceso.a.datos.Repository.EntrenamientoRepository;
import com.example.Acceso.a.datos.Service.StorageService;
import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/entrenamiento")
@CrossOrigin(origins = "http://localhost:5173")
public class EntrenamientoController {

    @Autowired
    private EntrenamientoRepository entrenamientoRepository;

    @Autowired
    private DeportistaRepository deportistaRepository;

    //Necesarios para tarea 5
    @Autowired private StorageService storageService;
    @Autowired private ObjectMapper objectMapper; // Para Import/Export JSON


    //Nuevos ENDpoints Tarea5 (subir imagenes a carpeta en directorio)

    // POST: Subir imagen asociada a un entrenamiento
    @PostMapping("/{id}/portada")
    public Entrenamiento subirImagen(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        Entrenamiento ent = entrenamientoRepository.findById(id).orElseThrow();
        String nombreArchivo = storageService.store(file);
        ent.setRutaImagen(nombreArchivo); // Actualizamos la ruta en la entidad
        return entrenamientoRepository.save(ent);
    }

    // GET: Descargar la imagen
    @GetMapping("/{id}/portada")
    public ResponseEntity<Resource> descargarImagen(@PathVariable String id) {
        // 1. Buscamos el entrenamiento en MongoDB
        Entrenamiento ent = entrenamientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrenamiento no encontrado"));

        // 2. Cargamos el archivo físico usando el StorageService
        // IMPORTANTE: Le pasamos 'ent.getRutaImagen()', que es el String con el nombre
        Resource file = (Resource) storageService.loadAsResource(ent.getRutaImagen());

        // 3. Devolvemos la respuesta
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE) // O IMAGE_PNG_VALUE
                .body(file);
    }

    //Borrar la imagen y limpiar en BD
    @DeleteMapping("/{id}/portada")
    public ResponseEntity<String> borrarPortada(@PathVariable String id) {
        // 1. Buscamos el entrenamiento
        Entrenamiento ent = entrenamientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrenamiento no encontrado"));

        // 2. Si no tiene imagen, no hay nada que borrar
        if (ent.getRutaImagen() == null || ent.getRutaImagen().isEmpty()) {
            return ResponseEntity.badRequest().body("El entrenamiento no tiene ninguna imagen asignada.");
        }

        try {
            // 3. Borramos el archivo físico usando el StorageService
            storageService.delete(ent.getRutaImagen());

            // 4. Limpiamos el campo en la base de datos
            ent.setRutaImagen(null);
            entrenamientoRepository.save(ent);

            return ResponseEntity.ok("Imagen eliminada correctamente del servidor y de la base de datos.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al intentar borrar el archivo físico: " + e.getMessage());
        }
    }

    // EXPORTAR A JSON (Opcional con IA)
    @GetMapping("/export/json")
    public ResponseEntity<byte[]> exportarJSON() throws IOException {
        List<Entrenamiento> lista = entrenamientoRepository.findAll();
        byte[] data = objectMapper.writeValueAsBytes(lista);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=entrenamientos.json")
                .body(data);
    }

    // IMPORTAR: Lee un archivo .json y guarda todos los entrenamientos de golpe
    @PostMapping("/import/json")
    public String importarJSON(@RequestParam("file") MultipartFile file) throws IOException {
        // Leemos el archivo y lo convertimos en una lista de objetos Entrenamiento
        List<Entrenamiento> lista = objectMapper.readValue(file.getInputStream(), new TypeReference<List<Entrenamiento>>(){});
        entrenamientoRepository.saveAll(lista);
        return "Importación completada: " + lista.size() + " registros añadidos.";
    }

    @PostMapping
    public ResponseEntity<?> guardar(@Valid @RequestBody EntrenamientoDTO dto) {

        // Usamos el repositorio de DEPORTISTA para buscar la relación
        return deportistaRepository.findById(dto.deportistaId())
                .map(deportistaEncontrado -> {

                    Entrenamiento ent = new Entrenamiento();
                    ent.setFecha(dto.fecha()); // Sacamos dato del DTO
                    ent.setDistancia(dto.distancia());
                    ent.setTiempoMinutos(dto.TiempoMinutos());
                    ent.setDisciplina(dto.disciplina());
                    ent.setDeportista(deportistaEncontrado); // Vinculamos con el deportista

                    // Usamos el repositorio de ENTRENAMIENTO para guardar la Entidad
                    entrenamientoRepository.save(ent);

                    return ResponseEntity.ok(ent);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    //Listar entrenamientos
    @GetMapping
    public ResponseEntity<List<Entrenamiento>> listarTodos() {
        List<Entrenamiento> entrenamientos = entrenamientoRepository.findAll();
        return ResponseEntity.ok(entrenamientos);
    }

    //Buscar entrenamiento por Id
    @GetMapping("/{id}")
    public ResponseEntity<Entrenamiento> obtenerPorId(@PathVariable String id) {
        return entrenamientoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //Eliminar entrenamiento
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        if (!entrenamientoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        entrenamientoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    //Listar entrenamientos de un deportista
    @GetMapping("/deportista/{deportistaId}")
    public ResponseEntity<List<Entrenamiento>> listarPorDeportista(@PathVariable String deportistaId) {
        List<Entrenamiento> lista = entrenamientoRepository.findByDeportistaId(deportistaId);
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @Valid @RequestBody EntrenamientoDTO dto) {
        return entrenamientoRepository.findById(id).map(entrenamiento -> {

            // 1. Buscamos al deportista primero para validar que existe
            return deportistaRepository.findById(dto.deportistaId()).map(nuevoDeportista -> {

                // 2. Si existe, actualizamos todo
                entrenamiento.setFecha(dto.fecha());
                entrenamiento.setDistancia(dto.distancia());
                entrenamiento.setTiempoMinutos(dto.TiempoMinutos());
                entrenamiento.setDisciplina(dto.disciplina());
                entrenamiento.setDeportista(nuevoDeportista);

                Entrenamiento actualizado = entrenamientoRepository.save(entrenamiento);
                return ResponseEntity.ok(actualizado);

            }).orElseGet(() -> ResponseEntity.badRequest().body(entrenamiento));

        }).orElse(ResponseEntity.notFound().build());
    }
}

