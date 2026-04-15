# 📌 REQUERIMIENTOS DEL FRONTEND - PROMPT PARA COPILOT BACKEND

**COPIA Y PEGA ESTO EN TU CHAT DE GITHUB COPILOT BACKEND:**

---

## 🎯 CONTEXTO

Tengo un **frontend React (Vite)** ejecutándose en `http://localhost:5175` que espera conectarse a mi backend Spring Boot en `http://localhost:8080`.

Mi frontend implementó:
- ✅ JWT Authentication con axios interceptor
- ✅ React Router (6 páginas + rutas protegidas)
- ✅ Global AuthContext para manejo de sesiones
- ✅ Llamadas a APIs REST esperando DTOs en camelCase

---

## 🔌 ENDPOINTS ESPERADOS

### AUTENTICACIÓN (Públicos)
```
POST   /auth/register          Registrar usuario (retorna JWT token)
POST   /auth/login             Login (retorna JWT token)
GET    /auth/me                Obtener datos usuario actual
```

### DEPORTISTAS (Requieren Token)
```
GET    /deportista             Listar todos
POST   /deportista             Crear nuevo
GET    /deportista/{id}        Obtener por ID
PUT    /deportista/{id}        Actualizar
DELETE /deportista/{id}        Eliminar
```

### ENTRENAMIENTOS (Requieren Token)
```
GET    /entrenamiento                              Listar todos
POST   /entrenamiento                              Crear nuevo
GET    /entrenamiento/deportista/{deportistaId}   Listar por deportista
DELETE /entrenamiento/{id}                         Eliminar
POST   /entrenamiento/{id}/upload-portada          Subir imagen
GET    /entrenamiento/{id}/portada                 Descargar imagen
POST   /entrenamiento/import                       Importar JSON
GET    /entrenamiento/export                       Exportar JSON
```

---

## ⚙️ REQUERIMIENTOS CRÍTICOS

### 1️⃣ CORS Configuration
Permitir requests desde:
- `http://localhost:5175`
- `http://localhost:5174`
- `http://localhost:5173`

Con métodos: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`
Headers permitidos: `Authorization`, `Content-Type`

```java
// Esto ya debe estar en SecurityConfig.java
CorsConfiguration configuration = new CorsConfiguration();
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:5173",
    "http://localhost:5174",
    "http://localhost:5175"
));
configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
configuration.setAllowedHeaders(Arrays.asList("*"));
configuration.setAllowCredentials(true);
```

### 2️⃣ JWT Token Handling - ⚠️ IMPORTANTE
- ❌ **NO hacer redirecciones** (NO status 302/303)
- ✅ **Solo retornar JSON**
- ✅ En token inválido → retornar **401 Unauthorized** (NO redirect)
- ✅ Frontend envía token en header: `Authorization: Bearer {token}`
- ✅ Todos los endpoints **EXCEPTO** `/auth/register` y `/auth/login` requieren token JWT

**Validación en cada request:**
```
GET /deportista
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

Si token inválido → 401 Unauthorized (JSON error)
Si token ausente → 401 Unauthorized (JSON error)
Si token válido → 200 OK con datos
```

### 3️⃣ Response DTOs - camelCase (NO snake_case)

**EntrenamientoDTO:**
```java
{
  String id,
  String deportistaId,           // ← Para vincular deportista
  String nombreDeportista,       // ← Nombre del deportista (evita queries extra)
  LocalDate fecha,
  BigDecimal distancia,
  Integer TiempoMinutos,         // ← IMPORTANTE: camelCase
  DisciplinaDTO disciplina
}
```

**DisciplinaDTO:**
```java
{
  String nombre  // Values: "Running", "Ciclismo", "Natación", "Triatlón", "Gym", "Otro"
}
```

**DeportistaDTO:**
```java
{
  String id,
  String nombre,
  String email,
  Integer edad
}
```

**AuthResponseDTO:**
```java
{
  String token,      // JWT token (24 horas expiración)
  String username,   // Username del usuario
  String email,      // Email del usuario
  String nombre      // Nombre completo
}
```

### 4️⃣ Validaciones

- ✅ Email y username únicos → error 409 Conflict si existen
- ✅ Campos requeridos no nulos → error 400 Bad Request
- ✅ Contraseñas hasheadas con BCrypt
- ✅ Token expira en 24 horas (86400000 ms)
- ✅ Email válido con patrón @email.com
- ✅ Contraseña mínimo 6 caracteres

### 5️⃣ Campo "nombreDeportista" en Entrenamientos

**CRÍTICO:** Cuando el frontend hace `GET /entrenamiento/deportista/{deportistaId}`, cada entrenamiento debe incluir:
```json
{
  "id": "123",
  "deportistaId": "456",
  "nombreDeportista": "Juan García",     // ← Incluir siempre
  "fecha": "2026-04-14",
  "distancia": 10.5,
  "TiempoMinutos": 45,
  "disciplina": { "nombre": "Running" }
}
```

Esto evita que el frontend deba hacer queries adicionales para obtener el nombre del deportista.

---

## 📋 FLUJOS ESPERADOS

### Flow 1: Registro e Inicio de Sesión
```
1. Usuario → POST /auth/register
   {
     "username": "juan123",
     "password": "pass123",
     "email": "juan@test.com",
     "nombre": "Juan García"
   }

2. Backend → 201 Created
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "username": "juan123",
     "email": "juan@test.com",
     "nombre": "Juan García"
   }

