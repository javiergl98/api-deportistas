package com.example.Acceso.a.datos.Controller;

import com.example.Acceso.a.datos.Collections.Deportista;
import com.example.Acceso.a.datos.Collections.Entrenamiento;
import com.example.Acceso.a.datos.DTOs.EntrenamientoDTO;
import com.example.Acceso.a.datos.Repository.DeportistaRepository;
import com.example.Acceso.a.datos.Repository.EntrenamientoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/entrenamiento")
public class EntrenamientoController {

    @Autowired
    private EntrenamientoRepository entrenamientoRepository;

    @Autowired
    private DeportistaRepository deportistaRepository;

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


}
