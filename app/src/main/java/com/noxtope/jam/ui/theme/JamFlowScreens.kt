package com.noxtope.jam.ui.theme

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CrearJamScreen(
    onVolver: () -> Unit,
    jamViewModel: JamViewModel = viewModel(),
    userViewModel: UserViewModel? = null
) {
    val context = LocalContext.current
    val isCreating by jamViewModel.isCreating.collectAsState()
    val esPremium = userViewModel?.tienePremium() ?: true
    val maxParticipantesLimite = if (esPremium) 150 else 15

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var esPrivada by remember { mutableStateOf(false) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    val tituloError = titulo.isBlank()
    val descripcionError = descripcion.isBlank()
    val direccionError = direccion.isBlank()
    val puedeCrear = titulo.isNotBlank() && descripcion.isNotBlank() && direccion.isNotBlank()

    var mostrarMapa by remember { mutableStateOf(false) }
    var latitudSeleccionada by remember { mutableStateOf<Double?>(null) }
    var longitudSeleccionada by remember { mutableStateOf<Double?>(null) }
    var ubicacionActual by remember { mutableStateOf<GeoPoint?>(null) }
    var maxParticipantes by remember { mutableStateOf(
        if (esPremium) "50" else "15"
    ) }

    // Referencia al MapView para poder actualizarlo desde fuera
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    val tagsGlobales by jamViewModel.tagsGlobales.collectAsState()
    var etiquetasSeleccionadas by remember { mutableStateOf(setOf<String>()) }
    var mostrarDialogoEtiqueta by remember { mutableStateOf(false) }
    var busquedaTag by remember { mutableStateOf("") }
    var nuevaEtiqueta by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { jamViewModel.cargarTags() }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imagenUri = uri }
    )

    // Función para mover el mapa a un punto y poner marker
    fun moverMapaA(punto: GeoPoint, textoDir: String) {
        mapViewRef?.let { map ->
            latitudSeleccionada = punto.latitude
            longitudSeleccionada = punto.longitude
            direccion = textoDir

            map.overlays.removeIf { it is Marker }
            val marker = Marker(map)
            marker.position = punto
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = "Ubicación de la Jam"
            map.overlays.add(marker)
            map.controller.animateTo(punto)
            map.controller.setZoom(16.0)
            map.invalidate()
        }
    }

    // Función para buscar dirección por texto
    fun buscarDireccion(texto: String) {
        if (texto.length < 3) return
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val results = geocoder.getFromLocationName(texto, 1)
            if (!results.isNullOrEmpty()) {
                val addr = results[0]
                val punto = GeoPoint(addr.latitude, addr.longitude)
                val nombreDir = buildString {
                    if (!addr.thoroughfare.isNullOrBlank()) append(addr.thoroughfare)
                    if (!addr.subThoroughfare.isNullOrBlank()) append(" ${addr.subThoroughfare}")
                    if (!addr.locality.isNullOrBlank()) append(", ${addr.locality}")
                }.ifBlank { texto }
                moverMapaA(punto, nombreDir)
            } else {
                Toast.makeText(context, "No se encontró esa dirección", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error buscando dirección", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para obtener ubicación actual
    fun obtenerUbicacionActual() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val punto = GeoPoint(location.latitude, location.longitude)
                    ubicacionActual = punto
                    if (mostrarMapa) {
                        // Convertir coordenadas a dirección
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(
                                location.latitude, location.longitude, 1
                            )
                            val nombreDir = if (!addresses.isNullOrEmpty()) {
                                val addr = addresses[0]
                                buildString {
                                    if (!addr.thoroughfare.isNullOrBlank())
                                        append(addr.thoroughfare)
                                    if (!addr.subThoroughfare.isNullOrBlank())
                                        append(" ${addr.subThoroughfare}")
                                    if (!addr.locality.isNullOrBlank())
                                        append(", ${addr.locality}")
                                }.ifBlank {
                                    "${location.latitude}, ${location.longitude}"
                                }
                            } else {
                                "${location.latitude}, ${location.longitude}"
                            }
                            moverMapaA(punto, nombreDir)
                        } catch (e: Exception) {
                            moverMapaA(
                                punto,
                                "${location.latitude}, ${location.longitude}"
                            )
                        }
                    } else {
                        mostrarMapa = true
                    }
                } else {
                    ubicacionActual = GeoPoint(-33.4489, -70.6693)
                    if (!mostrarMapa) mostrarMapa = true
                    Toast.makeText(
                        context,
                        "No se pudo obtener tu ubicación, mostrando ubicación por defecto",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val aceptado = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (aceptado) {
            obtenerUbicacionActual()
        } else {
            Toast.makeText(
                context,
                "Necesitamos tu ubicación para mostrar el mapa",
                Toast.LENGTH_SHORT
            ).show()
            // Abrir mapa de todas formas en ubicación por defecto
            ubicacionActual = GeoPoint(-33.4489, -70.6693)
            mostrarMapa = true
        }
    }

    fun abrirMapa() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            obtenerUbicacionActual()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "¡Arma tu Jam!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Imagen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                .clickable {
                    imagePicker.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (imagenUri != null) {
                val inputStream = context.contentResolver.openInputStream(imagenUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Imagen Jam",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = "Cambiar imagen",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = "Subir imagen",
                        tint = Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Toca para subir imagen de la Jam",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título de la Jam *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = tituloError,
            supportingText = if (tituloError) { { Text("Campo obligatorio", color = MaterialTheme.colorScheme.error) } } else null
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción *") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4,
            isError = descripcionError,
            supportingText = if (descripcionError) { { Text("Campo obligatorio", color = MaterialTheme.colorScheme.error) } } else null
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Campo dirección con botón buscar y botón mapa
        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            label = { Text("Dirección * (solo visible para aceptados)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = direccionError,
            supportingText = if (direccionError) { { Text("Campo obligatorio", color = MaterialTheme.colorScheme.error) } } else null,
            trailingIcon = {
                Row {
                    // Botón buscar dirección en el mapa
                    IconButton(onClick = {
                        if (!mostrarMapa) abrirMapa()
                        if (direccion.isNotBlank()) {
                            buscarDireccion(direccion)
                        }
                    }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Buscar dirección",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Botón abrir/centrar mapa
                    IconButton(onClick = { abrirMapa() }) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = "Abrir mapa",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )

        // Mapa
        if (mostrarMapa) {
            Spacer(modifier = Modifier.height(12.dp))

            // Barra de acciones del mapa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Toca el mapa para marcar la ubicación",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                // Botón mi ubicación
                IconButton(onClick = { obtenerUbicacionActual() }) {
                    Icon(
                        Icons.Filled.MyLocation,
                        contentDescription = "Mi ubicación",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            AndroidView(
                factory = { ctx ->
                    Configuration.getInstance().userAgentValue = ctx.packageName
                    val mapView = MapView(ctx)
                    mapView.setTileSource(TileSourceFactory.MAPNIK)
                    mapView.setMultiTouchControls(true)
                    val startPoint = ubicacionActual ?: GeoPoint(-33.4489, -70.6693)
                    mapView.controller.setZoom(15.0)
                    mapView.controller.setCenter(startPoint)

                    // Marker de ubicación actual
                    if (ubicacionActual != null) {
                        val miUbicacion = Marker(mapView)
                        miUbicacion.position = ubicacionActual
                        miUbicacion.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        miUbicacion.title = "Tu ubicación"
                        miUbicacion.snippet = "Estás aquí"
                        mapView.overlays.add(miUbicacion)
                    }

                    // Tap para seleccionar ubicación
                    mapView.overlays.add(object : org.osmdroid.views.overlay.Overlay() {
                        override fun onSingleTapConfirmed(
                            e: android.view.MotionEvent,
                            mapView: MapView
                        ): Boolean {
                            val projection = mapView.projection
                            val geoPoint = projection.fromPixels(
                                e.x.toInt(), e.y.toInt()
                            ) as GeoPoint

                            latitudSeleccionada = geoPoint.latitude
                            longitudSeleccionada = geoPoint.longitude

                            // Quitar markers de selección pero mantener el de ubicación actual
                            mapView.overlays.removeIf {
                                it is Marker && it.title != "Tu ubicación"
                            }
                            val marker = Marker(mapView)
                            marker.position = geoPoint
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = "Ubicación de la Jam"
                            mapView.overlays.add(marker)
                            mapView.invalidate()

                            try {
                                val geocoder = Geocoder(ctx, Locale.getDefault())
                                val addresses = geocoder.getFromLocation(
                                    geoPoint.latitude, geoPoint.longitude, 1
                                )
                                if (!addresses.isNullOrEmpty()) {
                                    val address = addresses[0]
                                    direccion = buildString {
                                        if (!address.thoroughfare.isNullOrBlank())
                                            append(address.thoroughfare)
                                        if (!address.subThoroughfare.isNullOrBlank())
                                            append(" ${address.subThoroughfare}")
                                        if (!address.locality.isNullOrBlank())
                                            append(", ${address.locality}")
                                    }.ifBlank {
                                        "${geoPoint.latitude}, ${geoPoint.longitude}"
                                    }
                                }
                            } catch (ex: Exception) {
                                direccion =
                                    "${geoPoint.latitude}, ${geoPoint.longitude}"
                            }
                            return true
                        }
                    })

                    mapViewRef = mapView
                    mapView
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            if (latitudSeleccionada != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📍", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            direccion,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Etiquetas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Etiquetas",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${etiquetasSeleccionadas.size}/5",
                color = if (etiquetasSeleccionadas.size >= 5)
                    MaterialTheme.colorScheme.error else Color.Gray,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = busquedaTag,
            onValueChange = { busquedaTag = it },
            placeholder = { Text("Buscar o crear etiqueta...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (busquedaTag.isNotBlank() && etiquetasSeleccionadas.size < 5) {
                    IconButton(onClick = {
                        nuevaEtiqueta = busquedaTag
                        mostrarDialogoEtiqueta = true
                    }) {
                        Icon(Icons.Filled.Add, "Crear etiqueta",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        val tagsFiltrados = remember(busquedaTag, tagsGlobales) {
            if (busquedaTag.isBlank()) tagsGlobales
            else tagsGlobales.filter {
                it.nombre.contains(busquedaTag, ignoreCase = true)
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (busquedaTag.isBlank()) {
                val recomendadas = tagsGlobales
                    .sortedByDescending { it.usos }
                    .take(6)
                    .filter { !etiquetasSeleccionadas.contains(it.nombre) }
                if (recomendadas.isNotEmpty()) {
                    recomendadas.forEach { tagInfo ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                if (etiquetasSeleccionadas.size < 5)
                                    etiquetasSeleccionadas = etiquetasSeleccionadas + tagInfo.nombre
                            },
                            label = { Text(tagInfo.nombre, fontSize = 12.sp) }
                        )
                    }
                }
            }
            tagsFiltrados.forEach { tagInfo ->
                val isSelected = etiquetasSeleccionadas.contains(tagInfo.nombre)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        etiquetasSeleccionadas = if (isSelected)
                            etiquetasSeleccionadas - tagInfo.nombre
                        else if (etiquetasSeleccionadas.size < 5)
                            etiquetasSeleccionadas + tagInfo.nombre
                        else etiquetasSeleccionadas
                    },
                    label = {
                        Text(
                            tagInfo.nombre,
                            fontSize = 12.sp,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onBackground
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
            if (etiquetasSeleccionadas.size < 5) {
                InputChip(
                    selected = false,
                    onClick = { mostrarDialogoEtiqueta = true },
                    label = { Text("+ Nueva", color = MaterialTheme.colorScheme.onBackground) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacidad
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Jam Privada",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Solo visible para invitados",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = esPrivada,
                    onCheckedChange = { esPrivada = it }
                )
            }
        }

        // Límite participantes
        OutlinedTextField(
            value = maxParticipantes,
            onValueChange = { input ->
                val num = input.filter { it.isDigit() }
                if (num.isEmpty() || (num.toIntOrNull() ?: 0) in 1..maxParticipantesLimite) {
                    maxParticipantes = num
                }
            },
            label = { Text("Límite de participantes (1-$maxParticipantesLimite)") },
            supportingText = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${maxParticipantes.toIntOrNull() ?: 15} personas",
                        fontSize = 12.sp, color = Color.Gray)
                    if (!esPremium) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("· Máx: $maxParticipantesLimite (apoya para m\u00e1s)",
                            fontSize = 11.sp, color = Color(0xFFFF9800))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                when {
                    titulo.isBlank() ->
                        Toast.makeText(context, "Agrega un título", Toast.LENGTH_SHORT).show()
                    descripcion.isBlank() ->
                        Toast.makeText(
                            context, "Agrega una descripción", Toast.LENGTH_SHORT
                        ).show()
                    direccion.isBlank() ->
                        Toast.makeText(
                            context,
                            "Selecciona una ubicación en el mapa",
                            Toast.LENGTH_SHORT
                        ).show()
                    else -> {
                        jamViewModel.crearJamConImagen(
                            context = context,
                            titulo = titulo,
                            descripcion = descripcion,
                            direccion = direccion,
                            etiquetas = etiquetasSeleccionadas.toList(),
                            maxParticipantes = maxParticipantes.toIntOrNull()?.coerceIn(1, maxParticipantesLimite) ?: 15,
                            esPrivada = esPrivada,
                            imagenUri = imagenUri,
                            latitud = latitudSeleccionada,
                            longitud = longitudSeleccionada,
                            onSuccess = {
                                Toast.makeText(
                                    context, "¡Jam publicada!", Toast.LENGTH_SHORT
                                ).show()
                                onVolver()
                            },
                            onError = { error ->
                                Toast.makeText(
                                    context, "Error: $error", Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isCreating && puedeCrear
        ) {
            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Publicar Jam",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (mostrarDialogoEtiqueta) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEtiqueta = false },
            title = { Text("Nueva etiqueta") },
            text = {
                Column {
                    Text("Se compartirá con todos los usuarios.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nuevaEtiqueta,
                        onValueChange = { nuevaEtiqueta = it },
                        label = { Text("Ej: Reggaeton, Cumbia, Jazz") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val exists = tagsGlobales.any {
                        it.nombre.equals(nuevaEtiqueta.trim(), ignoreCase = true)
                    }
                    if (nuevaEtiqueta.isNotBlank() && exists) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Ya existe una etiqueta con ese nombre",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nuevaEtiqueta.isNotBlank() && etiquetasSeleccionadas.size < 5) {
                        val tagLimpio = nuevaEtiqueta.trim()
                        val yaExiste = tagsGlobales.any {
                            it.nombre.equals(tagLimpio, ignoreCase = true)
                        }
                        if (yaExiste) {
                            etiquetasSeleccionadas = etiquetasSeleccionadas + tagLimpio
                        } else {
                            jamViewModel.agregarTagGlobal(
                                nuevoTag = tagLimpio,
                                onAgregado = {
                                    etiquetasSeleccionadas = etiquetasSeleccionadas + tagLimpio
                                }
                            )
                        }
                    }
                    nuevaEtiqueta = ""
                    busquedaTag = ""
                    mostrarDialogoEtiqueta = false
                }) { Text("Agregar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    nuevaEtiqueta = ""
                    mostrarDialogoEtiqueta = false
                }) { Text("Cancelar", color = Color.Gray) }
            }
        )
    }
}

// ====== INVITADOS + SOLICITUDES (creador) ======
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InvitadosScreen(
    jam: JamData,
    jamViewModel: JamViewModel,
    onVolver: () -> Unit,
    onVerPerfil: (String) -> Unit = {},
    onNavigateToGestionar: ((JamData) -> Unit)? = null
) {
    val ctx = LocalContext.current
    var tabIndex by remember { mutableIntStateOf(0) }
    val jams by jamViewModel.jamsActivos.collectAsState()
    val jamActual = jams.find { it.id == jam.id } ?: jam
    val asistentes = jamActual.asistentes

    // Batch load all user data
    val usuariosData = remember(asistentes) {
        mutableStateOf<Map<String, Map<String, Any>>>(emptyMap())
    }
    LaunchedEffect(asistentes) {
        if (asistentes.isEmpty()) return@LaunchedEffect
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .whereIn(FieldPath.documentId(), asistentes)
                .get().await()
            val data = mutableMapOf<String, Map<String, Any>>()
            for (doc in snapshot.documents) {
                val d = doc.data as? Map<String, Any>
                if (d != null) data[doc.id] = d
            }
            usuariosData.value = data
        } catch (_: Exception) {}
    }

    var solicitudes by remember { mutableStateOf<List<JamViewModel.SolicitudData>>(emptyList()) }
    LaunchedEffect(jam.id) {
        jamViewModel.obtenerSolicitudesDeJam(jam.id) { result ->
            solicitudes = result
        }
    }

    var uidAExpulsar by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(jam.titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(4.dp))
        Text("${asistentes.size}/${jamActual.maxParticipantes} participantes",
            fontSize = 13.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = tabIndex) {
            Tab(
                selected = tabIndex == 0,
                onClick = { tabIndex = 0 },
                text = { Text("Invitados") }
            )
            Tab(
                selected = tabIndex == 1,
                onClick = { tabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Solicitudes")
                        if (solicitudes.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Badge { Text("${solicitudes.size}",
                                fontSize = 10.sp) }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (tabIndex) {
            0 -> {
                if (asistentes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center) {
                        Text("Sin invitados aún", color = Color.Gray)
                    }
                } else {
                    LazyColumn {
                        items(asistentes, key = { it }) { uid ->
                            val userDoc = usuariosData.value[uid]
                            AsistenteRow(
                                userId = uid,
                                userDoc = userDoc,
                                esCreador = uid == jamActual.creadoPor,
                                esAdmin = uid in jamActual.admins,
                                showFullInfo = true,
                                onVerPerfil = { puid -> onVerPerfil(puid) },
                                onEliminarDeJam = if (uid != jamActual.creadoPor) {
                                    { uidAExpulsar = uid }
                                } else null,
                                onDarAdmin = if (uid != jamActual.creadoPor) {
                                    {
                                        if (uid in jamActual.admins) {
                                            jamViewModel.quitarAdmin(jamActual.id, uid)
                                        } else {
                                            jamViewModel.hacerAdmin(jamActual.id, uid)
                                        }
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
            1 -> {
                if (solicitudes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center) {
                        Text("Sin solicitudes pendientes", color = Color.Gray)
                    }
                } else {
                    LazyColumn {
                        items(solicitudes, key = { it.jamId + "_" + it.usuarioId }) { sol ->
                            SolicitanteCard(
                                solicitud = sol,
                                onAceptar = {
                                    jamViewModel.responderSolicitud(
                                        jamId = jamActual.id,
                                        userIdB = sol.usuarioId,
                                        aceptar = true,
                                        onSuccess = {
                                            Toast.makeText(ctx,
                                                "Solicitud aceptada",
                                                Toast.LENGTH_SHORT).show()
                                            solicitudes = solicitudes - sol
                                        },
                                        onError = {
                                            Toast.makeText(ctx,
                                                "Error: $it", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                                onRechazar = {
                                    jamViewModel.responderSolicitud(
                                        jamId = jamActual.id,
                                        userIdB = sol.usuarioId,
                                        aceptar = false,
                                        onSuccess = {
                                            Toast.makeText(ctx,
                                                "Solicitud rechazada",
                                                Toast.LENGTH_SHORT).show()
                                            solicitudes = solicitudes - sol
                                        },
                                        onError = {
                                            Toast.makeText(ctx,
                                                "Error: $it", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onVolver,
            modifier = Modifier.fillMaxWidth()) {
            Text("Volver", color = Color.Gray)
        }
    }

    if (uidAExpulsar != null) {
        AlertDialog(
            onDismissRequest = { uidAExpulsar = null },
            title = { Text("Expulsar participante") },
            text = { Text("¿Seguro que quieres expulsar a este usuario de la Jam? No podrá volver a unirse.") },
            confirmButton = {
                TextButton(onClick = {
                    val uid = uidAExpulsar!!
                    uidAExpulsar = null
                    jamViewModel.expulsarParticipante(jamActual.id, uid,
                        onSuccess = {
                            Toast.makeText(ctx, "Usuario expulsado", Toast.LENGTH_SHORT).show()
                        },
                        onError = {
                            Toast.makeText(ctx, "Error: $it", Toast.LENGTH_SHORT).show()
                        }
                    )
                }) { Text("Expulsar", color = Color(0xFFF44336)) }
            },
            dismissButton = {
                TextButton(onClick = { uidAExpulsar = null }) {
                    Text("Cancelar", color = Color.Gray) }
            }
        )
    }
}

@Composable
private fun SolicitanteCard(
    solicitud: JamViewModel.SolicitudData,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    val userId = solicitud.usuarioId
    val fotoUrl = solicitud.fotoUrl
    val username = solicitud.username
    val bio = solicitud.bio
    val tags = solicitud.etiquetas

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarIniciales(
                    nombre = username,
                    fotoBase64 = fotoUrl,
                    modifier = Modifier.size(56.dp),
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(username, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledTonalButton(
                        onClick = onAceptar,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f)),
                        modifier = Modifier.size(44.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("✓", fontSize = 18.sp, color = Color(0xFF4CAF50)) }
                    Spacer(modifier = Modifier.height(4.dp))
                    FilledTonalButton(
                        onClick = onRechazar,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFF44336).copy(alpha = 0.15f)),
                        modifier = Modifier.size(44.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("✗", fontSize = 18.sp, color = Color(0xFFF44336)) }
                }
            }
            if (bio.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(bio.take(120) + if (bio.length > 120) "..." else "",
                    fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
            }
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    tags.take(6).forEach { tag ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(tag, fontSize = 10.sp) },
                            modifier = Modifier.height(26.dp)
                        )
                    }
                    if (tags.size > 6) {
                        Text("+${tags.size - 6}", fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }
            }
        }
    }
}

// ====== GESTIONAR JAM (creador) ======
@Composable
fun GestionarJamScreen(
    jam: JamData,
    jamViewModel: JamViewModel,
    onVolver: () -> Unit,
    onInvitados: () -> Unit = {},
    onChat: () -> Unit = {},
    onVerPerfil: (String) -> Unit = {}
) {
    val ctx = LocalContext.current
    var editDireccion by remember { mutableStateOf(jam.direccion) }
    var editDescripcion by remember { mutableStateOf(jam.descripcion) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var accionPendiente by remember { mutableStateOf("") }

    // Batch load user data for all participants
    val gestUserData = remember(jam.asistentes) {
        mutableStateOf<Map<String, Map<String, Any>>>(emptyMap())
    }
    LaunchedEffect(jam.asistentes) {
        if (jam.asistentes.isEmpty()) return@LaunchedEffect
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .whereIn(FieldPath.documentId(), jam.asistentes)
                .get().await()
            val data = mutableMapOf<String, Map<String, Any>>()
            for (doc in snapshot.documents) {
                val d = doc.data as? Map<String, Any>
                if (d != null) data[doc.id] = d
            }
            gestUserData.value = data
        } catch (_: Exception) {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Gestionar Jam",
            fontSize = 22.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))

        Text(jam.titulo, fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        // LISTA DE INVITADOS (prominente)
        Button(
            onClick = onInvitados,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50))
        ) {
            Text("Lista de invitados",
                color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onChat,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Chat grupal",
                fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Configuración", fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = editDescripcion,
            onValueChange = { editDescripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = editDireccion,
            onValueChange = { editDireccion = it },
            label = { Text("Dirección") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                jamViewModel.actualizarJam(jam.id, mapOf(
                    "descripcion" to editDescripcion,
                    "direccion" to editDireccion
                ))
                Toast.makeText(ctx, "Jam actualizada", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary)
        ) { Text("Guardar cambios",
            color = MaterialTheme.colorScheme.onPrimary) }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Participantes (${jam.asistentes.size}/${jam.maxParticipantes})",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        for (uid in jam.asistentes) {
            AsistenteRow(
                userId = uid,
                userDoc = gestUserData.value[uid],
                esCreador = uid == jam.creadoPor,
                esAdmin = uid in jam.admins,
                onVerPerfil = { puid -> onVerPerfil(puid) },
                onEliminarDeJam = if (uid != jam.creadoPor) {
                    {
                        jamViewModel.expulsarParticipante(jam.id, uid,
                            onSuccess = { Toast.makeText(ctx, "Usuario eliminado de la Jam", Toast.LENGTH_SHORT).show() },
                            onError = { Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show() })
                    }
                } else null,
                onDarAdmin = if (uid != jam.creadoPor) {
                    {
                        if (uid in jam.admins) {
                            jamViewModel.quitarAdmin(jam.id, uid)
                        } else {
                            jamViewModel.hacerAdmin(jam.id, uid)
                        }
                    }
                } else null
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { accionPendiente = "terminar"; mostrarConfirmacion = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800))
        ) { Text("Terminar Jam",
            color = Color.White) }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { accionPendiente = "eliminar"; mostrarConfirmacion = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF44336))
        ) { Text("Eliminar Jam",
            color = Color.White) }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onVolver,
            modifier = Modifier.fillMaxWidth()) {
            Text("Volver", color = Color.Gray) }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text(
                if (accionPendiente == "terminar") "Terminar Jam"
                else "Eliminar Jam") },
            text = { Text(
                if (accionPendiente == "terminar")
                    "Esto marcará la Jam como terminada. Los participantes ya no podrán acceder."
                else "¿Seguro? No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacion = false
                    if (accionPendiente == "terminar") {
                        jamViewModel.terminarJam(jam.id,
                            onSuccess = {
                                Toast.makeText(ctx, "Jam terminada", Toast.LENGTH_SHORT).show()
                                onVolver()
                            },
                            onError = { Toast.makeText(ctx, "Error: $it", Toast.LENGTH_LONG).show() }
                        )
                    } else {
                        jamViewModel.eliminarJam(jam.id,
                            onSuccess = {
                                Toast.makeText(ctx, "Jam eliminada", Toast.LENGTH_SHORT).show()
                                onVolver()
                            },
                            onError = { Toast.makeText(ctx, "Error: $it", Toast.LENGTH_LONG).show() }
                        )
                    }
                }) { Text("Confirmar",
                    color = Color(0xFFF44336)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar", color = Color.Gray) }
            }
        )
    }
}

@Composable
private fun AsistenteRow(
    userId: String,
    userDoc: Map<String, Any>? = null,
    esCreador: Boolean,
    esAdmin: Boolean = false,
    showFullInfo: Boolean = false,
    onExpulsar: (() -> Unit)? = null,
    onVerPerfil: ((String) -> Unit)? = null,
    onEliminarDeJam: ((String) -> Unit)? = null,
    onDarAdmin: ((String) -> Unit)? = null
) {
    var internalDoc by remember(userId) { mutableStateOf<Map<String, Any>?>(null) }
    val doc = userDoc ?: internalDoc
    LaunchedEffect(userId) {
        if (userDoc == null) {
            try {
                val snap = FirebaseFirestore.getInstance()
                    .collection("usuarios").document(userId).get().await()
                internalDoc = snap.data as? Map<String, Any>
            } catch (_: Exception) {}
        }
    }
    val fotoUrl = doc?.get("fotoPerfilUrl") as? String ?: ""
    val username = doc?.get("username") as? String ?: "Cargando..."
    val bio = if (showFullInfo) doc?.get("bio") as? String ?: "" else ""

    var mostrarMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = if (showFullInfo) 8.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarIniciales(
            nombre = username,
            fotoBase64 = fotoUrl,
            modifier = Modifier.size(if (showFullInfo) 40.dp else 32.dp),
            fontSize = if (showFullInfo) 16.sp else 14.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(username, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                if (doc?.get("apoyoBeta") == true) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("\u271A", fontSize = 14.sp, color = Color(0xFFE53935))
                }
            }
            if (esCreador) {
                Text("Creador", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
            } else if (esAdmin) {
                Text("Admin", fontSize = 10.sp, color = Color(0xFFFF9800))
            }
            if (showFullInfo && bio.isNotBlank()) {
                Text(bio.take(80) + if (bio.length > 80) "..." else "",
                    fontSize = 11.sp, color = Color.Gray)
            }
        }
        Box {
            IconButton(onClick = { mostrarMenu = true }) {
                Text("•••", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DropdownMenu(
                expanded = mostrarMenu,
                onDismissRequest = { mostrarMenu = false }
            ) {
                if (onVerPerfil != null) {
                    DropdownMenuItem(
                        text = { Text("Ver perfil") },
                        onClick = { mostrarMenu = false; onVerPerfil(userId) },
                        leadingIcon = { Icon(Icons.Filled.Person, null) }
                    )
                }
                if (onDarAdmin != null && !esCreador) {
                    DropdownMenuItem(
                        text = { Text(if (esAdmin) "Quitar admin" else "Dar admin") },
                        onClick = { mostrarMenu = false; onDarAdmin(userId) },
                        leadingIcon = { Icon(Icons.Filled.AdminPanelSettings, null) }
                    )
                }
                if (onEliminarDeJam != null && !esCreador) {
                    DropdownMenuItem(
                        text = { Text("Eliminar de la Jam", color = Color(0xFFF44336)) },
                        onClick = { mostrarMenu = false; onEliminarDeJam(userId) },
                        leadingIcon = { Icon(Icons.Filled.PersonRemove, null, tint = Color(0xFFF44336)) }
                    )
                }
            }
        }
    }
}

// ====== CHAT GRUPAL ======
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    jamId: String,
    jamTitulo: String,
    jamViewModel: JamViewModel,
    onVolver: () -> Unit
) {
    val ctx = LocalContext.current
    var mostrarAdvertencia by remember { mutableStateOf(true) }
    val mensajes by jamViewModel.mensajesChat.collectAsState()
    var textoMensaje by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var enviandoImagen by remember { mutableStateOf(false) }

    var busquedaMensaje by remember { mutableStateOf("") }
    var mostrarBusqueda by remember { mutableStateOf(false) }

    val mensajesFiltrados by remember(mensajes, busquedaMensaje) {
        derivedStateOf {
            if (busquedaMensaje.isBlank()) mensajes
            else mensajes.filter {
                it.texto.contains(busquedaMensaje, ignoreCase = true) ||
                it.username.contains(busquedaMensaje, ignoreCase = true)
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                imagenUri = uri
                enviandoImagen = true
                jamViewModel.enviarMensajeConImagenStorage(
                    context = ctx,
                    jamId = jamId,
                    uri = uri,
                    texto = textoMensaje.trim(),
                    onSuccess = {
                        textoMensaje = ""
                        imagenUri = null
                        enviandoImagen = false
                    },
                    onError = {
                        Toast.makeText(ctx, "Error al enviar imagen", Toast.LENGTH_SHORT).show()
                        enviandoImagen = false
                    }
                )
            }
        }
    )

    if (mostrarAdvertencia) {
        AlertDialog(
            onDismissRequest = { onVolver() },
            title = { Text("Aviso de privacidad") },
            text = {
                Text("No compartas datos personales (teléfono, dirección exacta, redes sociales) en este chat. " +
                        "Esta sala es para coordinar la Jam. Si alguien te pide información sensible, repórtalo.")
            },
            confirmButton = {
                TextButton(onClick = { mostrarAdvertencia = false }) {
                    Text("Entendido, entrar") }
            },
            dismissButton = {
                TextButton(onClick = onVolver) {
                    Text("Salir", color = Color.Gray) }
            }
        )
        return
    }

    LaunchedEffect(jamId) { jamViewModel.cargarMensajes(jamId) }
    DisposableEffect(Unit) { onDispose { jamViewModel.detenerChat() } }

    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty() && busquedaMensaje.isBlank()) {
            listState.animateScrollToItem(mensajes.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onVolver) { Text("←", fontSize = 20.sp) }
            Text("Chat: $jamTitulo",
                fontWeight = FontWeight.Bold, fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f))
            IconButton(onClick = { mostrarBusqueda = !mostrarBusqueda }) {
                Icon(Icons.Filled.Search, "Buscar mensaje",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Search bar
        if (mostrarBusqueda) {
            OutlinedTextField(
                value = busquedaMensaje,
                onValueChange = { busquedaMensaje = it },
                placeholder = { Text("Buscar en el chat...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (busquedaMensaje.isNotBlank()) {
                        TextButton(onClick = { busquedaMensaje = "" },
                            contentPadding = PaddingValues(0.dp)) {
                            Text("✕", fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            )
        }

        // Messages (centrados, estilo username: mensaje)
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (mensajesFiltrados.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center) {
                        Text(
                            if (busquedaMensaje.isNotBlank()) "Sin resultados"
                            else "Sin mensajes aún. ¡Sé el primero!",
                            color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
            items(mensajesFiltrados, key = { it.id }) { msg ->
                val esMio = msg.usuarioId == FirebaseAuth.getInstance().currentUser?.uid
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Username (con color del usuario, animado si tiene modo arcoíris)
                    val nombreColor = if (msg.lucesActivas) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val hue by infiniteTransition.animateFloat(
                            initialValue = 0f, targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            )
                        )
                        Color.hsv(hue, 0.8f, 0.9f)
                    } else if (msg.colorSecundario != 0L) Color(msg.colorSecundario.toInt())
                    else MaterialTheme.colorScheme.onSurfaceVariant
                    Text(
                        msg.username,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = nombreColor,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    // Globo del mensaje
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 14.dp, topEnd = 14.dp,
                            bottomStart = 4.dp, bottomEnd = 14.dp
                        ),
                        color = if (esMio) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            if (msg.imagenBase64.isNotBlank()) {
                                val esUrl = msg.imagenBase64.startsWith("http")
                                val bitmap = remember(msg.imagenBase64) {
                                    if (esUrl) null else try {
                                        val bytes = android.util.Base64.decode(msg.imagenBase64, android.util.Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    } catch (e: Exception) { null }
                                }
                                if (bitmap != null || esUrl) {
                                    Box {
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Imagen compartida",
                                                modifier = Modifier
                                                    .widthIn(max = 200.dp)
                                                    .heightIn(max = 260.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Fit
                                            )
                                        } else {
                                            AsyncImage(
                                                model = msg.imagenBase64,
                                                contentDescription = "Imagen compartida",
                                                modifier = Modifier
                                                    .widthIn(max = 200.dp)
                                                    .heightIn(max = 260.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                        if (bitmap != null) {
                                            IconButton(
                                                onClick = {
                                                    try {
                                                        val values = android.content.ContentValues().apply {
                                                            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME,
                                                                "Jam4_${System.currentTimeMillis()}.jpg")
                                                            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                                                        }
                                                        val uri = ctx.contentResolver.insert(
                                                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                                                        if (uri != null) {
                                                            ctx.contentResolver.openOutputStream(uri)?.use { out ->
                                                                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                                                            }
                                                            Toast.makeText(ctx, "Imagen guardada en galería", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } catch (e: Exception) {
                                                        Toast.makeText(ctx, "Error al guardar", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                                            ) {
                                                Text("⬇", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            if (msg.texto.isNotBlank()) {
                                Text(msg.texto, fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Input
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { imagePicker.launch("image/*") }) {
                Icon(Icons.Filled.CameraAlt, "Adjuntar imagen",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedTextField(
                value = textoMensaje,
                onValueChange = { textoMensaje = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje...") },
                singleLine = true,
                enabled = !enviandoImagen,
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (textoMensaje.isNotBlank()) {
                        jamViewModel.enviarMensaje(jamId, textoMensaje.trim())
                        textoMensaje = ""
                    }
                },
                contentPadding = PaddingValues(horizontal = 16.dp),
                enabled = !enviandoImagen,
                shape = RoundedCornerShape(24.dp)
            ) { Text("Enviar") }
        }
    }
}