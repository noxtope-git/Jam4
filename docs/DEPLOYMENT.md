# Desplegar el backend en Render (gratis, sin tarjeta)

Tu backend Node ya está listo en el repo. El archivo `render.yaml` (Blueprint) ya está
configurado para que el deploy sea de un solo paso.

## Requisitos previos

- Tu repo ya está en GitHub (https://github.com/noxtope-git/Jam4) ✅
- Cuenta de Render (crear en https://render.com con "Sign up with GitHub")

## Paso 1 — Crear cuenta de Render

1. Entrá a https://render.com
2. Click en **Sign up** → elegí **GitHub** (tu cuenta `noxtope-git`)
3. Confirmá el email

## Paso 2 — Deploy del backend (Blueprint)

1. En el dashboard de Render: **New +** → **Blueprint**
2. Conectá tu repositorio `noxtope-git/Jam4`
3. Render lee el `render.yaml` y detecta el servicio `jam-backend` automáticamente
4. Click en **Apply** / **Deploy**

El backend se despliega en unos minutos. Render te da una URL tipo:
`https://jam-backend.onrender.com`

## Paso 3 — Configurar las credenciales de Firebase (clave para premium + notificaciones)

El premium y las notificaciones usan Firebase Admin, que necesita credenciales.

### 3.1 Generar la service account key

1. Entrá a https://console.firebase.google.com/project/jam-64d1b/settings/serviceaccounts/adminsdk
2. Click en **Generar nueva clave privada** → descargá el JSON

### 3.2 Agregarlas a Render

En el dashboard de Render → `jam-backend` → **Environment**, agregá estas variables:

| Variable | Valor (del JSON descargado) |
|---|---|
| `FIREBASE_PROJECT_ID` | El campo `project_id` |
| `FIREBASE_CLIENT_EMAIL` | El campo `client_email` |
| `FIREBASE_PRIVATE_KEY` | El campo `private_key` (todo el bloque, incluyendo `-----BEGIN...-----`) |

Luego click **Save Changes** (Render re-deploya automáticamente).

## Paso 4 — Apuntar la app Android al backend

En `app/src/main/java/com/noxtope/jam/ui/theme/UserViewModel.kt` → `BuildConfig.BACKEND_URL`:

```kotlin
// En app/build.gradle.kts, dentro de buildTypes.release:
buildConfigField("String", "BACKEND_URL", "\"https://jam-backend.onrender.com\"")
```

Recompilar el release y listo.

## Verificar que funciona

1. Abrí `https://jam-backend.onrender.com/api/health` → debería devolver `{"status":"ok",...}`
2. Con la app: Compra premium en Comunidad → debería activarse (el backend escribe en Firestore)

## Nota sobre PostgreSQL

Para **premium y notificaciones NO se necesita PostgreSQL** (usan Firebase/Firestore directamente).
PostgreSQL solo es necesario para la app web (registro/login web). Si más adelante querés la web,
agregá un servicio PostgreSQL en Render (tiene tier gratis de 90 días).

## Troubleshooting

- **Backend "cold start"**: Render apaga el servicio gratis tras 15 min sin uso. La primera
  petición tarda ~50s en responder. Es normal en el plan gratis.
- **Premium no se activa**: revisá que las 3 variables de Firebase estén bien (sobre todo
  `FIREBASE_PRIVATE_KEY` con sus saltos de línea `\n`).
