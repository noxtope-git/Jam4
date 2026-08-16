# Jam!

Red social para crear y encontrar eventos sociales temporales ("Jams") cerca de ti.

## 🚀 Estado actual

Monorepo con dos proyectos:
- **`app/`** — Prototipo nativo Android (Kotlin + Jetpack Compose + Firebase)
- **`backend/`** — API REST (Node.js + Express + PostgreSQL)
- **`frontend/`** — Web app (React + Vite + Tailwind CSS)

La migración a web está en fase 1: **scaffold + autenticación** (registro/login con JWT).

## 📁 Estructura del proyecto

```
Jam4/
├── app/                 # App Android (prototipo)
├── backend/             # API REST Node.js + Express
│   ├── src/
│   │   ├── config/      # Conexión a PostgreSQL
│   │   ├── controllers/ # Lógica de negocio (auth)
│   │   ├── db/          # Esquema SQL + migraciones
│   │   ├── middleware/  # Auth JWT, manejo de errores
│   │   ├── routes/      # Rutas API
│   │   └── index.js     # Punto de entrada
│   └── Dockerfile
├── frontend/            # Web app React + Vite + Tailwind
│   └── src/
│       ├── api/         # Cliente Axios
│       ├── context/     # AuthContext
│       └── pages/       # Login, Register, Home
├── docker-compose.yml   # PostgreSQL + backend
└── README.md
```

## 🛠️ Tech stack

### Backend
- **Node.js 22** + **Express 4**
- **PostgreSQL 16** (con `pg`)
- **JWT** para autenticación
- **bcrypt** para hashing de contraseñas
- **Zod** para validación de entrada
- **helmet**, **CORS**, **rate-limiting** para seguridad

### Frontend
- **React 18** + **TypeScript**
- **Vite 5**
- **Tailwind CSS 3**
- **React Router** + **Axios**

## 🔐 Seguridad implementada

- Contraseñas hasheadas con bcrypt (10 rounds)
- JWT con expiración configurable
- Validación estricta de inputs con Zod
- Consultas SQL parametrizadas (prevención de SQL injection)
- Rate limiting en endpoints de auth
- Headers de seguridad (helmet)
- CORS configurable por origen
- Variables de entorno para secretos (nunca en código)

## 🚀 Cómo levantar el proyecto

### Requisitos
- Node.js 22+
- Docker (para PostgreSQL) o PostgreSQL local
- npm

### 1. Levantar PostgreSQL con Docker

```bash
docker compose up -d db
```

### 2. Backend

```bash
cd backend
cp .env.example .env        # editar JWT_SECRET
npm install
npm run migrate             # crear tablas
npm run dev                 # http://localhost:4000
```

### 3. Frontend

```bash
cd frontend
npm install
npm run dev                 # http://localhost:5173
```

### Levantar todo con Docker (opcional)

```bash
docker compose up -d        # db + backend
```

## 📡 API Endpoints

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| GET | `/api/health` | Health check | No |
| POST | `/api/auth/registro` | Crear cuenta | No |
| POST | `/api/auth/login` | Iniciar sesión | No |
| GET | `/api/auth/me` | Perfil del usuario actual | Sí |

## 🗺️ Hoja de ruta

### Fase 1 — Scaffold + Auth ✅
- [x] Backend Express + PostgreSQL
- [x] Frontend React + Vite + Tailwind
- [x] Registro/login con JWT + bcrypt

### Fase 2 — Core (Jams)
- [ ] CRUD de Jams (crear, buscar, unirse)
- [ ] Feed por ubicación + etiquetas
- [ ] Perfiles de usuario

### Fase 3 — Tiempo real
- [ ] Chat con WebSockets (Socket.io)
- [ ] Notificaciones en tiempo real

### Fase 4 — Monetización
- [ ] Sistema de puntos de apoyo
- [ ] Ranking de donantes
- [ ] Beta Supporter (insignia ✚)

### Fase 5 — Ecosistema
- [ ] React Native (comparte lógica con web)
- [ ] Panel de administración
- [ ] API pública

## 👥 Equipo

- **noxtope-git** — desarrollador principal
- **pintoduoc** — colaborador
