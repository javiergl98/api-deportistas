# 🎯 Guía de Implementación Frontend - Autenticación JWT

Este documento proporciona ejemplos y patrones para implementar autenticación JWT en el frontend (Vite + Vue).

---

## 📦 Instalación de Dependencias

```bash
npm install axios
npm install vue-router
```

---

## 🔑 Configuración de Axios (Interceptor)

Crea el archivo `src/api/axiosConfig.js`:

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor para añadir token en cada petición
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para manejar errores de autenticación
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expirado o inválido
      localStorage.removeItem('authToken');
      localStorage.removeItem('username');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

---

## 👤 Store/Estado de Autenticación

Crea `src/stores/authStore.js`:

```javascript
import { ref, computed } from 'vue';
import api from '@/api/axiosConfig';

export const useAuthStore = () => {
  const username = ref(localStorage.getItem('username') || '');
  const nombre = ref(localStorage.getItem('nombre') || '');
  const email = ref(localStorage.getItem('email') || '');
  const token = ref(localStorage.getItem('authToken') || '');

  const isAuthenticated = computed(() => !!token.value);

  const login = async (username_input, password) => {
    try {
      const response = await api.post('/auth/login', {
        username: username_input,
        password: password
      });

      const { token: newToken, username: newUsername, nombre: newNombre, email: newEmail } = response.data;
      
      // Guardar en localStorage
      localStorage.setItem('authToken', newToken);
      localStorage.setItem('username', newUsername);
      localStorage.setItem('nombre', newNombre);
      localStorage.setItem('email', newEmail);

      // Actualizar estado
      token.value = newToken;
      username.value = newUsername;
      nombre.value = newNombre;
      email.value = newEmail;

      return true;
    } catch (error) {
      console.error('Error en login:', error);
      return false;
    }
  };

  const register = async (userData) => {
    try {
      const response = await api.post('/auth/register', userData);

      const { token: newToken, username: newUsername, nombre: newNombre, email: newEmail } = response.data;
      
      localStorage.setItem('authToken', newToken);
      localStorage.setItem('username', newUsername);
      localStorage.setItem('nombre', newNombre);
      localStorage.setItem('email', newEmail);

      token.value = newToken;
      username.value = newUsername;
      nombre.value = newNombre;
      email.value = newEmail;

      return true;
    } catch (error) {
      console.error('Error en registro:', error);
      return false;
    }
  };

  const logout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    localStorage.removeItem('nombre');
    localStorage.removeItem('email');

    token.value = '';
    username.value = '';
    nombre.value = '';
    email.value = '';
  };

  const getCurrentUser = async () => {
    try {
      const response = await api.get('/auth/me');
      return response.data;
    } catch (error) {
      console.error('Error obteniendo usuario actual:', error);
      return null;
    }
  };

  return {
    username,
    nombre,
    email,
    token,
    isAuthenticated,
    login,
    register,
    logout,
    getCurrentUser
  };
};
```

---

## 🔐 Página de Login

Crea `src/pages/Login.vue`:

```vue
<template>
  <div class="login-container">
    <div class="login-box">
      <h1>🏋️ Inicia Sesión</h1>
      
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">Usuario</label>
          <input
            v-model="form.username"
            type="text"
            id="username"
            placeholder="Tu usuario"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">Contraseña</label>
          <input
            v-model="form.password"
            type="password"
            id="password"
            placeholder="Tu contraseña"
            required
          />
        </div>

        <button type="submit" :disabled="loading" class="btn-login">
          {{ loading ? 'Cargando...' : 'Inicia Sesión' }}
        </button>

        <p v-if="error" class="error">{{ error }}</p>
      </form>

      <p class="register-link">
        ¿No tienes cuenta? <router-link to="/register">Regístrate aquí</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const authStore = useAuthStore();

const form = ref({
  username: '',
  password: ''
});

const loading = ref(false);
const error = ref('');

const handleLogin = async () => {
  loading.value = true;
  error.value = '';

  const success = await authStore.login(form.username, form.password);

  if (success) {
    router.push('/');
  } else {
    error.value = 'Usuario o contraseña incorrectos';
  }

  loading.value = false;
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

h1 {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.btn-login {
  width: 100%;
  padding: 0.75rem;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-login:hover:not(:disabled) {
  background: #5568d3;
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  color: #e74c3c;
  text-align: center;
  margin-top: 1rem;
}

.register-link {
  text-align: center;
  margin-top: 1.5rem;
  color: #666;
}

.register-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>
```

---

## 📝 Página de Registro

Crea `src/pages/Register.vue`:

