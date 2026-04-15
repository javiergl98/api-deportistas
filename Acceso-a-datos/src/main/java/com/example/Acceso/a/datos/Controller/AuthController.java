package com.example.Acceso.a.datos.Controller;

import com.example.Acceso.a.datos.Collections.Usuario;
import com.example.Acceso.a.datos.DTOs.LoginDTO;
import com.example.Acceso.a.datos.DTOs.RegisterDTO;
import com.example.Acceso.a.datos.DTOs.AuthResponseDTO;
import com.example.Acceso.a.datos.Repository.UsuarioRepository;
import com.example.Acceso.a.datos.Service.JwtProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176", "http://localhost:5177"})
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.username(),
                            loginDTO.password()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            Usuario usuario = (Usuario) authentication.getPrincipal();
            String token = jwtProvider.generateToken(usuario);

            return ResponseEntity.ok(new AuthResponseDTO(
                    token,
                    usuario.getUsername(),
                    usuario.getNombre(),
                    usuario.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciales inválidas: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        // Verificar si el usuario ya existe
        if (usuarioRepository.findByUsername(registerDTO.username()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El username ya está en uso");
        }

        if (usuarioRepository.findByEmail(registerDTO.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El email ya está registrado");
        }

        // Crear nuevo usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(registerDTO.username());
        nuevoUsuario.setPassword(passwordEncoder.encode(registerDTO.password()));
        nuevoUsuario.setEmail(registerDTO.email());
        nuevoUsuario.setNombre(registerDTO.nombre());
        nuevoUsuario.setEnabled(true);

        usuarioRepository.save(nuevoUsuario);

        // Generar token para el nuevo usuario
        String token = jwtProvider.generateToken(nuevoUsuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseDTO(
                token,
                nuevoUsuario.getUsername(),
                nuevoUsuario.getNombre(),
                nuevoUsuario.getEmail()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }

        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(new AuthResponseDTO(
                "",
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getEmail()
        ));
    }
}

