const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

const MAX_SAFE_INTEGER = 9007199254740991; // Number.MAX_SAFE_INTEGER

/**
 * Activa el premium de un usuario de forma segura.
 *
 * Solo el backend (Admin SDK) puede escribir los campos sensibles
 * esPremium, premiumVitalicio, premiumHasta, apoyoBeta y puntosApoyo.
 * El cliente Android la invoca tras completar la compra en Google Play.
 *
 * TODO (producción): verificar el purchaseToken contra la Google Play
 * Developer API ANTES de activar el premium, para confirmar que el pago
 * es real y corresponde al usuario autenticado.
 */
exports.activarPremium = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Debes iniciar sesión');
  }

  const uid = context.auth.uid;
  const puntos = Number(data && data.puntos);

  if (!Number.isInteger(puntos) || puntos < 5 || puntos > 100) {
    throw new functions.https.HttpsError('invalid-argument', 'Cantidad de puntos inválida (5-100)');
  }

  const db = admin.firestore();
  const userRef = db.collection('usuarios').doc(uid);

  const resultado = { esPrimeraCompra: false, puntosAcumulados: 0 };

  await db.runTransaction(async (tx) => {
    const doc = await tx.get(userRef);
    if (!doc.exists) {
      throw new functions.https.HttpsError('not-found', 'Usuario no encontrado');
    }

    const apoyoBeta = doc.get('apoyoBeta') === true;
    const puntosActuales = Number(doc.get('puntosApoyo') || 0);

    const updates = {
      puntosApoyo: puntosActuales + puntos,
    };

    if (!apoyoBeta) {
      updates.esPremium = true;
      updates.premiumVitalicio = true;
      updates.premiumHasta = MAX_SAFE_INTEGER;
      updates.apoyoBeta = true;
      resultado.esPrimeraCompra = true;
    }

    resultado.puntosAcumulados = puntosActuales + puntos;
    tx.update(userRef, updates);
  });

  return resultado;
});
