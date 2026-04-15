# 🚀 Guía de Ejecución - Autenticación JWT

Resumen completo de lo implementado y cómo ejecutar el proyecto con autenticación JWT.

---

## ✅ Lo que se ha implementado

### Backend (Spring Boot)
1. ✅ **Colección Usuario** - Documento MongoDB con implementación de UserDetails
2. ✅ **UsuarioRepository** - Acceso a datos de usuarios con métodos findByUsername/Email
3. ✅ **DTOs de Autenticación**:
   - LoginDTO: username + password
   - RegisterDTO: username + password + email + nombre
   - AuthResponseDTO: token + datos de usuario
4. ✅ **JwtProvider** - Generación y validación de tokens JWT (24 horas)
5. ✅ **JwtAuthenticationFilter** - Interceptor para validar tokens en peticiones
6. ✅ **SecurityConfig** - Configuración de Spring Security con:
   - Stateless session policy
   - Rutas públicas: /auth/** 
   - Rutas protegidas: /deportista/**, /entrenamiento/**
   - CORS habilitado para localhost:5173
7. ✅ **AuthController** - Endpoints:
   - POST /auth/register - Registrar nuevo usuario
   - POST /auth/login - Login y obtener token
   - GET /auth/me - Datos del usuario autenticado
8. ✅ **Encriptación** - Contraseñas encriptadas con BCrypt
9. ✅ **Inicialización** - StorageService.init() automático con @PostConstruct

### Frontend (Guía incluida)
- ✅ **axiosConfig.js** - Interceptor para incluir token en peticiones
- ✅ **authStore.js** - State management con Pinia/Vue composables
- ✅ **Login.vue** - Página de login
- ✅ **Register.vue** - Página de registro
- ✅ **Navbar.vue** - Navbar con estado de autenticación
- ✅ **Router guards** - Redirección a login si no está autenticado

---

## 📥 Instalación y Ejecución

### Requisitos Previos
- Java 17+
- Maven 3.6+
- MongoDB 4.4+ (corriendo en localhost:27017)
- Node.js 16+
- npm o yarn

### 1. MongoDB
```bash
# Iniciar MongoDB (en Windows, si está instalado como servicio)
net start MongoDB

# O en Linux/Mac
brew services start mongodb-community

# O con Docker
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

### 2. Backend (Spring Boot)

```bash
# Navega a la carpeta del proyecto
cd "C:\Users\javie\Desktop\Acceso a Datos\Tarea4-MongoDB\Acceso-a-datos"

# Compila el proyecto
.\mvnw clean install

# Ejecuta la aplicación
.\mvnw spring-boot:run

# El servidor estará disponible en http://localhost:8080
```

**Archivos clave del backend:**
- `application.properties` - Configuración de MongoDB y JWT
- `Collections/Usuario.java` - Modelo de usuario
- `Service/JwtProvider.java` - Lógica JWT
- `Controller/AuthController.java` - Endpoints de autenticación
- `Config/SecurityConfig.java` - Configuración de seguridad

### 3. Frontend (Vite + Vue)

```bash
# En tu proyecto frontend
npm install

# Copia los archivos de la guía FRONTEND_AUTH_GUIDE.md:
# - src/api/axiosConfig.js
# - src/stores/authStore.js
# - src/pages/Login.vue
# - src/pages/Register.vue
# - src/components/Navbar.vue
# - src/router/index.js

# Ejecuta el servidor de desarrollo
npm run dev

# El cliente estará disponible en http://localhost:5173
```

---

## 🧪 Testing Manual

### Con Postman

#### 1. Registrar Usuario
```
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "username": "juan123",
  "password": "password123",
  "email": "juan@ejemplo.com",
  "nombre": "Juan García"
}
```

**Respuesta esperada (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "juan123",
  "nombre": "Juan García",
  "email": "juan@ejemplo.com"
}
```

#### 2. Login
```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "juan123",
  "password": "password123"
}
```

**Respuesta esperada (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "juan123",
  "nombre": "Juan García",
  "email": "juan@ejemplo.com"
}
```

#### 3. Obtener Usuario Actual (con token)
```
GET http://localhost:8080/auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 4. Acceder a Ruta Protegida
```
GET http://localhost:8080/deportista
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 5. Sin Token (Debería Fallar)
```
GET http://localhost:8080/deportista
# Sin header Authorization
# Respuesta esperada: 401 Unauthorized
```

---

## 🔑 Flujo de Autenticación Completo

```
Usuario → Página Login
           ↓
        [Introduce credenciales]
           ↓
        POST /auth/login
           ↓
      [Backend valida]
           ↓
      Retorna JWT Token
           ↓
   Frontend guarda en localStorage
           ↓
   Navbar actualiza (muestra nombre + logout)
           ↓
   Peticiones posteriores incluyen
   Authorization: Bearer <token>
           ↓
    JwtAuthenticationFilter valida
           ↓
  [Si es válido] → Procesa petición
  [Si no es válido] → Error 401 → Redirige a login
           ↓
     Usuario hace Logout
           ↓
  Elimina token de localStorage
           ↓
  Redirige a /login
```

---

## 🛠️ Troubleshooting

### Error: MongoDB no disponible
```
Error: connect ECONNREFUSED 127.0.0.1:27017
```
**Solución**: Verifica que MongoDB está corriendo. Inicia el servicio:
```bash
net start MongoDB    # Windows
brew services start mongodb-community  # Mac
sudo systemctl start mongod  # Linux
```

### Error: Token inválido/expirado
```
"Error en autenticacion JWT"
```
**Solución**: El token tiene expiración de 24 horas. Vuelve a hacer login para obtener un nuevo token.

### Error: CORS bloqueado
```
Access to XMLHttpRequest blocked by CORS policy
```
**Solución**: Asegúrate de que:
1. El frontend corre en `http://localhost:5173`
2. El backend tiene CORS habilitado en `http://localhost:5173`
3. Revisa `SecurityConfig.java` - `corsConfigurationSource()`

### Error: 401 Unauthorized en rutas protegidas
```
{"timestamp":"...","status":401,"error":"Unauthorized"}
```
**Solución**: 
1. Verifica que envías el token en header: `Authorization: Bearer <token>`
2. Comprueba que el token no está expirado
3. Prueba hacer login de nuevo

---

## 📊 Estructura de Carpetas Backend

```
src/main/java/com/example/Acceso/a/datos/
├── Collections/
│   ├── Deportista.java
│   ├── Entrenamiento.java
│   ├── Disciplina.java
│   └── Usuario.java          ← NUEVO
├── Controller/
│   ├── DeportistaController.java
│   ├── EntrenamientoController.java
│   └── AuthController.java   ← NUEVO
├── Repository/
│   ├── DeportistaRepository.java
│   ├── EntrenamientoRepository.java
│   └── UsuarioRepository.java ← NUEVO
├── Service/
│   ├── StorageService.java (actualizado)
│   └── JwtProvider.java      ← NUEVO
├── Security/
│   └── JwtAuthenticationFilter.java ← NUEVO
├── Config/
│   └── SecurityConfig.java   ← NUEVO
└── Tarea4MongoDbApplication.java
```

---

## 🔒 Tokens JWT - Información

**Estructura del Token:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqdWFuMTIzIiwiaWF0IjoxNjc0NTAyNDAwLCJleHAiOjE2NzQ1ODg4MDB9.sig
└─ Header        └─ Payload (claims)              └─ Signature
```

**Expiración:** 24 horas (86400000 ms)
**Algoritmo:** HS256 (HMAC SHA-256)
**Secreto:** Configurado en `application.properties` (jwt.secret)

---

## 📝 Próximos Pasos

1. **Implementar frontend** - Usa FRONTEND_AUTH_GUIDE.md como referencia
2. **Proteger endpoints adicionales** - Agregar @PreAuthorize si es necesario
3. **Refresh tokens** - Implementar mecanismo de renovación de tokens
4. **2FA** - Agregar autenticación de dos factores (opcional)
5. **Logs de autenticación** - Registrar intentos de login/registro
6. **Rate limiting** - Limitar intentos de login para prevenir ataques

---

## 📞 Documentación Referenciada

- **AGENTS.md** - Guía para agentes de IA
- **README.md** - Endpoints de la API
- **FRONTEND_AUTH_GUIDE.md** - Guía completa para frontend


