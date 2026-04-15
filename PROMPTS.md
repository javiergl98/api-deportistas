# 📝 PROMPTS.md - Prompts Implementados

Prompts principales utilizados para implementar las funcionalidades del proyecto.

---

## 🎯 Prompts Clave

### Prompt 1: Análisis de Codebase para AGENTS.md
```
Analyze this codebase to generate a new `AGENTS.md` file in project root for guiding AI coding agents.

Focus on discovering the essential knowledge that would help AI agents be immediately productive in this codebase. Consider aspects like:
- The "big picture" architecture - major components, service boundaries, data flows
- Critical developer workflows (builds, tests, debugging)
- Project-specific conventions and patterns
- Integration points, external dependencies, cross-component communication patterns

Guidelines:
- Write concise, actionable instructions using markdown structure
- Include specific examples from the codebase when describing patterns
- Avoid generic advice - focus on THIS project's specific approaches
- Document only discoverable patterns, not aspirational practices
- Reference key files/directories that exemplify important patterns
```

### Prompt 2: Mantenimiento de AGENTS.md
```
Quiero que añadas un punto en el que se especifique a las IAs actualizar el AGENTS cada vez que se añadan cambios significativos de implementación
```

### Prompt 3: Implementación de Autenticación JWT
```
Añade al proyecto las siguientes funcionalidades: Autenticación JWT:

Login que obtenga un token del backend y lo almacene en el cliente.
Rutas protegidas: redirigir al login si no está autenticado.
Navbar que refleje el estado (nombre de usuario + logout, o enlace de login).
Token enviado en la cabecera Authorization en las peticiones protegidas.
```

---

## 🔧 Prompts de Debugging

### Prompt 4: Corregir Error de JwtProvider
```
Error:
C:\Users\javie\Desktop\Acceso a Datos\Tarea4-MongoDB\Acceso-a-datos\src\main\java\com\example\Acceso\a\datos\Service\JwtProvider.java:51:17
java: cannot find symbol
  symbol:   method parserBuilder()
  location: class io.j

El error indica que `parserBuilder()` no está disponible en la versión de JJWT. 
Actualiza el `JwtProvider.java` para usar la API correcta de JJWT 0.12.3
```

**Solución Aplicada:**
```java
// Cambiar de:
Jwts.parserBuilder()
    .setSigningKey(getSigningKey())
    .build()
    .parseClaimsJws(token);

// A:
Jwts.parser()
    .verifyWith(getSigningKey())
    .build()
    .parseSignedClaims(token)
    .getPayload();
```

### Prompt 5: Corregir Error de SecurityConfig - DaoAuthenticationProvider
```
Error:
C:\Users\javie\Desktop\Acceso a Datos\Tarea4-MongoDB\Acceso-a-datos\src\main\java\com\example\Acceso\a\datos\Config\SecurityConfig.java:48:42
java: constructor DaoAuthenticationProvider in class org.springframework.security.authentication.dao.DaoAuthenticationProvider cannot be applied to given types;
  required: org.springframework.security.core.userdetails.UserDetailsService
  found:    no arguments

El constructor de `DaoAuthenticationProvider` en Spring Security 6+ requiere un `UserDetailsService` en el constructor.
```

**Solución Aplicada:**
```java
// Cambiar de:
DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
auth.setUserDetailsService(userDetailsService());

// A:
DaoAuthenticationProvider auth = new DaoAuthenticationProvider(userDetailsService());
auth.setPasswordEncoder(passwordEncoder());
```

---

## 📋 Resumen de Cambios

| Prompt | Descripción | Estado |
|--------|-------------|--------|
| 1 | Generación AGENTS.md | ✅ Completado |
| 2 | Mantenimiento AGENTS.md | ✅ Completado |
| 3 | Autenticación JWT | ✅ Completado |
| 4 | Fix JwtProvider | ✅ Completado |
| 5 | Fix SecurityConfig | ✅ Completado |

---

**Versión:** 1.0 - 14 de Abril de 2026





