# 🏋️‍♂️ API de Gestión Deportiva

Sistema de gestión de entrenamientos y deportistas desarrollado con **Spring Boot** y **MongoDB**. La API permite una gestión eficiente de actividades físicas mediante el uso de **DTOs** para la transferencia segura de datos, con autenticación JWT integrada.

---

## 🛠️ Tecnologías y Herramientas
* **Backend:** Spring Boot 4.0.3
* **Base de Datos:** MongoDB (NoSQL)
* **Seguridad:** Spring Security + JWT (JJWT 0.12.3)
* **Validación:** Jakarta Bean Validation
* **Mapeo:** Java Records (DTOs)
* **Encriptación:** BCrypt para contraseñas
* **Pruebas:** Postman

---

## 🔐 Autenticación JWT

### Registro de Usuario
```bash
POST /auth/register
Content-Type: application/json

{
  "username": "usuario123",
  "password": "password123",
  "email": "usuario@ejemplo.com",
  "nombre": "Juan Pérez"
}
```

**Respuesta (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "usuario123",
  "nombre": "Juan Pérez",
  "email": "usuario@ejemplo.com"
}
```

### Login
```bash
POST /auth/login
Content-Type: application/json

{
  "username": "usuario123",
  "password": "password123"
}
```

**Respuesta (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "usuario123",
  "nombre": "Juan Pérez",
  "email": "usuario@ejemplo.com"
}
```

### Obtener Usuario Actual
```bash
GET /auth/me
Authorization: Bearer <tu_token>
```

### Almacenar Token en Cliente
El token debe almacenarse en `localStorage` o `sessionStorage`:

```javascript
// Después del login
const response = await fetch('http://localhost:8080/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'usuario123', password: 'password123' })
});

const data = await response.json();
localStorage.setItem('authToken', data.token);
localStorage.setItem('username', data.username);
```

### Enviar Token en Peticiones Protegidas
Todos los endpoints de `/deportista/**` y `/entrenamiento/**` requieren autenticación:

```javascript
const token = localStorage.getItem('authToken');
const response = await fetch('http://localhost:8080/deportista', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

---

## 🚦 Endpoints Principales

### 🔑 Autenticación
| Método | Endpoint | Descripción | Autenticación |
| :--- | :--- | :--- | :--- |
| **POST** | `/auth/register` | Registrar nuevo usuario | ❌ No |
| **POST** | `/auth/login` | Login con credenciales | ❌ No |
| **GET** | `/auth/me` | Obtener usuario actual | ✅ Sí |

### 👤 Sección: Deportistas
| Método | Endpoint | Acción | Autenticación |
| :--- | :--- | :--- | :--- |
| **GET** | `/deportista` | Obtener todos los perfiles | ✅ Sí |
| **POST** | `/deportista` | Registrar un nuevo perfil | ✅ Sí |
| **DELETE** | `/deportista/{id}` | Eliminar por ID | ✅ Sí |

> **Ejemplo Body (POST):**
> ```json
> {
>   "nombre": "Marc Márquez",
>   "email": "marquez@ejemplo.com",
>   "edad": 31
> }
> ```

### 🏃‍♂️ Sección: Entrenamientos
| Método | Endpoint | Acción | Autenticación |
| :--- | :--- | :--- | :--- |
| **GET** | `/entrenamiento` | Ver historial global | ✅ Sí |
| **POST** | `/entrenamiento` | Crear entreno (vía DTO) | ✅ Sí |
| **GET** | `/entrenamiento/deportista/{id}` | Filtrar por deportista | ✅ Sí |
| **POST** | `/entrenamiento/{id}/portada` | Subir imagen | ✅ Sí |
| **GET** | `/entrenamiento/{id}/portada` | Descargar imagen | ✅ Sí |
| **DELETE** | `/entrenamiento/{id}/portada` | Eliminar imagen | ✅ Sí |

> **Ejemplo Body (POST):**
> ```json
> {
>   "fecha": "2026-02-25",
>   "distancia": 10.5,
>   "TiempoMinutos": 45,
>   "disciplina": {
>     "nombre": "Running"
>   },
>   "deportistaId": "ID_GENERADO_POR_MONGO"
> }
> ```

---

## 🏗️ Lógica de Negocio Destacada

1. **Autenticación JWT:** Login devuelve un token que debe incluirse en todas las peticiones protegidas.
2. **Uso de DTOs:** Se utiliza `EntrenamientoDTO` (Java Record) para desacoplar la API de la base de datos.
3. **Relaciones en Mongo:** Los entrenamientos se vinculan a los deportistas mediante `@DocumentReference`.
4. **Validaciones:** Se implementan restricciones como `@NotBlank`, `@NotNull` y `@Valid`.
5. **Encriptación de contraseñas:** Las contraseñas se guardan encriptadas con BCrypt.

---

## ⚙️ Configuración del Entorno

### Backend
1. Asegúrate de que MongoDB está corriendo en `localhost:27017`
2. Navega a la carpeta `Acceso-a-datos/`
3. Ejecuta: `mvn spring-boot:run`
4. El servidor estará disponible en `http://localhost:8080`

### Frontend (Vite/Vue)
El frontend debe estar en `http://localhost:5173` (CORS habilitado).

**Configuración en `application.properties`:**
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/App_entrenamiento
jwt.secret=MySecretKeyForJWTTokenGenerationWithMinimum256BitsForHSAlgorithmAndVeryLongStringValue
jwt.expiration=86400000
```

---

## 🔄 Flujo de Autenticación en el Frontend

1. Usuario accede a `/login`
2. Introduce credenciales
3. Frontend llama a `POST /auth/login`
4. Backend retorna token + datos
5. Frontend guarda token en localStorage
6. Navbar actualiza para mostrar nombre del usuario
7. En peticiones futuras, incluir header `Authorization: Bearer <token>`
8. Logout: eliminar token y redirigir a `/login`

---

## 📚 Documentación Disponible

| Documento | Descripción |
|-----------|-------------|
| **AGENTS.md** | Guía para agentes de IA con arquitectura y patrones |
| **FRONTEND_AUTH_GUIDE.md** | Ejemplos completos de implementación en Vue.js |
| **EXECUTION_GUIDE.md** | Instrucciones paso a paso para ejecutar el proyecto |
| **SAMPLE_DATA.json** | Datos de ejemplo para testing |

---

## ✨ Características Implementadas

✅ Autenticación JWT completa
✅ Registro de usuarios con validación
✅ Encriptación BCrypt de contraseñas
✅ Rutas protegidas por token
✅ CORS habilitado para frontend
✅ Gestor de imágenes
✅ Export/Import JSON
✅ Filtro JWT automático

---

## 🚀 Inicio Rápido

```bash
# 1. Asegúrate de que MongoDB está corriendo
# 2. Navega a Acceso-a-datos/
cd Acceso-a-datos

# 3. Compila e instala
./mvnw clean install

# 4. Ejecuta el servidor
./mvnw spring-boot:run

# 5. El servidor está disponible en http://localhost:8080
```

Para el frontend, sigue la guía en **FRONTEND_AUTH_GUIDE.md**.


