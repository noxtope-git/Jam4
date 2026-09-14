# 🚀 Checklist de Lanzamiento — Jam!

> Guía paso a paso para publicar la app en Google Play.
> Todo el código ya está listo, testeado y desplegado. Esto es lo que queda de tu lado.

---

## ✅ Ya está hecho (no tenés que hacer nada de esto)

| Tarea | Estado |
|---|---|
| Backend en Render | ✅ LIVE → `https://jam-backend-v0ch.onrender.com` |
| Firebase `jam-508302` (credenciales + Firestore) | ✅ Configurado |
| Proveedor Email/Contraseña (Firebase Auth) | ✅ Habilitado |
| `trust proxy` (rate-limiting) | ✅ Arreglado |
| App testeada en emulador (login, feed, navegación) | ✅ Funciona |
| AAB firmado con la URL correcta del backend | ✅ `PlayStore/app-release.aab` |
| Gráficos (icono, feature graphic, capturas) | ✅ En `PlayStore/` |
| Firebase Hosting (privacidad + eliminación) | ✅ Desplegado en `jam-508302.web.app` |

---

## 💸 Costos — nada está gastando dinero

| Recurso | Plan | Costo |
|---|---|---|
| Render (backend) | Free | $0 — se suspende solo tras ~15 min sin uso |
| Firebase Hosting | Tier gratuito | $0 — 3 archivos estáticos, muy por debajo del límite |
| Firebase `jam-508302` | Blaze (billing habilitado) | $0 con 0 usuarios — todo dentro del tier gratis |

> ⚠️ **Único punto de atención:** el proyecto Firebase está en plan **Blaze** (billing
> activo). Con 0 usuarios no genera costo. Si querés **cero riesgo**, bajalo a **Spark**:
> Firebase Console → ⚙️ Configuración → Uso y facturación → "Cambiar a Spark".
> No usás Cloud Functions (el backend está en Render), así que Spark alcanza de sobra.

---

## Antes de subir (5 min)

### 1. Firebase Hosting ✅ (ya desplegado)
Los links ya están vivos y responden 200:
- https://jam-508302.web.app/privacy-policy.html
- https://jam-508302.web.app/account-deletion.html

Si algún día necesitás redesplegar:
```bash
firebase deploy --only hosting
```

### 2. Revisar los gráficos
Abrí la carpeta `PlayStore/` y mirá:
- `feature-graphic-1024x500.png` (el que generé — chequeá que te guste)
- `icono-app-512x512.png`
- `screenshots/` (5 capturas)

Si querés cambiar el feature graphic, avisame y lo ajusto.

### 3. Limpiar la cuenta de prueba (opcional, recomendado)
Creé `prueba@jamapp.com` / `JamPrueba123` para testear. Borrala antes del lanzamiento real:
- Firebase Console → jam-508302 → Authentication → Users → buscá `prueba@jamapp.com` → eliminar.

---

## Google Play Console — paso a paso

### 4. Subir el App Bundle
1. Play Console → tu app (`com.noxtope.jam`) → **Producción** → **Crear versión**.
2. Subí `PlayStore/app-release.aab`.
3. Guardá `PlayStore/mapping.txt` en un lugar seguro (desofuscación de crashes).

### 5. Ficha de Play Store
| Campo | Valor |
|---|---|
| Nombre | Jam! |
| Descripción corta (≤80) | Encuentra y crea eventos sociales cerca de ti. Conecta, comparte y crea. |
| Descripción completa | Ver abajo ↓ |
| Categoría | Social |
| Etiquetas | social, eventos, fiestas, quedadas, conocer gente |
| Email de contacto | contacto@jam-app.com |

**Descripción completa:**
```
Jam! es la app para encontrar y crear eventos sociales (Jams) cerca de ti.

¿Qué puedes hacer?
• Crear Jams: organiza eventos con título, descripción, ubicación y foto.
• Descubrir: el feed te muestra Jams de tu país, a menos de 10 km, ordenadas por tus intereses.
• Conectarte: sigue a personas, chatea en grupo o en privado, y comparte fotos.
• Personalizar: elige el color de tu perfil, modo oscuro y etiquetas de intereses.
• Apoyar el proyecto: conviértete en Beta Supporter ✚ y obtén Premium vitalicio (Jams ilimitadas, hasta 150 participantes y sin publicidad).

Todo en un solo lugar, con una identidad verificada y una comunidad segura.
```

### 6. Gráficos (Play Console → App → Gráficos)
- **Icono** → `PlayStore/icono-app-512x512.png`
- **Gráfico de funciones** → `PlayStore/feature-graphic-1024x500.png`
- **Capturas (teléfono)** → las 5 de `PlayStore/screenshots/`

### 7. URLs obligatorias (Play Console → App → Ficha → Privacidad)
- Política de privacidad → `https://jam-508302.web.app/privacy-policy.html`
- Eliminación de cuenta → `https://jam-508302.web.app/account-deletion.html`

### 8. Clasificación de contenido (Play Console → Políticas → Clasificación)
- App social con chat. Contestá según lo real:
  - Contenido generado por usuarios (UGC) → **Sí**
  - Interacciones de usuario → **Sí**
  - Compras dentro de la app → **Sí** (Premium)

### 9. Seguridad de datos (Play Console → Políticas → Seguridad de datos)
- Datos recopilados: email, identificadores, ubicación aproximada, fotos (chat).
- Cifrado en tránsito → **Sí** (Firebase usa HTTPS/TLS).
- Eliminación de cuenta → **Sí** (URL arriba).

### 10. Compras dentro de la app (Billing) ⚠️
El premium usa Google Play Billing. Antes del lanzamiento creá el producto en:
- Play Console → **Productos** → **Productos integrados** (o suscripciones).
- El flujo de "Apoyar con $X USD" debe mapear a ese producto (verificá con `CREADOR_EMAIL` si hay lógica pendiente).

### 11. Enviar a revisión
1. Completá todo lo anterior.
2. Configurá precio/países (gratis si corresponde).
3. **Enviar a revisión** (puede tardar horas o días).

---

## Verificación rápida antes de publicar

- [x] `firebase deploy --only hosting` corrió sin errores
- [x] Los 2 links de privacidad responden 200
- [ ] `app-release.aab` subido y sin errores de firma
- [ ] Icono + feature graphic + capturas cargadas
- [ ] Cuenta de prueba eliminada de Firebase
- [ ] Clasificación de contenido y data safety completados
- [ ] Producto de Billing creado (si usás premium)

---

## URLs y datos de referencia

| Qué | Valor |
|---|---|
| Backend | `https://jam-backend-v0ch.onrender.com` |
| Firebase project | `jam-508302` |
| Package | `com.noxtope.jam` |
| versionCode / versionName | `1` / `1.0.0` |
| Keystore | `jam-release.keystore` (alias `jam`) |
