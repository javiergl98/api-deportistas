package com.example.Acceso.a.datos.Collections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDate;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "entrenamientos")
public class Entrenamiento {

    @Id
    private String id;

    private LocalDate fecha;
    private double distancia;
    private Integer TiempoMinutos;
    private Disciplina disciplina;

    @Indexed
    @DocumentReference
    private Deportista deportista;
}