3. Frontend: localStorage.setItem('authToken', token)

4. Peticiones futuras:
   Authorization: Bearer {token}
```

### Flow 2: Crear Entrenamiento Vinculado a Deportista
```
1. Frontend obtiene deportistaId (ej: "64f5a1b2c3d4e5f6g7h8i9j0")

2. POST /entrenamiento
   Headers: Authorization: Bearer {token}
   {
     "deportistaId": "64f5a1b2c3d4e5f6g7h8i9j0",
     "fecha": "2026-04-14",
     "distancia": 10.5,
     "TiempoMinutos": 45,
     "disciplina": { "nombre": "Running" }
   }

3. Backend valida:
   - Token válido ✓
   - Deportista existe ✓
   - Campos válidos ✓

4. Backend retorna 201 Created:
   {
     "id": "new_entrenamiento_id",
     "deportistaId": "64f5a1b2c3d4e5f6g7h8i9j0",
     "nombreDeportista": "Juan García",  // ← Incluido
     "fecha": "2026-04-14",
     "distancia": 10.5,
     "TiempoMinutos": 45,
     "disciplina": { "nombre": "Running" }
   }
```

### Flow 3: Obtener Entrenamientos de un Deportista
```
1. GET /entrenamiento/deportista/64f5a1b2c3d4e5f6g7h8i9j0
   Headers: Authorization: Bearer {token}

2. Backend retorna 200 OK:
   [
     {
       "id": "ent1",
       "deportistaId": "64f5a1b2c3d4e5f6g7h8i9j0",
       "nombreDeportista": "Juan García",
       "fecha": "2026-04-14",
       "distancia": 10.5,
       "TiempoMinutos": 45,
       "disciplina": { "nombre": "Running" }
     },
     {
       "id": "ent2",
       "deportistaId": "64f5a1b2c3d4e5f6g7h8i9j0",
       "nombreDeportista": "Juan García",
       "fecha": "2026-04-13",
       "distancia": 8.3,
       "TiempoMinutos": 35,
       "disciplina": { "nombre": "Ciclismo" }
     }
   ]
```

---

## 🧪 TEST RÁPIDO (Después de implementar)

```bash
# 1. Registrar usuario
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username":"test",
    "password":"pass123",
    "email":"test@test.com",
    "nombre":"Test User"
  }'

# Respuesta esperada: 201 Created con token
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "username": "test",
#   "email": "test@test.com",
#   "nombre": "Test User"
# }

# 2. Copiar TOKEN de respuesta y reemplazar {TOKEN}

# 3. Obtener deportistas (debe retornar array vacío inicialmente)
curl -X GET http://localhost:8080/deportista \
  -H "Authorization: Bearer {TOKEN}"

# Respuesta esperada:
# - 401: Problema con JWT validation ❌
# - 302 Redirect: Problema con redirecciones (cambiar a 401) ❌
# - 200 OK con []: ¡CORRECTO! ✅

# 4. Crear deportista
curl -X POST http://localhost:8080/deportista \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "nombre":"Juan García",
    "email":"juan@test.com",
    "edad":28
  }'

# Respuesta esperada: 201 Created con deportista creado

# 5. Crear entrenamiento
DEPORTISTA_ID="<id_retornado_en_paso_4>"

curl -X POST http://localhost:8080/entrenamiento \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d "{
    \"deportistaId\":\"${DEPORTISTA_ID}\",
    \"fecha\":\"2026-04-14\",
    \"distancia\":10.5,
    \"TiempoMinutos\":45,
    \"disciplina\":{\"nombre\":\"Running\"}
  }"

# Respuesta esperada: 201 Created con nombreDeportista incluido
```

---

## ⚠️ ERRORES COMUNES QUE PODRÍA ENCONTRAR EL FRONTEND

### Error 1: CORS Bloqueado
```
Access to XMLHttpRequest at 'http://localhost:8080/...' 
from origin 'http://localhost:5173' has been blocked by CORS policy
```
**Solución:** Verificar que CORS está configurado en SecurityConfig.java

### Error 2: Token No Reconocido
```
GET /deportista
Response: 302 Redirect a /login
```
**Problema:** El backend está redirigiendo en lugar de retornar 401
**Solución:** Asegurar que JwtAuthenticationFilter retorna 401, NO redirect

### Error 3: Respuestas en snake_case
```
{ "deporte_id": "123" }  ❌
Esperado: { "deporteId": "123" }  ✅
```
**Solución:** Configurar Jackson para camelCase en application.properties

### Error 4: nombreDeportista Ausente
```
GET /entrenamiento/deportista/123
Response sin nombreDeportista  ❌
Esperado: incluir nombreDeportista en cada entrenamiento  ✅
```
**Solución:** Mapear nombre del deportista en el DTO

---

## 📞 CHECKLIST FINAL

- [ ] CORS configurado para 3 localhost
- [ ] JWT token retorna 401 (NO 302)
- [ ] Todos los DTOs están en camelCase
- [ ] campo `nombreDeportista` incluido en entrenamientos
- [ ] Endpoints de autenticación funcionan
- [ ] Endpoints de deportistas funcionan (CRUD)
- [ ] Endpoints de entrenamientos funcionan (CRUD)
- [ ] Upload/download de imágenes funcionan
- [ ] Import/export JSON funcionan
- [ ] Todos los endpoints excepto /auth requieren token
- [ ] Test manual con curl retorna 200 OK

---

**¿Necesitas que implemente algo específico? Estoy listo para ayudarte.**


