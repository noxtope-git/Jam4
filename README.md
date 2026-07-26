# Jam!

Red social para crear y encontrar eventos sociales temporales ("Jams") cerca de ti.

## 🚀 Estado actual

Prototipo funcional nativo Android (Kotlin + Jetpack Compose) con Firebase como backend.

### Features implementadas
- Creación y búsqueda de Jams por ubicación y etiquetas
- Chat grupal con fotos y búsqueda de mensajes
- Chat 1-a-1 entre usuarios
- Sistema de seguidores, perfil público, bloqueos
- Feed algoritmo con filtro por país + distancia + tags
- Pull-to-refresh estilo Instagram
- Personalización de perfil (colores, banner, foto)
- Sistema de solicitudes para unirse a Jams
- Historial de Jams
- Login y registro con verificación de identidad
- Modo oscuro/claro

### Monetización / Comunidad
- **Beta Supporter**: compra de puntos de apoyo vía Google Play Billing
- Premium vitalicio + insignia ✚ en el perfil al apoyar
- Ranking público de donantes con montos acumulados
- Límite semanal de 10 Jams para usuarios gratuitos
- Sin publicidad para supporters

### Tech stack actual
- **Frontend**: Kotlin, Jetpack Compose, Material 3
- **Backend**: Firebase (Auth, Firestore, Storage)
- **Pagos**: Google Play Billing 7.1.0
- **Mapas**: OpenStreetMap (osmdroid)

## 🗺️ Hoja de ruta

### Fase 2 — Web App (próximo)
Migrar Jam! a una plataforma web completa con stack moderno:

- **Frontend**: React.js + Tailwind CSS (UI más fluida y bonita)
- **Backend**: Node.js / Python (FastAPI) con API REST
- **Base de datos**: PostgreSQL + Redis (caché)
- **Tiempo real**: WebSockets para chat y notificaciones
- **Autenticación**: JWT + OAuth2

### Fase 3 — Ecosistema
- App móvil con React Native (comparte lógica con web)
- Panel de administración web
- Versión desktop (Electron/Tauri)
- API pública para integraciones de terceros

## 🔧 Build local

```bash
# Android
./gradlew assembleDebug        # debug APK
./gradlew assembleRelease       # release firmado (requiere keystore + env vars)
./gradlew bundleRelease         # App Bundle para Google Play
```

### Variables de entorno para release
```powershell
$env:JAM_STORE_PASSWORD = "tu_password"
$env:JAM_KEY_PASSWORD = "tu_password"
$env:JAM_KEY_ALIAS = "jam"
```

## 👥 Equipo

- **noxtope-git** — desarrollador principal
- **pintoduoc** — colaborador
