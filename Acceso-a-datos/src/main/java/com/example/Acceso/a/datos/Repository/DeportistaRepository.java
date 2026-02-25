package com.example.Acceso.a.datos.Repository;

import com.example.Acceso.a.datos.Collections.Deportista;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeportistaRepository extends MongoRepository<Deportista, String> {
    Deportista findByEmail(String email);
}
