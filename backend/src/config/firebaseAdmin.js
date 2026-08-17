import admin from 'firebase-admin';

// Configura Firebase Admin para poder escribir en Firestore
// ignorando las reglas de seguridad (equivalente a una Cloud Function,
// pero sin depender del plan Blaze).
//
// Requiere las variables de entorno FIREBASE_PROJECT_ID,
// FIREBASE_CLIENT_EMAIL y FIREBASE_PRIVATE_KEY (service account).
// Si no están definidas, la app arranca igual pero los endpoints
// de premium quedan deshabilitados.

let firestore = null;

const projectId = process.env.FIREBASE_PROJECT_ID;
const clientEmail = process.env.FIREBASE_CLIENT_EMAIL;
const privateKey = process.env.FIREBASE_PRIVATE_KEY;

if (projectId && clientEmail && privateKey) {
  admin.initializeApp({
    credential: admin.credential.cert({
      projectId,
      clientEmail,
      privateKey: privateKey.replace(/\\n/g, '\n'),
    }),
  });
  firestore = admin.firestore();
  console.log('✅ Firebase Admin inicializado (Firestore disponible)');
} else {
  console.warn('⚠️ Firebase Admin NO configurado. Los endpoints de premium están deshabilitados.');
  console.warn('   Define FIREBASE_PROJECT_ID, FIREBASE_CLIENT_EMAIL y FIREBASE_PRIVATE_KEY en .env');
}

export function hayFirestore() {
  return firestore !== null;
}

export function getFirestore() {
  return firestore;
}

export default admin;
