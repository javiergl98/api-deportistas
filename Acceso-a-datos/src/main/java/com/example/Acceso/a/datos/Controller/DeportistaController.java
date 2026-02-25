package com.example.Acceso.a.datos.Controller;

import com.example.Acceso.a.datos.Collections.Deportista;
import com.example.Acceso.a.datos.Repository.DeportistaRepository;
import com.example.Acceso.a.datos.Repository.EntrenamientoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/deportista")
public class DeportistaController {

    @Autowired
    private EntrenamientoRepository entrenamientoRepository;

    @Autowired
    private DeportistaRepository deportistaRepository;

    //Crear un deportista
    @PostMapping
    public ResponseEntity<Deportista> crearDeportista(@Valid @RequestBody Deportista deportista) {
        Deportista guardado = deportistaRepository.save(deportista);

        return new ResponseEntity<>(guardado, HttpStatus.CREATED);
    }

    //Listar
    @GetMapping
    public ResponseEntity<List<Deportista>> listarDeportistas(){
        List<Deportista> deportistas = deportistaRepository.findAll();

        return new ResponseEntity<>(deportistas, HttpStatus.FOUND);
    }

    //Borrar
    @DeleteMapping("/{id}")
    public ResponseEntity<?> borrarDeportista (@PathVariable String id) {
        Optional<Deportista> deportista = deportistaRepository.findById(id);
        if (deportista.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        deportistaRepository.deleteById(id);
        return new ResponseEntity<>(deportista, HttpStatus.NO_CONTENT);
    }

    //Obtener
    @GetMapping("/id")
    public ResponseEntity<?> obtenerDeportista (@PathVariable String id){
        Optional<Deportista> deportista = deportistaRepository.findById(id);
        if (deportista.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(deportista, HttpStatus.FOUND);
    }

    //Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarDeportista(@PathVariable String id, @RequestBody Deportista nuevosDatos){
        return deportistaRepository.findById(id)
                .map(deportista -> {
                    // Actualizamos los campos
                    deportista.setNombre(nuevosDatos.getNombre());
                    deportista.setEmail(nuevosDatos.getEmail());
                    deportista.setEdad(nuevosDatos.getEdad());

                    deportistaRepository.save(deportista);
                    return ResponseEntity.ok(deportista);
                })
                .orElse(ResponseEntity.notFound().build());
    }

}

