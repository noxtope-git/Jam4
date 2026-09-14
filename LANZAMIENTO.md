# 🚀 LANZAMIENTO — Jam!

Estado y pasos para publicar la app en Google Play.

> Última actualización: 14-sep-2026

---

## 1. Estado actual — todo listo y testeado

| Componente | Estado |
|---|---|
| Backend (Render) | ✅ LIVE — `https://jam-backend-v0ch.onrender.com` |
| Firebase `jam-508302` | ✅ Configurado (Admin + Firestore + Hosting) |
| Proveedor Email/Contraseña (Auth) | ✅ Habilitado |
| App Android | ✅ Testeada en emulador (login, feed, navegación) |
| AAB firmado | ✅ `PlayStore/app-release.aab` |
| Gráficos (icono, feature graphic, capturas) | ✅ en `PlayStore/` |
| Links de privacidad / eliminación | ✅ vivos (`jam-508302.web.app`) |

**Carpeta de subida:** `PlayStore/` (AAB + gráficos + `CHECKLIST.md`)

---

## 2. Costos — nada está gastando dinero ahora

| Recurso | Plan | Costo |
|---|---|---|
| Render (backend) | **Free** | $0. Se suspende solo tras 15 min sin uso. |
| Firebase Hosting | Tier gratuito (10 GB / 360 MB por día) | $0 (3 archivos estáticos) |
| Firebase `jam-508302` | **Blaze** (billing habilitado) | $0 con 0 usuarios. Todo dentro del tier gratis. |

> ⚠️ **Único punto de atención:** el proyecto Firebase está en plan **Blaze**
> (tiene billing habilitado). Con 0 usuarios no genera costo, pero si querés
> **cero riesgo**, podés bajarlo a **Spark (gratis)** en:
> Firebase Console → ⚙️ Configuración → Uso y facturación → "Cambiar a Spark".
> No usás Cloud Functions (el backend está en Render), así que Spark alcanza.

---

## 3. Pasos para publicar (cuando aprueben tus credenciales de Play)

1. **Subir el AAB**: Play Console → Producción → Crear versión → subir `PlayStore/app-release.aab`.
2. **Ficha de Play Store**: pegar nombre, descripciones, categoría y tags (están en `PlayStore/CHECKLIST.md`).
3. **Gráficos**: icono (`icono-app-512x512.png`), feature graphic (`feature-graphic-1024x500.png`) y capturas (`PlayStore/screenshots/`).
4. **URLs**: política de privacidad y eliminación de cuenta (ya vivos, ver `CHECKLIST.md`).
5. **Clasificación de contenido + Seguridad de datos**: contestar los cuestionarios.
6. **Compras dentro de la app (Billing)**: crear el producto de Premium en Play Console → Monetización, para que la compra de Premium funcione en producción.
7. **Enviar a revisión**.

---

## 4. Después del lanzamiento

- [ ] Guardar `mapping.txt` (ya está en `PlayStore/`) para desofuscar crashes en Crashlytics.
- [ ] Borrar la cuenta de prueba `prueba@jamapp.com` / `JamPrueba123` de Firebase Auth (opcional).
- [ ] Monitorear Crashlytics + Analytics los primeros días.
- [ ] Responder reseñas y reportes desde Play Console.

---

## 5. Checklist rápido

- [x] Backend desplegado y funcional
- [x] Firebase configurado (Auth + Firestore + Hosting)
- [x] App testeada en emulador
- [x] AAB firmado con `BACKEND_URL` correcta
- [x] Gráficos listos
- [x] Links de privacidad/eliminación vivos
- [ ] Subir AAB a Play Console
- [ ] Completar ficha + gráficos + clasificación + data safety
- [ ] Crear producto de Billing (Premium)
- [ ] Enviar a revisión
