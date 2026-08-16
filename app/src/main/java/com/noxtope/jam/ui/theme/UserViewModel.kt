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
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

const val CREADOR_EMAIL = "oscar2puerta@gmail.com"

data class UsuarioData(
    val uid: String = "",
    // Datos personales (bloqueados una vez guardados)
    val nombre: String = "",
    val apellidos: String = "",
    val pais: String = "",
    val telefono: String = "",
    val numeroIdentidad: String = "",
    val datosPersonalesCompletos: Boolean = false,
    // Datos de perfil (editables)
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val fotoPerfilUrl: String = "",
    val bannerUrl: String = "",
    val esVerificado: Boolean = false,
    val etiquetas: List<String> = emptyList(),
    val colorPrimario: Long = 0xFFFFFFFF,
    val colorSecundario: Long = 0xFF666666,
    val modoOscuro: Boolean = true,
    val mostrarNombreReal: Boolean = false,
    val mostrarEmail: Boolean = false,
    val lucesActivas: Boolean = false,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val seguidores: List<String> = emptyList(),
    val siguiendo: List<String> = emptyList(),
    val bloqueados: List<String> = emptyList(),
    val jamsHistorial: List<String> = emptyList(),
    // Premium / Planes
    val esPremium: Boolean = false,
    val premiumHasta: Long = 0L,
    val premiumVitalicio: Boolean = false,
    val jamsEstaSemana: Int = 0,
    val semanaActual: Int = 0,
    val primerMesGratis: Boolean = true,
    val introUsada: Boolean = false,
    val apoyoBeta: Boolean = false,
    val puntosApoyo: Int = 0
)

