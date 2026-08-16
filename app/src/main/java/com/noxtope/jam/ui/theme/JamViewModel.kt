package com.noxtope.jam.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

data class JamData(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val direccion: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val pais: String = "",
    val imagenBase64: String = "",
    val creadoPor: String = "",
    val creadorUsername: String = "",
    val creadorFotoUrl: String = "",
    val etiquetas: List<String> = emptyList(),
    val asistentes: List<String> = emptyList(),
    val solicitantes: List<String> = emptyList(),
    val maxParticipantes: Int = 50,
    val estado: String = "activa",
    val esPrivada: Boolean = false,
    val visible: Boolean = true,
    val timestamp: Long = 0L
)

class JamViewModel : ViewModel() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }

    private val _jams = MutableStateFlow<List<JamData>>(emptyList())
    val jams: StateFlow<List<JamData>> = _jams

    private val _misJams = MutableStateFlow<List<JamData>>(emptyList())
    val misJams: StateFlow<List<JamData>> = _misJams

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating

    private val _jamsHistorial = MutableStateFlow<List<JamData>>(emptyList())
    val jamsHistorial: StateFlow<List<JamData>> = _jamsHistorial

    fun cargarHistorial(ids: List<String>) {
        viewModelScope.launch {
            try {
                if (ids.isEmpty()) { _jamsHistorial.value = emptyList(); return@launch }
                val chunks = ids.chunked(10)
                val resultado = mutableListOf<JamData>()
                for (chunk in chunks) {
                    val snapshot = db.collection("jams")
                        .whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                        .get()
                        .await()
                    resultado.addAll(snapshot.documents.mapNotNull { documentToJamData(it) })
                }
                _jamsHistorial.value = resultado
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun eliminarDelHistorial(
        jamId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: run { onError("No hay sesión"); return }
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(uid)
                    .update("jamsHistorial", com.google.firebase.firestore.FieldValue.arrayRemove(jamId))
                    .await()
                _jamsHistorial.value = _jamsHistorial.value.filter { it.id != jamId }
                onSuccess()
            } catch (e: Exception) { onError(e.message ?: "Error") }
        }
    }

    // ====== TAGS COMPARTIDOS ======
    data class TagInfo(val nombre: String, val usos: Int = 0)

    private val _tagsGlobales = MutableStateFlow<List<TagInfo>>(emptyList())
    val tagsGlobales: StateFlow<List<TagInfo>> = _tagsGlobales

    fun uriToBase64(context: Context, uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            val resized = Bitmap.createScaledBitmap(originalBitmap, 600, 400, true)
            val outputStream = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun subirImagenAStorage(context: Context, uri: Uri, path: String): String {
        return try {
            val ref = storage.reference.child(path)
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            uriToBase64(context, uri)
        }
    }

    private fun documentToJamData(
        doc: com.google.firebase.firestore.DocumentSnapshot
    ): JamData {
        return JamData(
            id = doc.id,
            titulo = doc.getString("titulo") ?: "",
            descripcion = doc.getString("descripcion") ?: "",
            direccion = doc.getString("direccion") ?: "",
            latitud = doc.getDouble("latitud"),
            longitud = doc.getDouble("longitud"),
            pais = doc.getString("pais") ?: "",
            imagenBase64 = doc.getString("imagenBase64") ?: "",
            creadoPor = doc.getString("creadoPor") ?: "",
            creadorUsername = doc.getString("creadorUsername") ?: "",
            creadorFotoUrl = doc.getString("creadorFotoUrl") ?: "",
            etiquetas = (doc.get("etiquetas") as? List<*>)
                ?.filterIsInstance<String>() ?: emptyList(),
            asistentes = (doc.get("asistentes") as? List<*>)
                ?.filterIsInstance<String>() ?: emptyList(),
            solicitantes = (doc.get("solicitantes") as? List<*>)
                ?.filterIsInstance<String>() ?: emptyList(),
            maxParticipantes = (doc.getLong("maxParticipantes")?.toInt()) ?: 50,
            estado = doc.getString("estado") ?: "activa",
            esPrivada = doc.getBoolean("esPrivada") ?: false,
            visible = doc.getBoolean("visible") ?: true,
            timestamp = doc.getLong("timestamp") ?: 0L
        )
    }

    fun cargarFeed() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                migrarTagsDeJamsExistentes()
                feedListener?.remove()
                feedListener = db.collection("jams")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            _isLoading.value = false
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            _jams.value = snapshot.documents.mapNotNull { doc ->
                                val jam = documentToJamData(doc)
                                if (jam.estado != "activa") null
                                else if (!jam.esPrivada && jam.visible) jam
                                else null
                            }
                        }
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _isLoading.value = false
                e.printStackTrace()
            }
        }
    }

    fun refrescarFeed() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                val snapshot = db.collection("jams")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                _jams.value = snapshot.documents.mapNotNull { doc ->
                    val jam = documentToJamData(doc)
                    if (jam.estado != "activa") null
                    else if (!jam.esPrivada && jam.visible) jam
                    else null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun cargarMisJams() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                misJamsListener?.remove()
                misJamsListener = db.collection("jams")
                    .whereEqualTo("creadoPor", uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            _misJams.value = snapshot.documents
                                .map { doc -> documentToJamData(doc) }
                                .sortedByDescending { it.timestamp }
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun crearJam(
        context: Context,
        titulo: String,
        descripcion: String,
        direccion: String,
        etiquetas: List<String>,
        maxParticipantes: Int,
        esPrivada: Boolean,
        imagenUri: Uri?,
        latitud: Double? = null,
        longitud: Double? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onError("No hay sesión activa")
            return
        }
        _isCreating.value = true

        if (usuarioTieneOtraJamActiva("")) { onError("Ya tienes una Jam activa, termínala antes de crear otra"); _isCreating.value = false; return }

        viewModelScope.launch {
            try {
                val userDoc = db.collection("usuarios").document(uid).get().await()
                val username = userDoc.getString("username") ?: ""
                val fotoUrl = userDoc.getString("fotoPerfilUrl") ?: ""
                val userPais = userDoc.getString("pais") ?: ""

                val imagenBase64 = if (imagenUri != null) {
                    uriToBase64(context, imagenUri)
                } else ""

                // Registrar tags en Firestore (crear docs si no existen)
                registrarTagsEnFirestore(etiquetas)

                val jam = hashMapOf(
                    "titulo" to titulo,
                    "descripcion" to descripcion,
                    "direccion" to direccion,
                    "latitud" to latitud,
                    "longitud" to longitud,
                    "pais" to userPais,
                    "imagenBase64" to imagenBase64,
                    "creadoPor" to uid,
                    "creadorUsername" to username,
                    "creadorFotoUrl" to fotoUrl,
                    "etiquetas" to etiquetas,
                    "asistentes" to listOf(uid),
                    "solicitantes" to emptyList<String>(),
                    "maxParticipantes" to maxParticipantes,
                    "estado" to "activa",
                    "esPrivada" to esPrivada,
                    "visible" to true,
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("jams").add(jam).await()
                incrementarUsoTags(etiquetas)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al crear Jam")
            } finally {
                _isCreating.value = false
            }
        }
    }

    fun crearJamConImagen(
        context: Context,
        titulo: String,
        descripcion: String,
        direccion: String,
        etiquetas: List<String>,
        maxParticipantes: Int,
        esPrivada: Boolean,
        imagenUri: Uri?,
        latitud: Double? = null,
        longitud: Double? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onError("No hay sesión activa")
            return
        }
        _isCreating.value = true
        if (usuarioTieneOtraJamActiva("")) { onError("Ya tienes una Jam activa, termínala antes de crear otra"); _isCreating.value = false; return }

        viewModelScope.launch {
            try {
                val userDoc = db.collection("usuarios").document(uid).get().await()
                val username = userDoc.getString("username") ?: ""
                val fotoUrl = userDoc.getString("fotoPerfilUrl") ?: ""
                val userPais = userDoc.getString("pais") ?: ""

                registrarTagsEnFirestore(etiquetas)

                // 1. Crear jam sin imagen
                val jamData = hashMapOf(
                    "titulo" to titulo,
                    "descripcion" to descripcion,
                    "direccion" to direccion,
                    "latitud" to latitud,
                    "longitud" to longitud,
                    "pais" to userPais,
                    "imagenBase64" to "",
                    "creadoPor" to uid,
                    "creadorUsername" to username,
                    "creadorFotoUrl" to fotoUrl,
                    "etiquetas" to etiquetas,
                    "asistentes" to listOf(uid),
                    "solicitantes" to emptyList<String>(),
                    "maxParticipantes" to maxParticipantes,
                    "estado" to "activa",
                    "esPrivada" to esPrivada,
                    "visible" to true,
                    "timestamp" to System.currentTimeMillis()
                )
                val docRef = db.collection("jams").add(jamData).await()
                val jamId = docRef.id

                // 2. Subir imagen si existe
                var imagenUrl = ""
                if (imagenUri != null) {
                    imagenUrl = subirImagenAStorage(context, imagenUri, "jam_banners/${jamId}.jpg")
                    db.collection("jams").document(jamId).update("imagenBase64", imagenUrl).await()
                }

                incrementarUsoTags(etiquetas)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al crear Jam")
            } finally {
                _isCreating.value = false
            }
        }
    }

    fun toggleVisibilidad(
        jamId: String,
        visible: Boolean,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                db.collection("jams").document(jamId)
                    .update("visible", visible)
                    .await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al actualizar")
            }
        }
    }

    fun togglePrivacidad(
        jamId: String,
        esPrivada: Boolean,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                db.collection("jams").document(jamId)
                    .update("esPrivada", esPrivada)
                    .await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al actualizar")
            }
        }
    }

    fun eliminarJam(
        jamId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val doc = db.collection("jams").document(jamId).get().await()
                val asistentes = (doc.get("asistentes") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val creador = doc.getString("creadoPor") ?: ""

                // Guardar en historial antes de borrar
                val todosUids = (asistentes + creador).distinct()
                for (uidP in todosUids) {
                    db.collection("usuarios").document(uidP)
                        .update("jamsHistorial", com.google.firebase.firestore.FieldValue.arrayUnion(jamId))
                        .await()
                }

                db.collection("jams")
                    .document(jamId)
                    .delete()
                    .await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al eliminar")
            }
        }
    }

    fun reportarJam(jamId: String, motivo: String, onResult: (Boolean) -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: run { onResult(false); return }
        viewModelScope.launch {
            try {
                db.collection("reportes").add(hashMapOf(
                    "tipo" to "jam",
                    "denuncianteId" to uid,
                    "jamId" to jamId,
                    "motivo" to motivo,
                    "timestamp" to System.currentTimeMillis()
                )).await()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun estaAceptado(jam: JamData): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return jam.asistentes.contains(uid) || jam.creadoPor == uid
    }

    fun tieneSolicitudPendiente(jam: JamData): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return jam.solicitantes.contains(uid)
    }

    // ====== FUNCIONES DE TAGS COMPARTIDOS ======

    private var tagListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var tagListenerVersion = 0L
    private val tagsBase = listOf(
        "Fiesta", "Rave", "Chill", "After", "Techno",
        "Pop", "Hip-Hop", "Indie", "Rock", "Reggaeton"
    )

    fun cargarTags() {
        val version = ++tagListenerVersion
        if (_tagsGlobales.value.isEmpty()) {
            _tagsGlobales.value = tagsBase.map { TagInfo(it, 0) }
        }
        tagListener?.remove()
        tagListener = db.collection("tags").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            if (version != tagListenerVersion) return@addSnapshotListener
            if (snapshot.isEmpty) {
                sembrarTagsBase()
            } else {
                _tagsGlobales.value = mergeConLocal(snapshot.documents)
            }
        }
    }

    private fun sembrarTagsBase() {
        viewModelScope.launch {
            try {
                for (tag in tagsBase) {
                    val ref = db.collection("tags").document(tag.lowercase())
                    if (!ref.get().await().exists()) {
                        ref.set(mapOf("nombre" to tag, "usos" to 0)).await()
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun mergeConLocal(docs: List<com.google.firebase.firestore.DocumentSnapshot>): List<TagInfo> {
        val firebase = docs.mapNotNull { doc ->
            val nombre = doc.getString("nombre") ?: return@mapNotNull null
            val usos = doc.getLong("usos")?.toInt() ?: 0
            TagInfo(nombre, usos)
        }.toMutableList()
        val local = _tagsGlobales.value
        for (l in local) {
            if (firebase.none { it.nombre.equals(l.nombre, ignoreCase = true) }) {
                firebase.add(l)
            }
        }
        return firebase
    }

    fun agregarTagGlobal(
        nuevoTag: String,
        onYaExiste: () -> Unit = {},
        onAgregado: () -> Unit = {}
    ) {
        val tagLimpio = nuevoTag.trim()
        if (tagLimpio.isBlank()) return
        if (_tagsGlobales.value.any { it.nombre.equals(tagLimpio, ignoreCase = true) }) {
            onYaExiste(); return
        }
        // Optimistic update: añadir localmente para feedback inmediato
        _tagsGlobales.value = _tagsGlobales.value + TagInfo(tagLimpio, 0)
        onAgregado()
        // Firestore write en background
        viewModelScope.launch {
            try {
                val docRef = db.collection("tags").document(tagLimpio.lowercase())
                if (!docRef.get().await().exists()) {
                    docRef.set(mapOf("nombre" to tagLimpio, "usos" to 0)).await()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun incrementarUsoTags(tags: List<String>) {
        viewModelScope.launch {
            for (tag in tags) {
                try {
                    db.collection("tags").document(tag.lowercase())
                        .update("usos", com.google.firebase.firestore.FieldValue.increment(1))
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    private fun registrarTagsEnFirestore(tags: List<String>) {
        viewModelScope.launch {
            for (tag in tags) {
                try {
                    val ref = db.collection("tags").document(tag.lowercase())
                    if (!ref.get().await().exists()) {
                        ref.set(mapOf("nombre" to tag, "usos" to 0)).await()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    private var migracionEjecutada = false

    private fun migrarTagsDeJamsExistentes() {
        if (migracionEjecutada) return
        migracionEjecutada = true
        viewModelScope.launch {
            try {
                val jamsSnapshot = db.collection("jams").get().await()
                val todosTags = jamsSnapshot.documents.flatMap { doc ->
                    (doc.get("etiquetas") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                }.distinct()
                for (tag in todosTags) {
                    val ref = db.collection("tags").document(tag.lowercase())
                    if (!ref.get().await().exists()) {
                        ref.set(mapOf("nombre" to tag, "usos" to 0)).await()
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    companion object {
        /** Haversine distance in km */
        fun calcularDistanciaKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
            val r = 6371.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLng = Math.toRadians(lng2 - lng1)
            val sinLat = Math.sin(dLat / 2)
            val sinLng = Math.sin(dLng / 2)
            val a = sinLat * sinLat +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                    sinLng * sinLng
            return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        }
    }

    // ====== SOLICITUDES (colección separada para evitar permisos) ======
    data class SolicitudData(
        val jamId: String = "",
        val usuarioId: String = "",
        val username: String = "",
        val fotoUrl: String = "",
        val bio: String = "",
        val etiquetas: List<String> = emptyList(),
        val timestamp: Long = 0L
    )

    private val _misSolicitudes = MutableStateFlow<Set<String>>(emptySet())
    val misSolicitudes: StateFlow<Set<String>> = _misSolicitudes

    private var solicitudesListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun cargarMisSolicitudes() {
        val uid = auth.currentUser?.uid ?: return
        solicitudesListener?.remove()
        solicitudesListener = db.collection("solicitudes")
            .whereEqualTo("usuarioId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _misSolicitudes.value = snapshot.documents.mapNotNull { doc ->
                    doc.getString("jamId")
                }.toSet()
            }
    }

    fun solicitarUnirse(
        jam: JamData,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: run { onError("No hay sesión"); return }
        if (uid == jam.creadoPor) { onError("Eres el creador"); return }
        if (jam.asistentes.contains(uid)) { onError("Ya eres parte de esta Jam"); return }
        if (_misSolicitudes.value.contains(jam.id)) { onError("Ya enviaste solicitud"); return }
        if (jam.asistentes.size >= jam.maxParticipantes) { onError("La Jam está llena"); return }
        if (usuarioTieneOtraJamActiva(jam.id)) { onError("Solo puedes estar en 1 Jam activa a la vez"); return }

        viewModelScope.launch {
            try {
                val userDoc = db.collection("usuarios").document(uid).get().await()
                val username = userDoc.getString("username") ?: "Anónimo"
                val fotoUrl = userDoc.getString("fotoPerfilUrl") ?: ""
                val bio = userDoc.getString("bio") ?: ""
                val etiquetas: List<String> = (userDoc.get("etiquetas") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                db.collection("solicitudes").document("${jam.id}_$uid").set(mapOf(
                    "jamId" to jam.id,
                    "usuarioId" to uid,
                    "username" to username,
                    "fotoUrl" to fotoUrl,
                    "bio" to bio,
                    "etiquetas" to etiquetas,
                    "timestamp" to System.currentTimeMillis()
                )).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al solicitar unirte")
            }
        }
    }

    fun cancelarSolicitud(
        jamId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: run { onError("No hay sesión"); return }
        viewModelScope.launch {
            try {
                db.collection("solicitudes").document("${jamId}_$uid").delete().await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al cancelar solicitud")
            }
        }
    }

    fun obtenerSolicitudesDeJam(jamId: String, onResult: (List<SolicitudData>) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("solicitudes")
                    .whereEqualTo("jamId", jamId)
                    .get().await()
                val result = snapshot.documents.map { doc ->
                    SolicitudData(
                        jamId = doc.getString("jamId") ?: "",
                        usuarioId = doc.getString("usuarioId") ?: "",
                        username = doc.getString("username") ?: "",
                        fotoUrl = doc.getString("fotoUrl") ?: "",
                        bio = doc.getString("bio") ?: "",
                        etiquetas = (doc.get("etiquetas") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }
                onResult(result)
            } catch (e: Exception) { onResult(emptyList()) }
        }
    }

    // ====== RESPUESTA A SOLICITUD (creador decide) ======
    fun responderSolicitud(
        jamId: String,
        userIdB: String,
        aceptar: Boolean,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val jamRef = db.collection("jams").document(jamId)
                if (aceptar) {
                    jamRef.update("asistentes", com.google.firebase.firestore.FieldValue.arrayUnion(userIdB)).await()
                }
                db.collection("solicitudes").document("${jamId}_$userIdB").delete().await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al responder")
            }
        }
    }

    // ====== JAMS ACTIVOS (en ejecución) ======
    private val _jamsActivos = MutableStateFlow<List<JamData>>(emptyList())
    val jamsActivos: StateFlow<List<JamData>> = _jamsActivos

    fun cargarJamsActivos() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                jamsActivosListener?.remove()
                jamsActivosListener = db.collection("jams")
                    .whereEqualTo("estado", "activa")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null || snapshot == null) return@addSnapshotListener
                        val todas = snapshot.documents.mapNotNull { doc ->
                            val jam = documentToJamData(doc)
                            jam
                        }
                        _jamsActivos.value = todas.filter { jam ->
                            jam.asistentes.contains(uid) || jam.creadoPor == uid || _misSolicitudes.value.contains(jam.id)
                        }
                    }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun terminarJam(
        jamId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("jams").document(jamId).get().await()
                val asistentes = (doc.get("asistentes") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val creador = doc.getString("creadoPor") ?: ""

                // Actualizar estado a terminada
                db.collection("jams").document(jamId)
                    .update("estado", "terminada").await()

                // Agregar jamId al historial de todos los participantes
                val todosUids = (asistentes + creador).distinct()
                for (uidP in todosUids) {
                    db.collection("usuarios").document(uidP)
                        .update("jamsHistorial", com.google.firebase.firestore.FieldValue.arrayUnion(jamId))
                        .await()
                }
                onSuccess()
            } catch (e: Exception) { onError(e.message ?: "Error") }
        }
    }

    fun actualizarJam(
        jamId: String,
        datos: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                db.collection("jams").document(jamId)
                    .update(datos).await()
                onSuccess()
            } catch (e: Exception) { onError(e.message ?: "Error") }
        }
    }

    // ====== EXPULSAR / SALIR ======
    fun expulsarParticipante(
        jamId: String,
        userIdB: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                db.collection("jams").document(jamId)
                    .update("asistentes", com.google.firebase.firestore.FieldValue.arrayRemove(userIdB)).await()
                onSuccess()
            } catch (e: Exception) { onError(e.message ?: "Error al expulsar") }
        }
    }

    fun salirDeJam(
        jamId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: run { onError("No hay sesión"); return }
        viewModelScope.launch {
            try {
                db.collection("jams").document(jamId)
                    .update("asistentes", com.google.firebase.firestore.FieldValue.arrayRemove(uid)).await()
                onSuccess()
            } catch (e: Exception) { onError(e.message ?: "Error al salir") }
        }
    }

    fun usuarioTieneOtraJamActiva(jamIdActual: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return _jamsActivos.value.any { jam ->
            (jam.asistentes.contains(uid) || jam.creadoPor == uid) && jam.id != jamIdActual
        }
    }

    // ====== CHAT ======
    data class MensajeChat(
        val id: String = "",
        val usuarioId: String = "",
        val username: String = "",
        val texto: String = "",
        val imagenBase64: String = "",
        val colorSecundario: Long = 0L,
        val lucesActivas: Boolean = false,
        val timestamp: Long = 0L
    )

    private val _mensajesChat = MutableStateFlow<List<MensajeChat>>(emptyList())
    val mensajesChat: StateFlow<List<MensajeChat>> = _mensajesChat

    private var chatListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun cargarMensajes(jamId: String) {
        chatListener?.remove()
        chatListener = db.collection("jams").document(jamId)
            .collection("mensajes")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _mensajesChat.value = snapshot.documents.map { doc ->
                    MensajeChat(
                        id = doc.id,
                        usuarioId = doc.getString("usuarioId") ?: "",
                        username = doc.getString("username") ?: "",
                        texto = doc.getString("texto") ?: "",
                        imagenBase64 = doc.getString("imagenBase64") ?: "",
                        colorSecundario = doc.getLong("colorSecundario") ?: 0L,
                        lucesActivas = doc.getBoolean("lucesActivas") ?: false,
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }
            }
    }

    fun detenerChat() {
        chatListener?.remove()
        chatListener = null
        _mensajesChat.value = emptyList()
    }

    fun enviarMensaje(
        jamId: String,
        texto: String,
        onError: (String) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: run { onError("No hay sesión"); return }
        if (texto.isBlank()) return
        viewModelScope.launch {
            try {
                val userDoc = db.collection("usuarios").document(uid).get().await()
                val username = userDoc.getString("username") ?: "Anónimo"
                val colorSecundario = userDoc.getLong("colorSecundario") ?: 0L
                val lucesActivas = userDoc.getBoolean("lucesActivas") ?: false
                db.collection("jams").document(jamId)
                    .collection("mensajes").add(mapOf(
                        "usuarioId" to uid,
                        "username" to username,
                        "texto" to texto,
                        "colorSecundario" to colorSecundario,
                        "lucesActivas" to lucesActivas,
                        "timestamp" to System.currentTimeMillis()
                    )).await()
            } catch (e: Exception) {
                onError(e.message ?: "Error al enviar mensaje")
            }
        }
    }

    fun enviarMensajeConImagen(
        jamId: String,
        imagenBase64: String,
        texto: String = "",
        onError: (String) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: run { onError("No hay sesión"); return }
        viewModelScope.launch {
            try {
                val userDoc = db.collection("usuarios").document(uid).get().await()
                val username = userDoc.getString("username") ?: "Anónimo"
                val colorSecundario = userDoc.getLong("colorSecundario") ?: 0L
                val lucesActivas = userDoc.getBoolean("lucesActivas") ?: false
                val mensajeData = mutableMapOf<String, Any>(
                    "usuarioId" to uid,
                    "username" to username,
                    "colorSecundario" to colorSecundario,
                    "lucesActivas" to lucesActivas,
                    "imagenBase64" to imagenBase64,
                    "timestamp" to System.currentTimeMillis()
                )
                if (texto.isNotBlank()) {
                    mensajeData["texto"] = texto
                }
                db.collection("jams").document(jamId)
                    .collection("mensajes").add(mensajeData).await()
            } catch (e: Exception) {
                onError(e.message ?: "Error al enviar mensaje")
            }
        }
    }

    fun enviarMensajeConImagenStorage(
        context: Context,
        jamId: String,
        uri: Uri,
        texto: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: run { onError("No hay sesión"); return }
        viewModelScope.launch {
            try {
                val userDoc = db.collection("usuarios").document(uid).get().await()
                val username = userDoc.getString("username") ?: "Anónimo"
                val colorSecundario = userDoc.getLong("colorSecundario") ?: 0L
                val lucesActivas = userDoc.getBoolean("lucesActivas") ?: false

                val imagenUrl = subirImagenAStorage(context, uri, "chat/${jamId}/${System.currentTimeMillis()}.jpg")

                val mensajeData = mutableMapOf<String, Any>(
                    "usuarioId" to uid,
                    "username" to username,
                    "colorSecundario" to colorSecundario,
                    "lucesActivas" to lucesActivas,
                    "imagenBase64" to imagenUrl,
                    "timestamp" to System.currentTimeMillis()
                )
                if (texto.isNotBlank()) {
                    mensajeData["texto"] = texto
                }
                db.collection("jams").document(jamId)
                    .collection("mensajes").add(mensajeData).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al enviar mensaje")
            }
        }
    }

    fun convertirUriABase64(context: android.content.Context, uri: Uri, onResult: (String) -> Unit, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val maxSize = 800
                    val scale = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height, 1f)
                    val resized = Bitmap.createScaledBitmap(bitmap,
                        (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
                    val outputStream = ByteArrayOutputStream()
                    resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                    onResult(base64)
                } else {
                    onError("No se pudo leer la imagen")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error al procesar imagen")
            }
        }
    }

    // ====== BÚSQUEDA DE JAMS ======

    private val _jamsBusqueda = MutableStateFlow<List<JamData>>(emptyList())
    val jamsBusqueda: StateFlow<List<JamData>> = _jamsBusqueda

    private var jamsBusquedaListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var feedListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var misJamsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var jamsActivosListener: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCleared() {
        super.onCleared()
        tagListener?.remove()
        solicitudesListener?.remove()
        chatListener?.remove()
        jamsBusquedaListener?.remove()
        feedListener?.remove()
        misJamsListener?.remove()
        jamsActivosListener?.remove()
    }

    fun buscarJams(query: String = "", tagFiltro: String = "") {
        jamsBusquedaListener?.remove()
        if (query.isBlank() && tagFiltro.isBlank()) {
            _jamsBusqueda.value = emptyList()
            return
        }
        var ref = db.collection("jams")
            .whereEqualTo("estado", "activa")
            .whereEqualTo("visible", true)
            .limit(30)
        if (tagFiltro.isNotBlank()) {
            ref = ref.whereArrayContains("etiquetas", tagFiltro)
        }
        jamsBusquedaListener = ref.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val q = query.lowercase()
            _jamsBusqueda.value = snapshot.documents
                .mapNotNull { doc -> documentToJamData(doc) }
                .filter { jam ->
                    if (q.isBlank()) true
                    else jam.titulo.lowercase().contains(q) ||
                         jam.descripcion.lowercase().contains(q)
                }
                .filter { !it.esPrivada && it.visible }
        }
    }

    fun detenerBusquedaJams() {
        jamsBusquedaListener?.remove()
        jamsBusquedaListener = null
        _jamsBusqueda.value = emptyList()
    }

    fun refrescarBusquedaJams(query: String = "", tagFiltro: String = "") {
        if (query.isNotBlank() || tagFiltro.isNotBlank()) {
            buscarJams(query, tagFiltro)
        } else {
            detenerBusquedaJams()
        }
    }

    fun refrescarJamsActivos() {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val snapshot = db.collection("jams")
                    .whereEqualTo("estado", "activa")
                    .get().await()
                _jamsActivos.value = snapshot.documents.mapNotNull { doc ->
                    val jam = documentToJamData(doc)
                    if (jam.creadoPor == uid || jam.asistentes.contains(uid) || jam.solicitantes.contains(uid)) jam
                    else null
                }
            } catch (_: Exception) {}
        }
    }
}