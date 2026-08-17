import { z } from 'zod';
import { getFirestore, hayFirestore } from '../config/firebaseAdmin.js';

const activarPremiumSchema = z.object({
  puntos: z.number().int().min(5).max(100),
});

const MAX_SAFE_INTEGER = 9007199254740991;

/**
 * Activa el premium de un usuario de forma segura.
 *
 * El cliente Android la invoca tras completar la compra en Google Play.
 * Solo el backend (Admin SDK) puede escribir los campos sensibles.
 *
 * TODO (producción): verificar el purchaseToken contra la Google Play
 * Developer API ANTES de activar, para confirmar que el pago es real.
 */
export async function activarPremium(req, res, next) {
  try {
    if (!hayFirestore()) {
      return res.status(503).json({ error: 'Servicio no disponible' });
    }

    const data = activarPremiumSchema.parse(req.body);
    const uid = req.userId;

    const db = getFirestore();
    const userRef = db.collection('usuarios').doc(uid);

    const resultado = { esPrimeraCompra: false, puntosAcumulados: 0 };

    await db.runTransaction(async (tx) => {
      const doc = await tx.get(userRef);
      if (!doc.exists) {
        throw new Error('Usuario no encontrado');
      }

      const apoyoBeta = doc.get('apoyoBeta') === true;
      const puntosActuales = Number(doc.get('puntosApoyo') || 0);

      const updates = {
        puntosApoyo: puntosActuales + data.puntos,
      };

      if (!apoyoBeta) {
        updates.esPremium = true;
        updates.premiumVitalicio = true;
        updates.premiumHasta = MAX_SAFE_INTEGER;
        updates.apoyoBeta = true;
        resultado.esPrimeraCompra = true;
      }

      resultado.puntosAcumulados = puntosActuales + data.puntos;
      tx.update(userRef, updates);
    });

    res.json(resultado);
  } catch (err) {
    next(err);
  }
}
