package com.noxtope.jam.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.noxtope.jam.R

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private fun s(id: Int, vararg args: Any) = getApplication<Application>().getString(id, *args)

    fun traducirError(mensaje: String?): String {
        val m = mensaje?.lowercase() ?: return s(R.string.auth_error_desconocido)
        return when {
            m.contains("password is invalid") || m.contains("wrong password")
                -> s(R.string.auth_password_incorrecta)
            m.contains("no user record") || m.contains("user not found")
                -> s(R.string.auth_no_existe)
            m.contains("already in use") || m.contains("already exists")
                -> s(R.string.auth_ya_registrado)
            m.contains("badly formatted") || m.contains("invalid email")
                -> s(R.string.auth_formato_invalido)
            m.contains("6 characters") || m.contains("weak password")
                -> s(R.string.auth_password_corta)
            m.contains("network error") || m.contains("unreachable host") || m.contains("timeout")
                -> s(R.string.auth_error_conexion)
            m.contains("blocked all requests") || m.contains("too many")
                -> s(R.string.auth_demasiados)
            else -> s(R.string.error_msg, mensaje ?: s(R.string.auth_error_desconocido))
        }
    }

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
                            onError(e.message ?: s(R.string.auth_error_guardar_usuario))
                        }
                } else {
                    onError(traducirError(tarea.exception?.message))
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
                    onError(traducirError(tarea.exception?.message))
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
                                        onError(e.message ?: s(R.string.auth_error_guardar))
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            onError(e.message ?: s(R.string.auth_error_verificar_usuario))
                        }
                } else {
                    onError(traducirError(tarea.exception?.message))
                }
            }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) onSuccess()
                else onError(traducirError(tarea.exception?.message))
            }
    }

    private fun verificarDatosPersonales(
        onResultado: (necesitaDatos: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onError(s(R.string.auth_sin_sesion))
            return
        }
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val completos = doc.getBoolean("datosPersonalesCompletos") ?: false
                onResultado(!completos)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: s(R.string.auth_error_verificar_datos))
            }
    }

    fun eliminarCuenta(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            onError(s(R.string.auth_sin_sesion_activa))
            return
        }
        val uid = user.uid

        db.collection("usuarios").document(uid).delete()
            .addOnSuccessListener {
                // Borrar relaciones (follows) donde participo
                db.collection("relaciones").whereEqualTo("seguidor", uid).get()
                    .addOnSuccessListener { seguidor ->
                        seguidor.documents.forEach { it.reference.delete() }
                    }
                db.collection("relaciones").whereEqualTo("seguido", uid).get()
                    .addOnSuccessListener { seguido ->
                        seguido.documents.forEach { it.reference.delete() }
                    }
                // Borrar solicitudes de unión propias
                db.collection("solicitudes").whereEqualTo("usuarioId", uid).get()
                    .addOnSuccessListener { sols ->
                        sols.documents.forEach { it.reference.delete() }
                    }
                // Borrar conversaciones donde participo
                db.collection("conversaciones").whereArrayContains("participantes", uid).get()
                    .addOnSuccessListener { convs ->
                        convs.documents.forEach { it.reference.delete() }
                    }
                // Borrar Jams que creé
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
                                        ?: s(R.string.auth_reinicia_eliminar)
                                )
                            }
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: s(R.string.auth_error_borrar_jams))
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: s(R.string.auth_error_borrar_datos))
            }
    }
}