```vue
<template>
  <div class="register-container">
    <div class="register-box">
      <h1>📋 Crear Cuenta</h1>
      
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">Usuario</label>
          <input
            v-model="form.username"
            type="text"
            id="username"
            placeholder="Elige un usuario"
            required
          />
        </div>

        <div class="form-group">
          <label for="email">Email</label>
          <input
            v-model="form.email"
            type="email"
            id="email"
            placeholder="tu@email.com"
            required
          />
        </div>

        <div class="form-group">
          <label for="nombre">Nombre Completo</label>
          <input
            v-model="form.nombre"
            type="text"
            id="nombre"
            placeholder="Tu nombre"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">Contraseña</label>
          <input
            v-model="form.password"
            type="password"
            id="password"
            placeholder="Mínimo 6 caracteres"
            required
          />
        </div>

        <button type="submit" :disabled="loading" class="btn-register">
          {{ loading ? 'Registrando...' : 'Crear Cuenta' }}
        </button>

        <p v-if="error" class="error">{{ error }}</p>
      </form>

      <p class="login-link">
        ¿Ya tienes cuenta? <router-link to="/login">Inicia sesión aquí</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const authStore = useAuthStore();

const form = ref({
  username: '',
  email: '',
  nombre: '',
  password: ''
});

const loading = ref(false);
const error = ref('');

const handleRegister = async () => {
  loading.value = true;
  error.value = '';

  const success = await authStore.register(form.value);

  if (success) {
    router.push('/');
  } else {
    error.value = 'Error en el registro. Intenta de nuevo.';
  }

  loading.value = false;
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

h1 {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.btn-register {
  width: 100%;
  padding: 0.75rem;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-register:hover:not(:disabled) {
  background: #5568d3;
}

.btn-register:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  color: #e74c3c;
  text-align: center;
  margin-top: 1rem;
}

.login-link {
  text-align: center;
  margin-top: 1.5rem;
  color: #666;
}

.login-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
```

---

## 🧭 Navbar con Estado de Autenticación

Crea `src/components/Navbar.vue`:

```vue
<template>
  <nav class="navbar">
    <div class="navbar-container">
      <router-link to="/" class="navbar-brand">
        🏋️ SportApp
      </router-link>

      <ul class="nav-links" v-if="authStore.isAuthenticated">
        <li><router-link to="/">Inicio</router-link></li>
        <li><router-link to="/deportistas">Deportistas</router-link></li>
        <li><router-link to="/entrenamientos">Entrenamientos</router-link></li>
      </ul>

      <div class="nav-auth">
        <template v-if="authStore.isAuthenticated">
          <span class="user-info">👤 {{ authStore.nombre }}</span>
          <button @click="handleLogout" class="btn-logout">
            Cerrar Sesión
          </button>
        </template>
        <template v-else>
          <router-link to="/login" class="btn-login">Inicia Sesión</router-link>
          <router-link to="/register" class="btn-register">Regístrate</router-link>
        </template>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const authStore = useAuthStore();

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.navbar {
  background: #2c3e50;
  color: white;
  padding: 1rem 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.navbar-brand {
  font-size: 1.5rem;
  font-weight: 700;
  text-decoration: none;
  color: white;
}

.nav-links {
  display: flex;
  gap: 2rem;
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-links a {
  color: white;
  text-decoration: none;
  transition: color 0.2s;
}

.nav-links a:hover {
  color: #3498db;
}

.nav-auth {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-info {
  font-size: 0.9rem;
}

.btn-login,
.btn-register,
.btn-logout {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
  transition: background 0.2s;
  font-weight: 600;
}

.btn-login {
  color: white;
  text-decoration: none;
}

.btn-register {
  background: #3498db;
  color: white;
  text-decoration: none;
}

.btn-register:hover {
  background: #2980b9;
}

.btn-logout {
  background: #e74c3c;
  color: white;
}

.btn-logout:hover {
  background: #c0392b;
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }
}
</style>
```

---

## 🛣️ Configuración de Router

Crea `src/router/index.js`:

```javascript
import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

import Login from '@/pages/Login.vue';
import Register from '@/pages/Register.vue';
import Home from '@/pages/Home.vue';
import Deportistas from '@/pages/Deportistas.vue';
import Entrenamientos from '@/pages/Entrenamientos.vue';

const routes = [
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/', component: Home, meta: { requiresAuth: true } },
  { path: '/deportistas', component: Deportistas, meta: { requiresAuth: true } },
  { path: '/entrenamientos', component: Entrenamientos, meta: { requiresAuth: true } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// Guard para rutas protegidas
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login');
  } else if ((to.path === '/login' || to.path === '/register') && authStore.isAuthenticated) {
    next('/');
  } else {
    next();
  }
});

export default router;
```

---

## 💡 Ejemplo: Petición Autenticada

```javascript
// En cualquier componente
import { ref } from 'vue';
import api from '@/api/axiosConfig';
import { useAuthStore } from '@/stores/authStore';

export default {
  setup() {
    const authStore = useAuthStore();
    const deportistas = ref([]);

    const cargarDeportistas = async () => {
      try {
        const response = await api.get('/deportista');
        deportistas.value = response.data;
      } catch (error) {
        console.error('Error:', error);
      }
    };

    return {
      deportistas,
      cargarDeportistas
    };
  }
};
```

---

## 🔒 Seguridad - Puntos Clave

1. **Token en localStorage**: Se almacena automáticamente tras login
2. **Interceptor de Axios**: Añade el token a cada petición automáticamente
3. **Error 401**: Si el token expira, se borra y redirige a login
4. **Rutas protegidas**: Router guard redirige a login si no está autenticado
5. **Logout**: Limpia el localStorage y redirige a login


