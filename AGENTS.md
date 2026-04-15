# AGENTS.md - Guía para Agentes de IA

## Visión General del Proyecto

**Tarea 4 - MongoDB** es una API REST de gestión deportiva desarrollada con **Spring Boot 4.0.3** (Java 17) y **MongoDB**. El sistema gestiona entrenamientos y perfiles de deportistas con soporte para carga de imágenes y export/import en JSON.

### Arquitectura en Capas

```
Controller (REST Endpoints)
    ↓
Service Layer (StorageService para archivos)
    ↓
Repository (DeportistaRepository, EntrenamientoRepository)
    ↓
MongoDB + File System (upload-dir/)
```

---

## Estructura Crítica de Componentes

### 1. **Modelos de Datos (Collections)**

**Deportista** (`Collections/Deportista.java`):
- Documento MongoDB con índice único en `email`
- Campos: `id`, `nombre`, `email` (unique), `edad`
- Sin relaciones directas outbound; es referenciado por Entrenamiento via `@DocumentReference`

**Usuario** (`Collections/Usuario.java`):
- Documento MongoDB que implementa `UserDetails` de Spring Security
- Índice único en `username` y `email`
- Campos: `id`, `username`, `password` (encriptada con BCrypt), `email`, `nombre`, `enabled`
- Usado para autenticación JWT y autorización

**Entrenamiento** (`Collections/Entrenamiento.java`):
- Documento MongoDB con referencias a Deportista
- Usa `@DocumentReference` + `@Indexed` para vincular deportistas (DBRef en MongoDB)
- Campos incluyen: `fecha`, `distancia`, `TiempoMinutos` (camelCase incorrecto, ver patrón de nombrado), `disciplina`, `rutaImagen`
- **Relación crítica**: `@DocumentReference private Deportista deportista`

**Disciplina** (`Collections/Disciplina.java`):
- Embedded document dentro de Entrenamiento (no es colección independiente)
- Contiene solo `nombre` (ej: "Running", "Ciclismo")

### 2. **DTOs (Desacoplamiento API-BD)**

**EntrenamientoDTO** (`DTOs/EntrenamientoDTO.java`):
- Java Record con validaciones Jakarta Bean Validation
- **Propósito**: Recibir datos del cliente sin cargar la entidad completa
- **Campos clave**:
  - `deportistaId` (@NotBlank): Usado para buscar y vincular Deportista en la BD
  - `nombreDeportista`: Metadata para respuestas sin cargar todo el objeto
  - Validaciones: `@NotNull`, `@NotBlank` en campos críticos
- **Patrón crítico**: El controlador busca Deportista por ID antes de crear Entrenamiento

**LoginDTO** (`DTOs/LoginDTO.java`):
- Record para recibir credenciales en `/auth/login`
- Campos: `username` (@NotBlank), `password` (@NotBlank, mín 6 caracteres)

**RegisterDTO** (`DTOs/RegisterDTO.java`):
- Record para registro de nuevos usuarios en `/auth/register`
- Campos: `username`, `password`, `email` (@Email), `nombre`
- Todas las validaciones son obligatorias

**AuthResponseDTO** (`DTOs/AuthResponseDTO.java`):
- Record de respuesta tras login/registro exitoso
- Contiene: `token` (JWT), `username`, `nombre`, `email`

### 3. **Repositorios**

**DeportistaRepository** y **EntrenamientoRepository** (Repository/):
- Extienden `MongoRepository<T, String>` (ID tipo String = ObjectId de MongoDB)
- Método custom: `EntrenamientoRepository.findByDeportistaId(deportistaId)`
- No hay lógica de negocio compleja; son CRUD simples

**UsuarioRepository** (Repository/):
- Extienden `MongoRepository<Usuario, String>`
- Métodos custom: `findByUsername(String)`, `findByEmail(String)` (retornan Optional)

---

## Flujos de Datos Clave

### Autenticación JWT
1. Cliente envía `LoginDTO` a `POST /auth/login` con username y password
2. `AuthController.login()` valida credenciales contra BD con `AuthenticationManager`
3. Si son válidas, `JwtProvider.generateToken()` crea un JWT con expiración de 24 horas
4. Cliente almacena el token en localStorage/sessionStorage
5. Incluye token en cabecera `Authorization: Bearer <token>` en peticiones protegidas
6. `JwtAuthenticationFilter` intercepta la petición, valida el token y establece SecurityContext

**Endpoints de autenticación**:
- `POST /auth/login` → Login con credenciales, retorna token + datos usuario
- `POST /auth/register` → Registro de nuevo usuario, retorna token automático
- `GET /auth/me` → Obtener datos del usuario autenticado (requiere token)

### Crear Entrenamiento
1. Cliente envía `EntrenamientoDTO` con `deportistaId` a `POST /entrenamiento` (requiere token)
2. `EntrenamientoController.guardar()` valida el DTO con `@Valid`
3. Busca Deportista por ID → si NO existe, retorna 404
4. Crea entidad `Entrenamiento` nuevos datos
5. Guarda en MongoDB con referencia DocumentReference al Deportista

