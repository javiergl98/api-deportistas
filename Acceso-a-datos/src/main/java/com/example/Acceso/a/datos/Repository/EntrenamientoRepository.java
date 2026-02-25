package com.example.Acceso.a.datos.Repository;

import com.example.Acceso.a.datos.Collections.Entrenamiento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntrenamientoRepository extends MongoRepository<Entrenamiento, String> {

    List<Entrenamiento> findByDeportistaId(String deportistaId);
}
