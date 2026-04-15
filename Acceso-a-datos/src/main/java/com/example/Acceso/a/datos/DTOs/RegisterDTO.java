package com.example.Acceso.a.datos.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDTO(
        @NotBlank(message = "El username es obligatorio")
        @Size(min = 3, max = 20, message = "El username debe tener entre 3 y 20 caracteres")
        String username,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser válido")
        String email,
        @NotBlank(message = "El nombre es obligatorio")
        String nombre
) {}

