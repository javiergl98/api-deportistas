package com.example.Acceso.a.datos.DTOs;

import com.example.Acceso.a.datos.Collections.Disciplina;
import java.time.LocalDate;

public record EntrenamientoResponseDTO(
        String id,
        String deportistaId,
        String nombreDeportista,
        LocalDate fecha,
        Double distancia,
        Integer tiempoMinutos,
        Disciplina disciplina
) {}