**Endpoints críticos**:
- `POST /entrenamiento` → guardar con DTO (autenticado)
- `POST /entrenamiento/{id}/portada` → subir imagen (multipart, autenticado)
- `GET /entrenamiento/deportista/{deportistaId}` → entrenamientos filtrados (autenticado)

### Ciclo de Vida de Imágenes
1. `StorageService.store(MultipartFile)` → genera nombre único (`System.currentTimeMillis()_filename`)
2. Guarda en `upload-dir/` (carpeta raíz del proyecto)
3. Almacena ruta en campo `rutaImagen` del Entrenamiento
4. `GET /{id}/portada` → carga Resource y devuelve con headers multipart
5. `DELETE /{id}/portada` → borra archivo físico + limpia BD

---

## Patrones y Convenciones del Proyecto

### 1. **Validación en Múltiples Niveles**
- Controller: `@Valid` en parámetros de método
- DTO: Anotaciones `@NotNull`, `@NotBlank`, `@Valid` 
- MongoDB: Índices únicos (`@Indexed(unique = true)` en email)
- **Implicación**: No confundir DTO con Entity; siempre mapear manualmente en controller

### 2. **Naming Inconsistencies** ⚠️
- Campo en Entrenamiento: `TiempoMinutos` (PascalCase incorrecto - debería ser `tiempoMinutos`)
- Mismo en DTO: `TiempoMinutos`
- Al agregar features: mantener este patrón por compatibilidad, pero documentar la deuda técnica

### 3. **Manejo de Relaciones MongoDB**
- Deportista es "padre"; Entrenamiento es "hijo"
- Usar `@DocumentReference` para evitar duplicación de datos
- **Al eliminar Deportista**: Revisar cascadas (actualmente NO hay eliminación en cascada - puede dejar huérfanos)
- Buscar Entrenamiento por Deportista: usar método custom `findByDeportistaId()`

### 4. **Tratamiento de Nulos**
- `Optional` en repositorios: siempre usar `.map()` / `.orElse()` / `.orElseThrow()`
- Para imágenes: check `if (ent.getRutaImagen() == null || ent.getRutaImagen().isEmpty())`
- Disciplina puede ser null en JSON → no validado en DTO (¡potencial bug!)

### 5. **CORS y Frontend**
- Habilitado solo para `http://localhost:5173` (Vite/Vue)
- Cambiar en `@CrossOrigin(origins = "...")` si ambiente cambio

---

## Tecnologías Clave y Configuración

### Stack
- **Spring Boot**: 4.0.3, Java 17
- **MongoDB**: URI en `application.properties` → `mongodb://localhost:27017/App_entrenamiento`
- **Spring Security + JWT**: JJWT 0.12.3 para generación y validación de tokens
- **BCrypt**: Encriptación de contraseñas en Usuario
- **Build**: Maven, incluye Lombok para boilerplate
- **Testing**: MongoDB embedded test dependency + Jakarta Validation tests

### Configuración JWT
- Secreto en `application.properties`: `jwt.secret` (mín 256 bits para HS256)
- Expiración: `jwt.expiration=86400000` (24 horas en milisegundos)
- Token se envía en cabecera: `Authorization: Bearer <token>`
- Validación automática por `JwtAuthenticationFilter` en todas las rutas protegidas

### Seguridad
- Spring Security habilitado con filtro stateless (STATELESS session policy)
- Rutas públicas: `/auth/login`, `/auth/register`, `/auth/me` (sin autenticación)
- Rutas protegidas: `/deportista/**`, `/entrenamiento/**` (requieren token JWT válido)
- CORS configurado solo para `http://localhost:5173`
- CSRF deshabilitado (API stateless con JWT, no necesita CSRF)

### Inicialización del Almacenamiento
- `StorageService.init()` crea carpeta `upload-dir/` si no existe
- **Revisar**: No se llama automáticamente; verificar si existe listener que lo trigger en `@PostConstruct`

### Importar/Exportar JSON
- `GET /entrenamiento/export/json` → descarga lista completa como JSON
- `POST /entrenamiento/import/json` → sube archivo con lista de Entrenamientos
- Usa Jackson `ObjectMapper` y `TypeReference<List<Entrenamiento>>()`
- **Cuidado**: No valida integridad referencial en import (puede importar IDs de Deportistas inexistentes)

---

## Convenciones de Desarrollo

### Cómo Agregar un Nuevo Endpoint
1. Define DTO en `DTOs/` si necesita validación o desacoplamiento
2. Crea método en Controller con `@GetMapping`, `@PostMapping`, etc.
3. Inyecta repositorios necesarios con `@Autowired`
4. Retorna `ResponseEntity<T>` con status HTTP apropiado
5. Usa Optional.map() para manejo de nulos

