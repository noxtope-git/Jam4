# Backend en Render — Despliegue (finalizado)

El backend de Jam! está desplegado en Render y funcionando.

- **URL de producción:** `https://jam-backend-v0ch.onrender.com`
- **Health check:** `https://jam-backend-v0ch.onrender.com/api/health` → `{"status":"ok"}`
- **Blueprint:** `Jam!` (rama `master`, archivo `render.yaml`)
- **Servicio:** `jam-backend` (Node.js, plan gratis, región `oregon`, `rootDir: backend`)

## Estado actual

| Item | Estado |
|---|---|
| Deploy del backend | ✅ LIVE |
| Credenciales Firebase Admin (`jam-508302`) | ✅ Configuradas en Render |
| Firestore (premium + notificaciones) | ✅ Funcionando |
| `trust proxy` para rate-limiting | ✅ Configurado en `backend/src/app.js` |
| `BACKEND_URL` (release Android) | ✅ `https://jam-backend-v0ch.onrender.com` |

## Archivos clave

- `render.yaml` — Blueprint de Render (define `jam-backend`, `rootDir: backend`).
  Las credenciales de Firebase **no** están en el blueprint; se configuran manualmente
  en el dashboard (ver abajo).
- `backend/src/app.js` — tiene `app.set('trust proxy', 1)` (obligatorio para
  `express-rate-limit` detrás del proxy de Render; sin esto los endpoints con rate-limit
  devolvían 500).
- `backend/src/config/firebaseAdmin.js` — inicializa Firebase Admin si están las 3 variables
  de entorno. Si faltan, la app arranca igual pero premium/notificaciones quedan deshabilitados.
- `app/build.gradle.kts` — `BACKEND_URL` de release apunta al backend de Render.

## Credenciales de Firebase (ya configuradas)

Se generaron en Firebase Console → proyecto `jam-508302` → Cuentas de servicio →
**Generar nueva clave privada**, y se cargaron en Render → `jam-backend` → Environment:

| Variable | Valor (del JSON de la service account) |
|---|---|
| `FIREBASE_PROJECT_ID` | `project_id` |
| `FIREBASE_CLIENT_EMAIL` | `client_email` |
| `FIREBASE_PRIVATE_KEY` | `private_key` completo (con saltos de línea `\n`) |

El código hace `privateKey.replace(/\\n/g, '\n')`, así que pegá la clave **en una sola línea**
con los `\n` literales (tal como viene en `backend/.env`).

## Cómo redeployar (si cambiás el backend)

`render.yaml` tiene `autoDeploy: yes`, así que cualquier push a `master` redeploya solo.
También podés usar **Manual sync** desde el Blueprint en el dashboard de Render.

## Verificación

```bash
# Health check
curl https://jam-backend-v0ch.onrender.com/api/health

# Premium (Firebase Admin activo → debe responder 401 "Token inválido o expirado" con token dummy)
curl -X POST https://jam-backend-v0ch.onrender.com/api/premium/activar \
  -H "Authorization: Bearer token-dummy" -H "Content-Type: application/json" \
  -d '{"puntos":5}'
```

## Nota sobre PostgreSQL

Para **premium y notificaciones NO se necesita PostgreSQL** (usan Firebase/Firestore directamente).
PostgreSQL solo sería necesario para la app web (registro/login web). Si más adelante querés la web,
agregá un servicio PostgreSQL en Render (tier gratis de 90 días).

## Troubleshooting

- **Backend "cold start"**: Render apaga el servicio gratis tras ~15 min sin uso. La primera
  petición tarda ~50s en responder. Es normal en el plan gratis.
- **Premium no se activa**: revisá que las 3 variables de Firebase estén bien (sobre todo
  `FIREBASE_PRIVATE_KEY` con sus saltos de línea `\n`).
- **Error 500 en endpoints con rate-limit**: confirmá que `app.set('trust proxy', 1)` siga en
  `backend/src/app.js` (Render agrega el header `X-Forwarded-For` y `express-rate-limit` lanza
  `ERR_ERL_UNEXPECTED_X_FORWARDED_FOR` si no está configurado).
