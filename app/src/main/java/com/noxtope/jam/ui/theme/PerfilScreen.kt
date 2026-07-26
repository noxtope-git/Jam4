package com.noxtope.jam.ui.theme

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.atan2
import kotlin.math.hypot

private fun decodificarBase64Perfil(base64: String): Bitmap? {
    return try {
        if (base64.isBlank()) null
        else {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PerfilScreen(
    userViewModel: UserViewModel = viewModel(),
    jamViewModel: JamViewModel = viewModel(),
    onCerrarSesion: () -> Unit = {},
    onCuentaEliminada: () -> Unit = {}
) {
    val context = LocalContext.current
    val authViewModel = remember { AuthViewModel() }
    val usuario by userViewModel.usuario.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val misJams by jamViewModel.misJams.collectAsState()
    val tagsGlobales by jamViewModel.tagsGlobales.collectAsState()
    val jamsActivos by jamViewModel.jamsActivos.collectAsState()
    val jamsHistorial by jamViewModel.jamsHistorial.collectAsState()
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    val currentUid = remember { auth.currentUser?.uid ?: "" }
    val activasFiltradas = remember(jamsActivos, misJams) {
        val idsMisJams = misJams.map { it.id }.toSet()
        jamsActivos.filter { j ->
            (j.asistentes.contains(currentUid) || j.creadoPor == currentUid) &&
                j.id !in idsMisJams
        }
    }
    val historialSinDuplicados = remember(jamsHistorial, misJams, activasFiltradas) {
        val idsExcluir = (misJams.map { it.id } + activasFiltradas.map { it.id }).toSet()
        jamsHistorial.filter { it.id !in idsExcluir }
    }

    var isEditing by remember { mutableStateOf(false) }
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }
    var mostrarDialogoEliminarCuenta by remember { mutableStateOf(false) }
    var mostrarDialogoEtiquetas by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var mostrarNombreReal by remember { mutableStateOf(false) }
    var mostrarEmail by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var bannerImageUri by remember { mutableStateOf<Uri?>(null) }

    var editColor by remember { mutableStateOf(Color.White) }
    var editSecundarioColor by remember { mutableStateOf(Color(0xFF666666)) }
    var editDarkMode by remember { mutableStateOf(true) }
    var editLuces by remember { mutableStateOf(false) }
    var editEtiquetas by remember { mutableStateOf(emptyList<String>()) }

    var origColor by remember { mutableStateOf(Color.White) }
    var origSecundarioColor by remember { mutableStateOf(Color(0xFF666666)) }
    var origDarkMode by remember { mutableStateOf(true) }
    var origLuces by remember { mutableStateOf(false) }
    var origEtiquetas by remember { mutableStateOf(emptyList<String>()) }
    var origUsername by remember { mutableStateOf("") }
    var origBio by remember { mutableStateOf("") }
    var origTelefono by remember { mutableStateOf("") }
    var origNombreReal by remember { mutableStateOf(false) }
    var origEmail by remember { mutableStateOf(false) }

    var mostrarDialogoConfirmar by remember { mutableStateOf(false) }

    // Estados para el diálogo de etiquetas
    var tagBusqueda by remember { mutableStateOf("") }
    var tagSeleccionTemp by remember { mutableStateOf(emptySet<String>()) }
    var mostrarDialogoCrearTag by remember { mutableStateOf(false) }
    var nuevaTag by remember { mutableStateOf("") }
    var mostrarSelectorColorSecundario by remember { mutableStateOf(false) }

    val profilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) profileImageUri = uri }

    val bannerPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) bannerImageUri = uri }

    LaunchedEffect(Unit) {
        userViewModel.cargarUsuario()
        jamViewModel.cargarMisJams()
        jamViewModel.cargarTags()
    }

    LaunchedEffect(usuario) {
        usuario?.let {
            username = it.username
            bio = it.bio
            telefono = it.telefono
            mostrarNombreReal = it.mostrarNombreReal
            mostrarEmail = it.mostrarEmail
            if (it.colorPrimario != 0L) editColor = Color(it.colorPrimario.toInt())
            if (it.colorSecundario != 0L) editSecundarioColor = Color(it.colorSecundario.toInt())
            else editSecundarioColor = calcularColorSecundario(editColor, editDarkMode)
            editDarkMode = it.modoOscuro
            editLuces = it.lucesActivas
            editEtiquetas = it.etiquetas
            jamViewModel.cargarHistorial(it.jamsHistorial)
        }
    }

    // Diálogo cerrar sesión
    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres salir?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoCerrarSesion = false
                    userViewModel.cerrarSesion { onCerrarSesion() }
                }) { Text("Salir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarSesion = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo eliminar cuenta
    if (mostrarDialogoEliminarCuenta) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminarCuenta = false },
            icon = { Icon(Icons.Filled.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar cuenta") },
            text = { Text("Esta acción es permanente. Se borrarán tu perfil, tus Jams y todos tus datos.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoEliminarCuenta = false
                    authViewModel.eliminarCuenta(
                        onSuccess = {
                            Toast.makeText(context, "Cuenta eliminada", Toast.LENGTH_SHORT).show()
                            onCuentaEliminada()
                        },
                        onError = { error -> Toast.makeText(context, error, Toast.LENGTH_LONG).show() }
                    )
                }) { Text("Eliminar definitivamente", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminarCuenta = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de gestión de etiquetas
    if (mostrarDialogoEtiquetas) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEtiquetas = false },
            title = { Text("Tus intereses") },
            text = {
                Column {
                    Text("Selecciona hasta 10 etiquetas", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tagBusqueda,
                        onValueChange = { tagBusqueda = it },
                        label = { Text("Buscar...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val tagsInfo = tagsGlobales
                    val tagFiltrados = if (tagBusqueda.isBlank()) tagsInfo
                    else tagsInfo.filter { it.nombre.contains(tagBusqueda, ignoreCase = true) }
                    val tagsRecomendadas = remember(tagsInfo) {
                        tagsInfo.sortedByDescending { it.usos }.take(8)
                    }

                    if (tagBusqueda.isBlank()) {
                        val recomFiltradas = tagsRecomendadas
                            .filter { !tagSeleccionTemp.contains(it.nombre) }
                        if (recomFiltradas.isNotEmpty()) {
                            Text("Más usadas", fontSize = 11.sp, color = Color.Gray,
                                fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                recomFiltradas.forEach { tagInfo ->
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            if (tagSeleccionTemp.size < 10)
                                                tagSeleccionTemp = tagSeleccionTemp + tagInfo.nombre
                                        },
                                        label = { Text("${tagInfo.nombre} (${tagInfo.usos})", fontSize = 11.sp) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        tagFiltrados.forEach { tagInfo ->
                            val sel = tagSeleccionTemp.contains(tagInfo.nombre)
                            FilterChip(
                                selected = sel,
                                onClick = {
                                    tagSeleccionTemp = if (sel) tagSeleccionTemp - tagInfo.nombre
                                    else if (tagSeleccionTemp.size < 10) tagSeleccionTemp + tagInfo.nombre
                                    else tagSeleccionTemp
                                },
                                label = { Text(tagInfo.nombre, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                    if (tagSeleccionTemp.size < 10) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { mostrarDialogoCrearTag = true }) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Crear nueva etiqueta", fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    editEtiquetas = tagSeleccionTemp.toList()
                    mostrarDialogoEtiquetas = false
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEtiquetas = false }) { Text("Cancelar", color = Color.Gray) }
            }
        )
    }

    // Diálogo confirmar guardado
    if (mostrarDialogoConfirmar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmar = false },
            icon = { Icon(Icons.Filled.Save, null, tint = Color(0xFF4CAF50)) },
            title = { Text("Guardar cambios") },
            text = { Text("¿Estás seguro de estos cambios?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoConfirmar = false
                    if (telefono != usuario?.telefono) userViewModel.actualizarTelefono(telefono)
                    userViewModel.guardarPerfil(
                        context = context, username = username, bio = bio,
                        etiquetas = editEtiquetas, mostrarNombreReal = mostrarNombreReal,
                        mostrarEmail = mostrarEmail, nuevaFotoUri = profileImageUri, nuevoBannerUri = bannerImageUri,
                        onSuccess = {
                            userViewModel.guardarPreferencias(
                                colorPrimario = editColor.toArgbLong(),
                                colorSecundario = editSecundarioColor.toArgbLong(),
                                modoOscuro = editDarkMode, lucesActivas = editLuces
                            )
                            origColor = editColor
                            origSecundarioColor = editSecundarioColor
                            origDarkMode = editDarkMode
                            origLuces = editLuces
                            origEtiquetas = editEtiquetas
                            origUsername = username
                            origBio = bio
                            origTelefono = telefono
                            origNombreReal = mostrarNombreReal
                            origEmail = mostrarEmail
                            isEditing = false
                            profileImageUri = null; bannerImageUri = null
                            Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error -> Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show() }
                    )
                }) { Text("Guardar", color = Color(0xFF4CAF50)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmar = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo crear etiqueta
    if (mostrarDialogoCrearTag) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCrearTag = false },
            title = { Text("Nueva etiqueta") },
            text = {
                Column {
                    Text("Se compartirá con todos los usuarios.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = nuevaTag, onValueChange = { nuevaTag = it },
                        label = { Text("Ej: Salsa, Jazz, Trap") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val tagLimpio = nuevaTag.trim()
                    if (tagLimpio.isNotBlank() && tagSeleccionTemp.size < 10) {
                        jamViewModel.agregarTagGlobal(
                            nuevoTag = tagLimpio,
                            onYaExiste = {
                                Toast.makeText(context, "Esta etiqueta ya existe, intenta buscarla", Toast.LENGTH_SHORT).show()
                            },
                            onAgregado = {
                                tagSeleccionTemp = tagSeleccionTemp + tagLimpio
                                tagBusqueda = tagLimpio
                            }
                        )
                    }
                    nuevaTag = ""
                    mostrarDialogoCrearTag = false
                }) { Text("Agregar") }
            },
            dismissButton = {
                TextButton(onClick = { nuevaTag = ""; mostrarDialogoCrearTag = false }) { Text("Cancelar", color = Color.Gray) }
            }
        )
    }

    // Selector de color secundario (paleta con secciones)
    if (mostrarSelectorColorSecundario) {
        SelectorColorDialog(
            titulo = "Elegir color secundario",
            colorActual = editSecundarioColor,
            onColorSelected = { color ->
                editSecundarioColor = color
                mostrarSelectorColorSecundario = false
            },
            onDismiss = { mostrarSelectorColorSecundario = false }
        )
    }

    // Layout principal
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isEditing) 80.dp else 0.dp)
        ) {
            // BANNER + FOTO + BOTÓN EDITAR
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) {
                    // Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = isEditing) {
                                bannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                    ) {
                        if (bannerImageUri != null) {
                            val inputStream = context.contentResolver.openInputStream(bannerImageUri!!)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            if (bitmap != null) Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Banner", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            val bannerBitmap = decodificarBase64Perfil(usuario?.bannerUrl ?: "")
                            if (bannerBitmap != null) Image(bitmap = bannerBitmap.asImageBitmap(), contentDescription = "Banner", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                        if (isEditing) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.CameraAlt, "Cambiar Banner", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }
                    }

                    // Botón editar perfil (modo no edición)
                    if (!isEditing) {
                        IconButton(
                            onClick = {
                                origColor = editColor
                                origSecundarioColor = editSecundarioColor
                                origDarkMode = editDarkMode
                                origLuces = editLuces
                                origEtiquetas = editEtiquetas
                                origUsername = username
                                origBio = bio
                                origTelefono = telefono
                                origNombreReal = mostrarNombreReal
                                origEmail = mostrarEmail
                                isEditing = true
                            },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(40.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(Icons.Filled.Edit, "Editar perfil", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }

                    // Botón cancelar edición
                    if (isEditing) {
                        TextButton(
                            onClick = { isEditing = false; userViewModel.cargarUsuario() },
                            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                        ) {
                            Text("Cancelar", color = Color.White, fontSize = 14.sp)
                        }

                        IconButton(
                            onClick = { mostrarDialogoCerrarSesion = true },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, "Cerrar sesión", tint = Color.White)
                        }
                    }

                    // Foto de perfil
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .align(Alignment.BottomCenter)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(4.dp, MaterialTheme.colorScheme.background, CircleShape)
                            .clickable(enabled = isEditing) {
                                profilePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageUri != null) {
                            val inputStream = context.contentResolver.openInputStream(profileImageUri!!)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            if (bitmap != null) Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            val fotoBitmap = decodificarBase64Perfil(usuario?.fotoPerfilUrl ?: "")
                            if (fotoBitmap != null) Image(bitmap = fotoBitmap.asImageBitmap(), contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else Icon(Icons.Filled.CameraAlt, "Sin foto", tint = Color.Gray, modifier = Modifier.size(40.dp))
                        }
                        if (isEditing) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.CameraAlt, "Cambiar foto", tint = Color.White)
                            }
                        }
                    }
                }
            }

            // INFO DEL USUARIO
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isEditing) {
                        // Formulario de edición
                        OutlinedTextField(value = username, onValueChange = { username = it },
                            label = { Text("@usuario") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = bio, onValueChange = { bio = it },
                            label = { Text("Biografía") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = telefono, onValueChange = { telefono = it.filter { c -> c.isDigit() } },
                            label = { Text("Teléfono") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(16.dp))

                        // Datos verificados (no editables)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Lock, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Datos verificados", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${usuario?.nombre ?: ""} ${usuario?.apellidos ?: ""} · ${usuario?.pais ?: ""}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // COLOR DE FONDO (3 fijos: negro/blanco/gris)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Modo oscuro", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                            Switch(checked = editDarkMode, onCheckedChange = { editDarkMode = it })
                        }
                        Text("Fondo: ${if (editDarkMode) "Negro · Gris oscuro" else "Blanco · Gris claro"}",
                            fontSize = 12.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(16.dp))

                        // COLOR SECUNDARIO (íconos, textos)
                        Text("Color de íconos y textos", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.align(Alignment.Start))
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp)).background(editSecundarioColor))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Color actual", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(editSecundarioColor.toHex(), fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { mostrarSelectorColorSecundario = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Elegir color", fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Privacidad
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Mostrar nombre real", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Switch(checked = mostrarNombreReal, onCheckedChange = { mostrarNombreReal = it })
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Mostrar correo", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Switch(checked = mostrarEmail, onCheckedChange = { mostrarEmail = it })
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Modo oscuro + luces
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Modo oscuro", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Switch(checked = editDarkMode, onCheckedChange = { editDarkMode = it })
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Modo luces 🪩", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Switch(checked = editLuces, onCheckedChange = { editLuces = it })
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Etiquetas / Intereses
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp).clickable {
                                    tagSeleccionTemp = editEtiquetas.toSet()
                                    tagBusqueda = ""
                                    mostrarDialogoEtiquetas = true
                                },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Intereses", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        if (editEtiquetas.isEmpty()) "Toca para añadir etiquetas"
                                        else editEtiquetas.joinToString(", "),
                                        fontSize = 11.sp, color = Color.Gray, maxLines = 1
                                    )
                                }
                                Icon(Icons.Filled.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Cerrar sesión + Eliminar cuenta
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            TextButton(
                                onClick = { mostrarDialogoCerrarSesion = true }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cerrar sesión", color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp)
                            }
                            TextButton(
                                onClick = { mostrarDialogoEliminarCuenta = true }
                            ) {
                                Icon(Icons.Filled.DeleteForever, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Eliminar cuenta", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                            }
                        }

                    } else {
                        // MODO VISUALIZACIÓN
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("@${username.ifBlank { "usuario" }}", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground)
                            if (usuario?.datosPersonalesCompletos == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Filled.Verified, "Verificado", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            if (usuario?.apoyoBeta == true) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("\u271A", fontSize = 20.sp, color = Color(0xFFE53935))
                            }
                        }
                        if (mostrarNombreReal && (usuario?.nombre?.isNotBlank() == true)) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${usuario?.nombre} ${usuario?.apellidos}", fontSize = 16.sp, color = Color.Gray)
                        }
                        if (mostrarEmail && usuario?.email?.isNotBlank() == true) {
                            Text(usuario?.email ?: "", fontSize = 14.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(bio.ifBlank { "Sin biografía aún..." }, fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }

            // ETIQUETAS (modo visualización)
            item {
                if (!isEditing && usuario?.etiquetas?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Tu vibra", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        usuario?.etiquetas?.forEach { etiqueta ->
                            AssistChip(onClick = {}, label = { Text(etiqueta) })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Tus Jams", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(12.dp))
            }

            // MIS JAMS (creadas)
            if (misJams.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                        Text("No has creado ninguna Jam", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(misJams, key = { it.id }) { jam ->
                    MiJamCard(jam = jam, jamViewModel = jamViewModel, esHistorial = false)
                }
            }

            // JAMS ACTIVAS
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Jams Activas", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (activasFiltradas.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                        Text("No estás en ninguna Jam activa", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(activasFiltradas, key = { it.id }) { jam ->
                    MiJamCard(jam = jam, jamViewModel = jamViewModel, esHistorial = false)
                }
            }

            // HISTORIAL
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Historial", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (historialSinDuplicados.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                        Text("Aún no hay Jams en tu historial", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(historialSinDuplicados, key = { it.id }) { jam ->
                    MiJamCard(jam = jam, jamViewModel = jamViewModel, esHistorial = true)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Botón sticky GUARDAR (verde cilíndrico, solo si hay cambios)
        val hayCambios = isEditing && (
            username != origUsername || bio != origBio || telefono != origTelefono ||
            mostrarNombreReal != origNombreReal || mostrarEmail != origEmail ||
            profileImageUri != null || bannerImageUri != null ||
            editColor != origColor || editSecundarioColor != origSecundarioColor ||
            editDarkMode != origDarkMode || editLuces != origLuces ||
            editEtiquetas != origEtiquetas
        )

        if (hayCambios) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = { mostrarDialogoConfirmar = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(50.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    else {
                        Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Cambios", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// RUEDA DE COLOR HSV COMPACTA — colores suaves tipo arcoíris, sin brillo
@Composable
fun RuedaColorHSVCompact(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val fixedBrightness = 0.78f
    val maxSaturation = 0.85f

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .pointerInput(fixedBrightness, maxSaturation) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val dist = hypot(dx, dy)
                            val radio = size.width / 2f
                            if (dist <= radio) {
                                var angulo = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (angulo < 0) angulo += 360f
                                val saturacion = ((dist / radio) * maxSaturation).coerceIn(0f, 1f)
                                onColorSelected(Color.hsv(angulo, saturacion, fixedBrightness))
                            }
                        }
                    }
            ) {
                val radio = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                for (angulo in 0 until 360 step 1) {
                    val color = Color.hsv(angulo.toFloat(), maxSaturation, fixedBrightness)
                    drawArc(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.hsv(angulo.toFloat(), 0f, fixedBrightness), color),
                            center = center,
                            radius = radio
                        ),
                        startAngle = angulo.toFloat(),
                        sweepAngle = 1.5f,
                        useCenter = true
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(selectedColor)
                    .border(3.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
fun MiJamCard(
    jam: JamData,
    jamViewModel: JamViewModel,
    esHistorial: Boolean = false
) {
    val context = LocalContext.current
    var mostrarMenu by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarDialogoQuitarHistorial by remember { mutableStateOf(false) }

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar Jam") },
            text = { Text("¿Seguro que quieres eliminar '${jam.titulo}'?") },
            confirmButton = {
                TextButton(onClick = {
                    jamViewModel.eliminarJam(jamId = jam.id, onSuccess = { Toast.makeText(context, "Jam eliminada", Toast.LENGTH_SHORT).show() }, onError = { error -> Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show() })
                    mostrarDialogoEliminar = false
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) { Text("Cancelar") }
            }
        )
    }

    if (mostrarDialogoQuitarHistorial) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoQuitarHistorial = false },
            title = { Text("Quitar del historial") },
            text = { Text("¿Quitar '${jam.titulo}' de tu historial?") },
            confirmButton = {
                TextButton(onClick = {
                    jamViewModel.eliminarDelHistorial(jam.id)
                    mostrarDialogoQuitarHistorial = false
                }) { Text("Quitar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoQuitarHistorial = false }) { Text("Cancelar") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val jamBitmap = remember(jam.imagenBase64) { decodificarBase64Perfil(jam.imagenBase64) }
            if (jamBitmap != null) {
                Image(bitmap = jamBitmap.asImageBitmap(), contentDescription = "Jam",
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                    Text("J", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(jam.titulo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                Text(
                    when { esHistorial -> "Finalizada"; jam.esPrivada -> "Privada"; !jam.visible -> "Oculta"; else -> "Pública" },
                    fontSize = 11.sp, color = when { esHistorial -> Color.Gray; jam.esPrivada -> MaterialTheme.colorScheme.primary; !jam.visible -> Color.Gray; else -> Color(0xFF4CAF50) }
                )
            }
            Box {
                IconButton(onClick = { mostrarMenu = true }) {
                    Text("•••", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
                DropdownMenu(expanded = mostrarMenu, onDismissRequest = { mostrarMenu = false }) {
                    if (esHistorial) {
                        DropdownMenuItem(
                            text = { Text("Quitar del historial", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { mostrarMenu = false; mostrarDialogoQuitarHistorial = true }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text(if (jam.visible) "Ocultar del feed" else "Mostrar en feed") },
                            leadingIcon = { Icon(if (jam.visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null) },
                            onClick = { jamViewModel.toggleVisibilidad(jam.id, !jam.visible); Toast.makeText(context, if (jam.visible) "Ocultada" else "Visible", Toast.LENGTH_SHORT).show(); mostrarMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text(if (jam.esPrivada) "Hacer pública" else "Hacer privada") },
                            leadingIcon = { Icon(if (jam.esPrivada) Icons.Filled.LockOpen else Icons.Filled.Lock, null) },
                            onClick = { jamViewModel.togglePrivacidad(jam.id, !jam.esPrivada); Toast.makeText(context, if (jam.esPrivada) "Pública" else "Privada", Toast.LENGTH_SHORT).show(); mostrarMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { mostrarMenu = false; mostrarDialogoEliminar = true }
                        )
                    }
                }
            }
        }
    }
}