### Cómo Agregar una Nueva Colección
1. Crea clase en `Collections/` con anotaciones:
   - `@Document(collection = "nombre_plural")`
   - `@Id` para MongoDB ID
   - `@Indexed` / `@Indexed(unique = true)` para búsquedas
2. Crea `XRepository extends MongoRepository<X, String>` en `Repository/`
3. Agrega métodos custom si necesita queries específicas (ej: `findBy...`)
4. Crea Controller en `Controller/` siguiendo patrón CRUD

### Testing
- Archivo de prueba base: `Tarea4MongoDbApplicationTests.java`
- Usa `@SpringBootTest` + MongoDB embedded

---

## 🔄 Mantenimiento de AGENTS.md

**⚠️ IMPORTANTE PARA AGENTES DE IA**: Cada vez que se implementen cambios significativos, actualiza este archivo:

- **Nuevas colecciones MongoDB**: Agrega entrada en sección "Estructura Crítica de Componentes"
- **Nuevos endpoints**: Documenta en "Flujos de Datos Clave"
- **Cambios en patrones de validación**: Actualiza "Patrones y Convenciones del Proyecto"
- **Nuevos DTOs**: Agrega referencia en tabla de "Referencias Rápidas"
- **Cambios en dependencias**: Actualiza "Tecnologías Clave y Configuración"
- **Nuevos bugs o deuda técnica descubiertos**: Agrega a "Puntos de Extensión y Deuda Técnica"

**Objetivo**: Mantener este documento como fuente única de verdad para que otros agentes IA (y desarrolladores humanos) comprendan rápidamente el estado actual del proyecto.

---

## 📋 Historial de Cambios Implementados

### ✅ Autenticación JWT (Completado)
- Colección Usuario con implementación UserDetails
- DTOs para Login, Register, AuthResponse
- JwtProvider para generación/validación de tokens
- SecurityConfig con Spring Security
- AuthController con endpoints /login, /register, /me
- JwtAuthenticationFilter para interceptar peticiones
- @PostConstruct en StorageService para inicialización automática
- FRONTEND_AUTH_GUIDE.md con ejemplos de implementación frontend

---

## Workflows Críticos

### Build y Deploy
```bash
# Desde Acceso-a-datos/
mvn clean install
mvn spring-boot:run
```

### MongoDB Requerido
```bash
# Local: MongoDB debe estar corriendo en localhost:27017
# Base de datos: App_entrenamiento (se crea automáticamente)
```

### Testing Manual (Postman)
- **Base URL**: `http://localhost:8080`
- Headers: `Content-Type: application/json`
- Ejemplos en README.md: Cuerpos JSON para POST /deportista y POST /entrenamiento

---

## Puntos de Extensión y Deuda Técnica

### Bugs/Riesgos Conocidos
1. **Orphan Entrenamientos**: Al deletar Deportista, sus Entrenamientos quedan huérfanos (sin referencia válida)
2. **Disciplina sin validación**: DTO no valida que Disciplina sea válida (puede ser null)
3. **Naming incorrecto**: `TiempoMinutos` en camelCase

### Mejoras Sugeridas (no implementadas)
- Eliminar en cascada de Entrenamientos al borrar Deportista
- Validación de Disciplina en DTO (enum o referencia a BD)
- Usar LocalDateTime en lugar de LocalDate para Entrenamientos
- Configurar limit de tamaño de archivos en StorageService
- Implementar refresh tokens para renovación automática
- Agregar 2FA (autenticación de dos factores)
- Rate limiting en endpoints de autenticación
- Logging detallado de intentos de autenticación

---

## Puntos de Conexión Externos

- **Frontend**: http://localhost:5173 (CORS habilitado)
- **MongoDB**: localhost:27017
- **Almacenamiento de archivos**: Carpeta local `upload-dir/` (relativo a donde se ejecuta el JAR)

---

## Referencias Rápidas

| Componente | Ubicación | Propósito |
|-----------|-----------|----------|
| DTOs | `DTOs/EntrenamientoDTO.java` | Validación y desacoplamiento |
| Login/Register DTOs | `DTOs/LoginDTO.java`, `DTOs/RegisterDTO.java` | Autenticación |
| Auth Response DTO | `DTOs/AuthResponseDTO.java` | Respuesta con token JWT |
| Almacenamiento | `Service/StorageService.java` | Gestión física de imágenes |
| JWT Provider | `Service/JwtProvider.java` | Generación y validación de tokens |
| Security Config | `Config/SecurityConfig.java` | Configuración de Spring Security |
| Endpoints Auth | `Controller/AuthController.java` | Login, registro, usuario actual |
| Endpoints Deportista | `Controller/DeportistaController.java` | CRUD de perfiles |
| Endpoints Entrenamiento | `Controller/EntrenamientoController.java` | CRUD + Imágenes + Import/Export |
| Configuración BD | `application.properties` | MongoDB URI + JWT config |


