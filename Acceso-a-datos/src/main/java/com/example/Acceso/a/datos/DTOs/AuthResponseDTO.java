package com.example.Acceso.a.datos.DTOs;

public record AuthResponseDTO(
        String token,
        String username,
        String nombre,
        String email
) {}

