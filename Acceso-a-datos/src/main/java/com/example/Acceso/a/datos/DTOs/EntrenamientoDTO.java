package com.example.Acceso.a.datos.DTOs;

import com.example.Acceso.a.datos.Collections.Disciplina;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record EntrenamientoDTO(
        String id,
        @NotNull(message = "La fecha no puede ser nula")
        LocalDate fecha,
        @NotNull(message = "La disciplina es obligatoria")
        Disciplina disciplina, // Guardamos el nombre directamente
        @NotBlank(message = "El ID del deportista es obligatorio para vincular el entrenamiento")
        String deportistaId,// Para poder crear/vincular el entrenamiento
        String nombreDeportista, // Para mostrar quién entrena sin cargar todo el objeto
        double distancia,
        int TiempoMinutos
) {}
