package com.example.Acceso.a.datos.Collections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Document(collection = "deportistas")
public class Deportista {

    @Id
    private String id;

    private String nombre;

    @Indexed(unique = true)
    private String email;
    private int edad;

}
