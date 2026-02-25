package com.example.Acceso.a.datos.Collections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Getter
@Setter
@AllArgsConstructor
@Document(collection = "disciplinas")
public class Disciplina {

    @Id
    private String id;
    private String nombre;

}
