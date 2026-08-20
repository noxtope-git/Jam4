import { z } from 'zod';
import admin, { getFirestore, hayFirestore } from '../config/firebaseAdmin.js';

const enviarSchema = z.object({
  uid: z.string().min(1, 'uid requerido'),
  titulo: z.string().min(1, 'título requerido'),
  cuerpo: z.string().min(1, 'cuerpo requerido'),
});

/**
 * Envía una notificación push a un usuario por su FCM token.
 * El token se lee de Firestore (campo "fcmToken" del usuario).
 */
export async function enviarNotificacion(req, res, next) {
  try {
    if (!hayFirestore()) {
      return res.status(503).json({ error: 'Servicio no disponible' });
    }

    const data = enviarSchema.parse(req.body);
    const db = getFirestore();

    const userDoc = await db.collection('usuarios').doc(data.uid).get();
    const token = userDoc.get('fcmToken');
    if (!token) {
      return res.status(404).json({ error: 'El usuario no tiene token de notificaciones' });
    }

    await admin.messaging().send({
      token,
      notification: {
        title: data.titulo,
        body: data.cuerpo,
      },
    });

    res.json({ ok: true });
  } catch (err) {
    next(err);
  }
}
