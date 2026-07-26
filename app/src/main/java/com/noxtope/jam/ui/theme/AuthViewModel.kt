package com.noxtope.jam.ui.theme

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun crearCuenta(
        email: String,
        contrasena: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, contrasena)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val nuevoUsuario = hashMapOf(
                        "uid" to uid,
                        "email" to email,
                        "datosPersonalesCompletos" to false,
                        "modoOscuro" to true,
                        "colorPrimario" to 0xFFFFFFFF.toLong(),
                        "colorSecundario" to 0xFF666666.toLong(),
                        "lucesActivas" to false
                    )
                    db.collection("usuarios").document(uid)
                        .set(nuevoUsuario)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e ->
                            onError(e.message ?: "Error al guardar usuario")
                        }
                } else {
                    onError(tarea.exception?.message ?: "Error desconocido")
                }
            }
    }

    fun iniciarSesion(
        email: String,
        contrasena: String,
        onResultado: (necesitaDatos: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, contrasena)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    verificarDatosPersonales(onResultado = onResultado, onError = onError)
                } else {
                    onError(tarea.exception?.message ?: "Correo o contraseña incorrectos")
                }
            }
    }

    fun iniciarSesionConGoogle(
        idToken: String,
        onResultado: (necesitaDatos: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    val user = auth.currentUser ?: return@addOnCompleteListener
                    val uid = user.uid

                    db.collection("usuarios").document(uid).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                val completos = doc.getBoolean("datosPersonalesCompletos") ?: false
                                onResultado(!completos)
                            } else {
                                val nuevoUsuario = hashMapOf(
                                    "uid" to uid,
                                    "email" to (user.email ?: ""),
                                    "datosPersonalesCompletos" to false,
                                    "modoOscuro" to true,
                                    "colorPrimario" to 0xFFFFFFFF.toLong(),
                                    "colorSecundario" to 0xFF666666.toLong(),
                                    "lucesActivas" to false
                                )
                                db.collection("usuarios").document(uid)
                                    .set(nuevoUsuario)
                                    .addOnSuccessListener { onResultado(true) }
                                    .addOnFailureListener { e ->
                                        onError(e.message ?: "Error al guardar")
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            onError(e.message ?: "Error al verificar usuario")
                        }
                } else {
                    onError(tarea.exception?.message ?: "Error con Google")
                }
            }
    }

    private fun verificarDatosPersonales(
        onResultado: (necesitaDatos: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onError("No hay sesión")
            return
        }
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val completos = doc.getBoolean("datosPersonalesCompletos") ?: false
                onResultado(!completos)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al verificar datos")
            }
    }

    fun eliminarCuenta(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            onError("No hay sesión activa")
            return
        }
        val uid = user.uid

        db.collection("usuarios").document(uid).delete()
            .addOnSuccessListener {
                db.collection("jams")
                    .whereEqualTo("creadoPor", uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        for (doc in snapshot.documents) {
                            doc.reference.delete()
                        }
                        user.delete()
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e ->
                                onError(
                                    e.message
                                        ?: "Vuelve a iniciar sesión para eliminar la cuenta"
                                )
                            }
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Error al borrar las Jams")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al borrar datos")
            }
    }
}