class UserViewModel : ViewModel() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }

    private val _usuario = MutableStateFlow<UsuarioData?>(null)
    val usuario: StateFlow<UsuarioData?> = _usuario

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Estado de arranque: null = cargando, luego "login", "datos_personales" o "home"
    private val _rutaInicial = MutableStateFlow<String?>(null)
    val rutaInicial: StateFlow<String?> = _rutaInicial

    fun verificarSesionActiva(): Boolean {
        return auth.currentUser != null
    }

    // Decide a dónde mandar al usuario al abrir la app
    fun decidirRutaInicial(recordarSesion: Boolean) {
        val user = auth.currentUser
        if (user == null || !recordarSesion) {
            _rutaInicial.value = "login"
            return
        }
        viewModelScope.launch {
            try {
                val doc = db.collection("usuarios").document(user.uid).get().await()
                val completos = doc.getBoolean("datosPersonalesCompletos") ?: false
                if (doc.exists() && completos) {
                    cargarUsuario()
                    _rutaInicial.value = "home"
                } else {
                    _rutaInicial.value = "datos_personales"
                }
            } catch (e: Exception) {
                _rutaInicial.value = "login"
            }
        }
    }

    private fun uriToBase64(context: Context, uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            val resized = Bitmap.createScaledBitmap(originalBitmap, 500, 500, true)
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
            uriToBase64(context, uri) // fallback a base64 si falla Storage
        }
    }

    fun cargarUsuario() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("usuarios").document(uid).get().await()
                if (doc.exists()) {
                    _usuario.value = UsuarioData(
                        uid = uid,
                        nombre = doc.getString("nombre") ?: "",
                        apellidos = doc.getString("apellidos") ?: "",
                        pais = doc.getString("pais") ?: "",
                        telefono = doc.getString("telefono") ?: "",
                        numeroIdentidad = doc.getString("numeroIdentidad") ?: "",
                        datosPersonalesCompletos = doc.getBoolean("datosPersonalesCompletos") ?: false,
                        username = doc.getString("username") ?: "",
                        email = doc.getString("email") ?: "",
                        bio = doc.getString("bio") ?: "",
                        fotoPerfilUrl = doc.getString("fotoPerfilUrl") ?: "",
                        bannerUrl = doc.getString("bannerUrl") ?: "",
                        esVerificado = (doc.getString("email") ?: "") == CREADOR_EMAIL,
                        etiquetas = (doc.get("etiquetas") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        colorPrimario = doc.getLong("colorPrimario") ?: 0xFFFFFFFF,
                        colorSecundario = doc.getLong("colorSecundario") ?: 0xFF666666,
                        modoOscuro = doc.getBoolean("modoOscuro") ?: true,
                        mostrarNombreReal = doc.getBoolean("mostrarNombreReal") ?: false,
                        mostrarEmail = doc.getBoolean("mostrarEmail") ?: false,
                        lucesActivas = doc.getBoolean("lucesActivas") ?: false,
                        seguidores = (doc.get("seguidores") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        siguiendo = (doc.get("siguiendo") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        bloqueados = (doc.get("bloqueados") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        jamsHistorial = (doc.get("jamsHistorial") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        esPremium = doc.getBoolean("esPremium") ?: false,
                        premiumHasta = doc.getLong("premiumHasta") ?: 0L,
                        premiumVitalicio = doc.getBoolean("premiumVitalicio") ?: false,
                        jamsEstaSemana = (doc.getLong("jamsEstaSemana") ?: 0L).toInt(),
                        semanaActual = (doc.getLong("semanaActual") ?: 0L).toInt(),
                        primerMesGratis = doc.getBoolean("primerMesGratis") ?: true,
                        introUsada = doc.getBoolean("introUsada") ?: false,
                        apoyoBeta = doc.getBoolean("apoyoBeta") ?: false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Verifica si el número de identidad ya existe en otra cuenta
    fun verificarIdentidadDisponible(
        numeroIdentidad: String,
        onResult: (disponible: Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("usuarios")
                    .whereEqualTo("numeroIdentidad", numeroIdentidad)
                    .get()
                    .await()
                onResult(snapshot.isEmpty)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // Guarda los datos personales (solo una vez)
    fun guardarDatosPersonales(
        nombre: String,
        apellidos: String,
        pais: String,
        telefono: String,
        numeroIdentidad: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onError("No hay sesión activa")
            return
        }
        val email = auth.currentUser?.email ?: ""
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val snapshot = db.collection("usuarios")
                    .whereEqualTo("numeroIdentidad", numeroIdentidad)
                    .get()
                    .await()
                if (!snapshot.isEmpty) {
                    _isLoading.value = false
                    onError("Este número de identidad ya está registrado en otra cuenta")
                    return@launch
                }

                val datos = mapOf(
                    "nombre" to nombre,
                    "apellidos" to apellidos,
                    "pais" to pais,
                    "telefono" to telefono,
                    "numeroIdentidad" to numeroIdentidad,
                    "email" to email,
                    "datosPersonalesCompletos" to true
                )
                db.collection("usuarios").document(uid)
                    .set(datos, SetOptions.merge())
                    .await()
                cargarUsuario()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al guardar datos")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Actualiza solo el teléfono (editable)
    fun actualizarTelefono(
        nuevoTelefono: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(uid)
                    .update("telefono", nuevoTelefono).await()
                cargarUsuario()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al actualizar teléfono")
            }
        }
    }

    fun actualizarUbicacion(latitud: Double, longitud: Double) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(uid)
                    .update(mapOf("latitud" to latitud, "longitud" to longitud)).await()
                cargarUsuario()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // Guarda datos de perfil (editables siempre)
    fun guardarPerfil(
        context: Context,
        username: String,
        bio: String,
        etiquetas: List<String>,
        mostrarNombreReal: Boolean,
        mostrarEmail: Boolean,
        nuevaFotoUri: Uri? = null,
        nuevoBannerUri: Uri? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onError("No hay sesión activa")
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                var fotoUrl = _usuario.value?.fotoPerfilUrl ?: ""
                var bannerUrl = _usuario.value?.bannerUrl ?: ""

                if (nuevaFotoUri != null) {
                    fotoUrl = subirImagenAStorage(context, nuevaFotoUri, "profile/${uid}.jpg")
                }
                if (nuevoBannerUri != null) {
                    bannerUrl = subirImagenAStorage(context, nuevoBannerUri, "banner/${uid}.jpg")
                }

                val datos = mapOf(
                    "username" to username,
                    "bio" to bio,
                    "etiquetas" to etiquetas,
                    "mostrarNombreReal" to mostrarNombreReal,
                    "mostrarEmail" to mostrarEmail,
                    "fotoPerfilUrl" to fotoUrl,
                    "bannerUrl" to bannerUrl
                )
                db.collection("usuarios").document(uid)
                    .set(datos, SetOptions.merge())
                    .await()
                cargarUsuario()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al guardar")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarPreferencias(
        colorPrimario: Long,
        colorSecundario: Long,
        modoOscuro: Boolean,
        lucesActivas: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val datos = mapOf(
                    "colorPrimario" to colorPrimario,
                    "colorSecundario" to colorSecundario,
                    "modoOscuro" to modoOscuro,
                    "lucesActivas" to lucesActivas
                )
                db.collection("usuarios").document(uid)
                    .set(datos, SetOptions.merge())
                    .await()
                _usuario.value = (_usuario.value ?: UsuarioData(uid = uid)).copy(
                    colorPrimario = colorPrimario,
                    colorSecundario = colorSecundario,
                    modoOscuro = modoOscuro,
                    lucesActivas = lucesActivas
                )
                cargarUsuario()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ====== SEGUIDORES ======

    private val _perfilPublico = MutableStateFlow<UsuarioData?>(null)
    val perfilPublico: StateFlow<UsuarioData?> = _perfilPublico

    fun cargarUsuarioPublico(uid: String) {
        viewModelScope.launch {
            try {
                val doc = db.collection("usuarios").document(uid).get().await()
                if (doc.exists()) {
                    _perfilPublico.value = UsuarioData(
                        uid = uid,
                        username = doc.getString("username") ?: "",
                        email = doc.getString("email") ?: "",
                        bio = doc.getString("bio") ?: "",
                        fotoPerfilUrl = doc.getString("fotoPerfilUrl") ?: "",
                        bannerUrl = doc.getString("bannerUrl") ?: "",
                        etiquetas = (doc.get("etiquetas") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        colorPrimario = doc.getLong("colorPrimario") ?: 0xFFFFFFFF,
                        colorSecundario = doc.getLong("colorSecundario") ?: 0xFF666666,
                        lucesActivas = doc.getBoolean("lucesActivas") ?: false,
                        seguidores = (doc.get("seguidores") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        siguiendo = (doc.get("siguiendo") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        esVerificado = (doc.getString("email") ?: "") == CREADOR_EMAIL,
                        apoyoBeta = doc.getBoolean("apoyoBeta") ?: false,
                        puntosApoyo = (doc.getLong("puntosApoyo") ?: 0L).toInt()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun limpiarPerfilPublico() { _perfilPublico.value = null }

    fun seguirUsuario(uid: String, onResult: (Boolean) -> Unit = {}) {
        val miUid = auth.currentUser?.uid ?: run { onResult(false); return }
        viewModelScope.launch {
            try {
                db.runTransaction { transaction ->
                    val miRef = db.collection("usuarios").document(miUid)
                    val otroRef = db.collection("usuarios").document(uid)
                    val miDoc = transaction.get(miRef)
                    val otroDoc = transaction.get(otroRef)
                    val misS = (miDoc.get("siguiendo") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                    val otrosS = (otroDoc.get("seguidores") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                    if (!misS.contains(uid)) misS.add(uid)
                    if (!otrosS.contains(miUid)) otrosS.add(miUid)
                    transaction.update(miRef, "siguiendo", misS)
                    transaction.update(otroRef, "seguidores", otrosS)
                    null
                }.await()
                cargarUsuario()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun dejarDeSeguir(uid: String, onResult: (Boolean) -> Unit = {}) {
        val miUid = auth.currentUser?.uid ?: run { onResult(false); return }
        viewModelScope.launch {
            try {
                db.runTransaction { transaction ->
                    val miRef = db.collection("usuarios").document(miUid)
                    val otroRef = db.collection("usuarios").document(uid)
                    val miDoc = transaction.get(miRef)
                    val otroDoc = transaction.get(otroRef)
                    val misS = (miDoc.get("siguiendo") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                    val otrosS = (otroDoc.get("seguidores") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                    misS.remove(uid)
                    otrosS.remove(miUid)
                    transaction.update(miRef, "siguiendo", misS)
                    transaction.update(otroRef, "seguidores", otrosS)
                    null
                }.await()
                cargarUsuario()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun loSigo(uid: String): Boolean {
        return _usuario.value?.siguiendo?.contains(uid) ?: false
    }

    fun loBloquee(uid: String): Boolean {
        return _usuario.value?.bloqueados?.contains(uid) ?: false
    }

    fun bloquearUsuario(uid: String, onResult: (Boolean) -> Unit = {}) {
        val miUid = auth.currentUser?.uid ?: run { onResult(false); return }
        viewModelScope.launch {
            try {
                db.runTransaction { transaction ->
                    val miRef = db.collection("usuarios").document(miUid)
                    val otroRef = db.collection("usuarios").document(uid)
                    val miDoc = transaction.get(miRef)
                    val otroDoc = transaction.get(otroRef)
                    val misB = (miDoc.get("bloqueados") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                    val misS = (miDoc.get("siguiendo") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                    val otrosS = (otroDoc.get("seguidores") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                    if (!misB.contains(uid)) misB.add(uid)
                    misS.remove(uid)
                    otrosS.remove(miUid)
                    transaction.update(miRef, "bloqueados", misB)
                    transaction.update(miRef, "siguiendo", misS)
                    transaction.update(otroRef, "seguidores", otrosS)
                    null
                }.await()
                cargarUsuario()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun desbloquearUsuario(uid: String, onResult: (Boolean) -> Unit = {}) {
        val miUid = auth.currentUser?.uid ?: run { onResult(false); return }
        viewModelScope.launch {
            try {
                val miRef = db.collection("usuarios").document(miUid)
                val misB = _usuario.value?.bloqueados?.toMutableList() ?: mutableListOf()
                misB.remove(uid)
                miRef.update("bloqueados", misB).await()
                cargarUsuario()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun reportarUsuario(uid: String, motivo: String, onResult: (Boolean) -> Unit = {}) {
        val miUid = auth.currentUser?.uid ?: run { onResult(false); return }
        viewModelScope.launch {
            try {
                db.collection("reportes").add(hashMapOf(
                    "tipo" to "usuario",
                    "denuncianteId" to miUid,
                    "denunciadoId" to uid,
                    "motivo" to motivo,
                    "timestamp" to System.currentTimeMillis()
                )).await()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun eliminarSeguidor(uid: String, onResult: (Boolean) -> Unit = {}) {
        val miUid = auth.currentUser?.uid ?: run { onResult(false); return }
        viewModelScope.launch {
            try {
                db.runTransaction { transaction ->
                    val miRef = db.collection("usuarios").document(miUid)
                    val otroRef = db.collection("usuarios").document(uid)
                    val miDoc = transaction.get(miRef)
                    val otroDoc = transaction.get(otroRef)
                    val misSeg = (miDoc.get("seguidores") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                    val otrosS = (otroDoc.get("siguiendo") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                    misSeg.remove(uid)
                    otrosS.remove(miUid)
                    transaction.update(miRef, "seguidores", misSeg)
                    transaction.update(otroRef, "siguiendo", otrosS)
                    null
                }.await()
                cargarUsuario()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // ====== BÚSQUEDA ======

    private val _resultadosBusqueda = MutableStateFlow<List<UsuarioData>>(emptyList())
    val resultadosBusqueda: StateFlow<List<UsuarioData>> = _resultadosBusqueda

    private var busquedaListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun buscarUsuarios(query: String, tagFiltro: String = "") {
        busquedaListener?.remove()
        if (query.isBlank() && tagFiltro.isBlank()) {
            _resultadosBusqueda.value = emptyList()
            return
        }
        var ref = db.collection("usuarios")
            .limit(30)
        if (tagFiltro.isNotBlank()) {
            ref = ref.whereArrayContains("etiquetas", tagFiltro)
        }
        busquedaListener = ref.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val q = query.lowercase()
            _resultadosBusqueda.value = snapshot.documents
                .mapNotNull { doc ->
                    val username = doc.getString("username") ?: return@mapNotNull null
                    if (q.isBlank() || username.lowercase().contains(q) ||
                        (doc.getString("bio")?.lowercase()?.contains(q) == true)) {
                        UsuarioData(
                            uid = doc.id,
                            username = username,
                            email = doc.getString("email") ?: "",
                            bio = doc.getString("bio") ?: "",
                            fotoPerfilUrl = doc.getString("fotoPerfilUrl") ?: "",
                            bannerUrl = doc.getString("bannerUrl") ?: "",
                            etiquetas = (doc.get("etiquetas") as? List<*>)
                                ?.filterIsInstance<String>() ?: emptyList(),
                            colorPrimario = doc.getLong("colorPrimario") ?: 0xFFFFFFFF,
                            colorSecundario = doc.getLong("colorSecundario") ?: 0xFF666666,
                        lucesActivas = doc.getBoolean("lucesActivas") ?: false,
                        latitud = doc.getDouble("latitud"),
                        longitud = doc.getDouble("longitud"),
                        seguidores = (doc.get("seguidores") as? List<*>)
                                ?.filterIsInstance<String>() ?: emptyList(),
                            siguiendo = (doc.get("siguiendo") as? List<*>)
                                ?.filterIsInstance<String>() ?: emptyList(),
                            esVerificado = (doc.getString("email") ?: "") == CREADOR_EMAIL,
                        apoyoBeta = doc.getBoolean("apoyoBeta") ?: false,
                        puntosApoyo = (doc.getLong("puntosApoyo") ?: 0L).toInt()
                        )
                    } else null
                }
                .filter { it.uid != auth.currentUser?.uid }
        }
    }

    fun detenerBusqueda() {
        busquedaListener?.remove()
        busquedaListener = null
        _resultadosBusqueda.value = emptyList()
    }

    fun refrescarBusqueda(query: String = "", tagFiltro: String = "") {
        if (query.isNotBlank() || tagFiltro.isNotBlank()) {
            buscarUsuarios(query, tagFiltro)
        } else {
            detenerBusqueda()
        }
    }

    fun cerrarSesion(onSuccess: () -> Unit = {}) {
        auth.signOut()
        _usuario.value = null
        onSuccess()
    }

    // ====== PREMIUM ======

    fun getSemanaISO(): Int {
        val cal = java.util.Calendar.getInstance()
        cal.firstDayOfWeek = java.util.Calendar.MONDAY
        return cal.get(java.util.Calendar.WEEK_OF_YEAR)
    }

    fun tienePremium(): Boolean {
        val u = _usuario.value ?: return false
        if (u.premiumVitalicio) return true
        if (!u.esPremium) return false
        return System.currentTimeMillis() < u.premiumHasta
    }

    fun verificarLimiteSemanal(): Boolean {
        val u = _usuario.value ?: return false
        if (tienePremium()) return true
        val semanaHoy = getSemanaISO()
        return if (semanaHoy != u.semanaActual) true
        else u.jamsEstaSemana < 10
    }

    fun getJamsRestantesSemana(): Int {
        val u = _usuario.value ?: return 0
        if (tienePremium()) return Int.MAX_VALUE
        val semanaHoy = getSemanaISO()
        if (semanaHoy != u.semanaActual) return 10
        return (10 - u.jamsEstaSemana).coerceAtLeast(0)
    }

    private fun reiniciarContadorSiNuevaSemana() {
        val u = _usuario.value ?: return
        val semanaHoy = getSemanaISO()
        if (semanaHoy != u.semanaActual) {
            _usuario.value = u.copy(semanaActual = semanaHoy, jamsEstaSemana = 0)
        }
    }

    fun incrementarContadorSemanal() {
        val uid = auth.currentUser?.uid ?: return
        reiniciarContadorSiNuevaSemana()
        val u = _usuario.value ?: return
        _usuario.value = u.copy(jamsEstaSemana = u.jamsEstaSemana + 1)
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(uid)
                    .update(mapOf(
                        "jamsEstaSemana" to (u.jamsEstaSemana + 1),
                        "semanaActual" to u.semanaActual
                    )).await()
            } catch (_: Exception) {}
        }
    }

    fun activarPremium(dias: Int, esVitalicio: Boolean = false) {
        val uid = auth.currentUser?.uid ?: return
        val hasta = if (esVitalicio) Long.MAX_VALUE
        else System.currentTimeMillis() + dias * 24L * 3600 * 1000
        viewModelScope.launch {
            try {
                val updates = mutableMapOf<String, Any>(
                    "esPremium" to true,
                    "premiumHasta" to hasta,
                    "premiumVitalicio" to esVitalicio
                )
                db.collection("usuarios").document(uid)
                    .set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
                cargarUsuario()
            } catch (_: Exception) {}
        }
    }

    fun registrarDonacion(puntos: Int) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val u = _usuario.value
                val esPrimeraCompra = u?.apoyoBeta != true

                val updates = mutableMapOf<String, Any>(
                    "puntosApoyo" to com.google.firebase.firestore.FieldValue.increment(puntos.toLong())
                )
                if (esPrimeraCompra) {
                    updates["esPremium"] = true
                    updates["premiumHasta"] = Long.MAX_VALUE
                    updates["premiumVitalicio"] = true
                    updates["apoyoBeta"] = true
                }
                db.collection("usuarios").document(uid)
                    .set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
                cargarUsuario()
            } catch (_: Exception) {}
        }
    }

    fun marcarIntroUsada() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(uid)
                    .update("introUsada", true).await()
                cargarUsuario()
            } catch (_: Exception) {}
        }
    }

    fun marcarPrimerMesUsado() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(uid)
                    .update("primerMesGratis", false).await()
                cargarUsuario()
            } catch (_: Exception) {}
        }
    }

    data class DonanteRanking(
        val uid: String,
        val username: String,
        val fotoUrl: String,
        val puntos: Int
    )

    private val _topDonantes = MutableStateFlow<List<DonanteRanking>>(emptyList())
    val topDonantes: StateFlow<List<DonanteRanking>> = _topDonantes

    fun cargarTopDonantes() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("usuarios")
                    .whereGreaterThan("puntosApoyo", 0)
                    .orderBy("puntosApoyo", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(50)
                    .get().await()
                _topDonantes.value = snapshot.documents.mapNotNull { doc ->
                    val pts = (doc.getLong("puntosApoyo") ?: 0L).toInt()
                    if (pts <= 0) return@mapNotNull null
                    DonanteRanking(
                        uid = doc.id,
                        username = doc.getString("username") ?: "Anónimo",
                        fotoUrl = doc.getString("fotoPerfilUrl") ?: "",
                        puntos = pts
                    )
                }
            } catch (_: Exception) {
                _topDonantes.value = emptyList()
            }
        }
    }
}