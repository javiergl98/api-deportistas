# 🏋️‍♂️ API de Gestión Deportiva

Sistema de gestión de entrenamientos y deportistas desarrollado con **Spring Boot** y **MongoDB**. La API permite una gestión eficiente de actividades físicas mediante el uso de **DTOs** para la transferencia segura de datos.

---

## 🛠️ Tecnologías y Herramientas
* **Backend:** Spring Boot 3.2+
* **Base de Datos:** MongoDB (NoSQL)
* **Validación:** Jakarta Bean Validation
* **Mapeo:** Java Records (DTOs)
* **Pruebas:** Postman

---

## 🚦 Endpoints Principales

### 👤 Sección: Deportistas
| Método | Endpoint | Acción |
| :--- | :--- | :--- |
| **GET** | `/deportista` | Obtener todos los perfiles |
| **POST** | `/deportista` | Registrar un nuevo perfil |
| **DELETE** | `/deportista/{id}` | Eliminar por ID |

> **Ejemplo Body (POST):**
> ```json
> {
>   "nombre": "Marc Márquez",
>   "email": "marquez@ejemplo.com",
>   "nivel": "Profesional",
>   "edad": 31
> }
> ```

---

### 🏃‍♂️ Sección: Entrenamientos
| Método | Endpoint | Acción |
| :--- | :--- | :--- |
| **GET** | `/entrenamiento` | Ver historial global |
| **POST** | `/entrenamiento` | Crear entreno (vía DTO) |
| **GET** | `/entrenamiento/deportista/{id}` | Filtrar por deportista |

> **Ejemplo Body (POST):**
> ```json
> {
>   "fecha": "2026-02-25",
>   "distancia": 10.5,
>   "tiempoMinutos": 45,
>   "disciplina": {
>     "nombre": "Running"
>   },
>   "deportistaId": "ID_GENERADO_POR_MONGO"
> }
> ```

---

## 🏗️ Lógica de Negocio Destacada

1. **Uso de DTOs:** Se utiliza `EntrenamientoDTO` (Java Record) para desacoplar la API de la base de datos y validar los datos de entrada antes de procesarlos.
2. **Relaciones en Mongo:** Los entrenamientos se vinculan a los deportistas mediante `@DocumentReference`, optimizando la integridad referencial en un entorno NoSQL.
3. **Validaciones:** Se implementan restricciones como `@NotBlank`, `@NotNull` y `@Valid` para asegurar que no se guarden datos corruptos o incompletos.

---

## ⚙️ Configuración del Entorno

Para levantar el proyecto localmente, configura tu `application.properties`:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/tu_base_de_datos
spring.jackson.deserialization.fail-on-unknown-properties=false