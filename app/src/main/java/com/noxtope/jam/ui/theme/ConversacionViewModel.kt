package com.noxtope.jam.ui.theme

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ConversacionData(
    val id: String = "",
    val participantes: List<String> = emptyList(),
    val ultimoMensaje: String = "",
    val ultimaFecha: Long = 0L,
    val otroUsername: String = "",
    val otroFotoUrl: String = "",
    val noLeidos: Int = 0
)

data class MensajeDirecto(
    val id: String = "",
    val emisorId: String = "",
    val username: String = "",
    val texto: String = "",
    val timestamp: Long = 0L,
    val imagenUrl: String = ""
)

class ConversacionViewModel : ViewModel() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }

    private val _conversaciones = MutableStateFlow<List<ConversacionData>>(emptyList())
    val conversaciones: StateFlow<List<ConversacionData>> = _conversaciones

    private val _mensajes = MutableStateFlow<List<MensajeDirecto>>(emptyList())
    val mensajes: StateFlow<List<MensajeDirecto>> = _mensajes

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private var conversacionesListener: ListenerRegistration? = null
    private var mensajesListener: ListenerRegistration? = null

    fun generarConversacionId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    fun escucharConversaciones() {
        val uid = auth.currentUser?.uid ?: return
        conversacionesListener?.remove()
        conversacionesListener = db.collection("conversaciones")
            .whereArrayContains("participantes", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val conversaciones = snapshot.documents.mapNotNull { doc ->
                    val participantes = (doc.get("participantes") as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList()
                    val otroUid = participantes.find { it != uid } ?: return@mapNotNull null
                    ConversacionData(
                        id = doc.id,
                        participantes = participantes,
                        ultimoMensaje = doc.getString("ultimoMensaje") ?: "",
                        ultimaFecha = doc.getLong("ultimaFecha") ?: 0L,
                        otroUsername = doc.getString("otroUsername_$otroUid") ?: "Usuario",
                        otroFotoUrl = doc.getString("otroFotoUrl_$otroUid") ?: ""
                    )
                }.sortedByDescending { it.ultimaFecha }
                _conversaciones.value = conversaciones
            }
    }

    fun detenerEscuchaConversaciones() {
        conversacionesListener?.remove()
        conversacionesListener = null
    }

    fun obtenerOCrearConversacion(otroUid: String, onResult: (String) -> Unit = {}) {
        val miUid = auth.currentUser?.uid ?: return
        val convId = generarConversacionId(miUid, otroUid)
        viewModelScope.launch {
            try {
                val doc = db.collection("conversaciones").document(convId).get().await()
                if (doc.exists()) {
                    onResult(convId)
                } else {
                    db.collection("usuarios").document(otroUid).get().await().let { otroDoc ->
                        val otroUsername = otroDoc.getString("username") ?: "Usuario"
                        val otroFotoUrl = otroDoc.getString("fotoPerfilUrl") ?: ""

                        db.collection("usuarios").document(miUid).get().await().let { miDoc ->
                            val miUsername = miDoc.getString("username") ?: "Yo"
                            val miFotoUrl = miDoc.getString("fotoPerfilUrl") ?: ""

                            val data = hashMapOf(
                                "participantes" to listOf(miUid, otroUid),
                                "otroUsername_$miUid" to otroUsername,
                                "otroUsername_$otroUid" to miUsername,
                                "otroFotoUrl_$miUid" to otroFotoUrl,
                                "otroFotoUrl_$otroUid" to miFotoUrl,
                                "ultimoMensaje" to "",
                                "ultimaFecha" to System.currentTimeMillis()
                            )
                            db.collection("conversaciones").document(convId)
                                .set(data).await()
                        }
                    }
                    onResult(convId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun escucharMensajes(conversacionId: String) {
        mensajesListener?.remove()
        mensajesListener = db.collection("conversaciones").document(conversacionId)
            .collection("mensajes")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _mensajes.value = snapshot.documents.mapNotNull { doc ->
                    MensajeDirecto(
                        id = doc.id,
                        emisorId = doc.getString("emisorId") ?: "",
                        username = doc.getString("username") ?: "",
                        texto = doc.getString("texto") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        imagenUrl = doc.getString("imagenUrl") ?: ""
                    )
                }
            }
    }

    fun detenerEscuchaMensajes() {
        mensajesListener?.remove()
        mensajesListener = null
    }

    fun enviarMensaje(conversacionId: String, texto: String, otroUid: String = "") {
        val uid = auth.currentUser?.uid ?: return
        if (texto.isBlank()) return
        viewModelScope.launch {
            try {
                val miUsername = db.collection("usuarios").document(uid).get().await()
                    .getString("username") ?: "Yo"
                val msgRef = db.collection("conversaciones").document(conversacionId)
                    .collection("mensajes").document()
                msgRef.set(mapOf(
                    "emisorId" to uid,
                    "username" to miUsername,
                    "texto" to texto,
                    "timestamp" to System.currentTimeMillis()
                )).await()
                db.collection("conversaciones").document(conversacionId)
                    .update(mapOf(
                        "ultimoMensaje" to texto,
                        "ultimaFecha" to System.currentTimeMillis()
                    )).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun enviarMensajeConImagen(conversacionId: String, imageUri: Uri, otroUid: String = "") {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val miUsername = db.collection("usuarios").document(uid).get().await()
                    .getString("username") ?: "Yo"
                val ref = storage.reference.child("chat_directo/${conversacionId}/${System.currentTimeMillis()}.jpg")
                ref.putFile(imageUri).await()
                val url = ref.downloadUrl.await().toString()
                val msgRef = db.collection("conversaciones").document(conversacionId)
                    .collection("mensajes").document()
                msgRef.set(mapOf(
                    "emisorId" to uid,
                    "username" to miUsername,
                    "texto" to "",
                    "imagenUrl" to url,
                    "timestamp" to System.currentTimeMillis()
                )).await()
                db.collection("conversaciones").document(conversacionId)
                    .update(mapOf(
                        "ultimoMensaje" to "📷 Foto",
                        "ultimaFecha" to System.currentTimeMillis()
                    )).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        detenerEscuchaConversaciones()
        detenerEscuchaMensajes()
    }
}
