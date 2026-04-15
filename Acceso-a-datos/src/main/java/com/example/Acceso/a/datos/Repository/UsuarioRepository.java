package com.example.Acceso.a.datos.Repository;

import com.example.Acceso.a.datos.Collections.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
}

