package com.noxtope.jam.ui.theme

import android.graphics.BitmapFactory
import android.util.Base64
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.noxtope.jam.ui.theme.JamIconSmall
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    onCreateJamClick: () -> Unit,
    onCerrarSesion: () -> Unit = {},
    onCuentaEliminada: () -> Unit = {},
    onNavigateToGestionar: (JamData) -> Unit = {},
    onNavigateToInvitados: (JamData) -> Unit = {},
    onNavigateToChat: (JamData) -> Unit = {},
    onNavigateToPerfilPublico: (String) -> Unit = {},
    onNavigateToDetalleJam: (JamData) -> Unit = {},
    onNavigateToChatDirecto: (String) -> Unit = {},
    onNavigateToComunidad: () -> Unit = {},
    userViewModel: UserViewModel = viewModel(),
    jamViewModel: JamViewModel = viewModel()
) {
    var tabSeleccionada by remember { mutableStateOf("inicio") }

    LaunchedEffect(Unit) {
        userViewModel.cargarUsuario()
        jamViewModel.cargarFeed()
        jamViewModel.cargarMisJams()
        jamViewModel.cargarMisSolicitudes()
        jamViewModel.cargarJamsActivos()
    }

    Scaffold(
        floatingActionButton = {
            if (tabSeleccionada == "inicio") {
                FloatingActionButton(
                    onClick = onCreateJamClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Crear Jam")
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, "Inicio") },
                    label = { Text("Inicio") },
                    selected = tabSeleccionada == "inicio",
                    onClick = { tabSeleccionada = "inicio" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Search, "Buscar") },
                    label = { Text("Buscar") },
                    selected = tabSeleccionada == "buscar",
                    onClick = { tabSeleccionada = "buscar" }
                )
                NavigationBarItem(
                    icon = {
                        JamIconSmall(size = 28.dp)
                    },
                    label = { Text("Jams") },
                    selected = tabSeleccionada == "jams",
                    onClick = { tabSeleccionada = "jams" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, "Perfil") },
                    label = { Text("Perfil") },
                    selected = tabSeleccionada == "perfil",
                    onClick = { tabSeleccionada = "perfil" }
                )
                NavigationBarItem(
                    icon = {
                        Icon(Icons.Filled.People, "Comunidad", tint = Color(0xFFFFD700))
                    },
                    label = { Text("Comunidad") },
                    selected = false,
                    onClick = onNavigateToComunidad
                )
            }
        }
        ) { paddingValues ->
        val ctx = LocalContext.current
        val usuario by userViewModel.usuario.collectAsState()
        val esPremium = userViewModel.tienePremium()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (tabSeleccionada) {
                "inicio" -> FeedInicio(
                    jamViewModel = jamViewModel,
                    userViewModel = userViewModel,
                    userTags = usuario?.etiquetas ?: emptyList(),
                    userPais = usuario?.pais ?: "",
                    userLat = usuario?.latitud,
                    userLng = usuario?.longitud,
                    onJoinClick = {
                        if (esPremium || userViewModel.verificarLimiteSemanal()) {
                            jamViewModel.solicitarUnirse(it,
                                onSuccess = {
                                    userViewModel.incrementarContadorSemanal()
                                    Toast.makeText(ctx,
                                        "Solicitud enviada. Revisa la pestaña Jams.",
                                        Toast.LENGTH_SHORT).show()
                                },
                                onError = { error ->
                                    Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Toast.makeText(ctx,
                                "Límite semanal alcanzado (10/10). Apoya el proyecto para más.",
                                Toast.LENGTH_LONG).show()
                        }
                    },
                    onJamClick = onNavigateToDetalleJam,
                    onNavigateToComunidad = onNavigateToComunidad,
                    showAds = !esPremium
                )
                "buscar" -> PantallaBusqueda(
                    userViewModel = userViewModel,
                    jamViewModel = jamViewModel,
                    onVerPerfil = onNavigateToPerfilPublico,
                    onJamClick = onNavigateToDetalleJam
                )
                "jams" -> JamsActivosContent(
                    jamViewModel = jamViewModel,
                    onGestionar = onNavigateToGestionar,
                    onInvitados = onNavigateToInvitados,
                    onChat = onNavigateToChat,
                    onAmigos = { onNavigateToChatDirecto("") }
                )
                "perfil" -> PerfilScreen(
                    userViewModel = userViewModel,
                    jamViewModel = jamViewModel,
                    onCerrarSesion = onCerrarSesion,
                    onCuentaEliminada = onCuentaEliminada
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FeedInicio(
    jamViewModel: JamViewModel,
    userViewModel: UserViewModel,
    userTags: List<String> = emptyList(),
    userPais: String = "",
    userLat: Double? = null,
    userLng: Double? = null,
    onJoinClick: (JamData) -> Unit = {},
    onJamClick: (JamData) -> Unit = {},
    onNavigateToComunidad: () -> Unit = {},
    showAds: Boolean = false
) {
    val jams by jamViewModel.jams.collectAsState()
    val isLoading by jamViewModel.isLoading.collectAsState()
    val isRefreshing by jamViewModel.isRefreshing.collectAsState()

    // Feed algorithm: same country + 10km max + tag match sorting
    val jamsFiltrados by remember(jams, userPais, userTags, userLat, userLng) {
        derivedStateOf {
            val paisOk = if (userPais.isNotBlank()) {
                jams.filter { it.pais.equals(userPais, ignoreCase = true) }
            } else jams

            val conDistancia = if (userLat != null && userLng != null) {
                paisOk.filter { jam ->
                    if (jam.latitud != null && jam.longitud != null) {
                        JamViewModel.calcularDistanciaKm(userLat, userLng, jam.latitud, jam.longitud) <= 10.0
                    } else true
                }
            } else paisOk

            if (userTags.isNotEmpty()) {
                conDistancia.sortedByDescending { jam ->
                    jam.etiquetas.count { tag ->
                        userTags.any { it.equals(tag, ignoreCase = true) }
                    }
                }
            } else conDistancia
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { jamViewModel.refrescarFeed() }
    )

    if (isLoading && jams.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        if (jamsFiltrados.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No hay Jams por aquí aún...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "¡Sé el primero en crear una!",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(jamsFiltrados.size + (if (showAds) jamsFiltrados.size / 3 + 1 else 0)) { index ->
                    val adInterval = 3
                    val jamIndex = if (showAds) {
                        val adsBefore = index / (adInterval + 1)
                        index - adsBefore
                    } else index
                    val isAd = showAds && (index % (adInterval + 1) == adInterval)

                    if (isAd && showAds) {
                        AdJamCard(onNavigateToComunidad = onNavigateToComunidad)
                    } else if (jamIndex < jamsFiltrados.size) {
                        JamPostCard(
                            jam = jamsFiltrados[jamIndex],
                            jamViewModel = jamViewModel,
                            onJoinClick = { onJoinClick(jamsFiltrados[jamIndex]) },
                            onJamClick = { onJamClick(jamsFiltrados[jamIndex]) }
                        )
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
fun PantallaBusqueda(
    userViewModel: UserViewModel,
    jamViewModel: JamViewModel,
    onVerPerfil: (String) -> Unit = {},
    onJamClick: (JamData) -> Unit = {}
) {
    val ctx = LocalContext.current
    val resultadosPersonas by userViewModel.resultadosBusqueda.collectAsState()
    val resultadosJams by jamViewModel.jamsBusqueda.collectAsState()
    val tagsGlobales by jamViewModel.tagsGlobales.collectAsState()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var busqueda by remember { mutableStateOf("") }
    var tabBusqueda by remember { mutableStateOf(0) }
    var tagSeleccionado by remember { mutableStateOf("") }
    var mostrarSelectorTag by remember { mutableStateOf(false) }
    var tagBusquedaDialog by remember { mutableStateOf("") }

    // Diálogo de selección de tags para filtro
    if (mostrarSelectorTag) {
        val tagsFiltrados by remember(tagBusquedaDialog, tagsGlobales) {
            derivedStateOf {
                if (tagBusquedaDialog.isBlank()) tagsGlobales
                else tagsGlobales.filter { it.nombre.contains(tagBusquedaDialog, ignoreCase = true) }
            }
        }
        AlertDialog(
            onDismissRequest = { mostrarSelectorTag = false; tagBusquedaDialog = "" },
            title = { Text("Filtrar por tag", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp)) {
                    OutlinedTextField(
                        value = tagBusquedaDialog,
                        onValueChange = { tagBusquedaDialog = it },
                        placeholder = { Text("Buscar tag...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        tagsFiltrados.forEach { tag ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {
                                        tagSeleccionado = tag.nombre
                                        mostrarSelectorTag = false
                                        tagBusquedaDialog = ""
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tag.nombre, fontSize = 14.sp)
                                Text("${tag.usos} usos", fontSize = 11.sp, color = Color.Gray)
                            }
                            if (tag != tagsFiltrados.last()) HorizontalDivider()
                        }
                        if (tagsFiltrados.isEmpty() && tagBusquedaDialog.isNotBlank()) {
                            Text("Sin resultados", color = Color.Gray,
                                modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarSelectorTag = false; tagBusquedaDialog = "" }) {
                    Text("Cerrar")
                }
            }
        )
    }

    LaunchedEffect(Unit) { jamViewModel.cargarTags() }

    LaunchedEffect(busqueda, tagSeleccionado) {
        if (busqueda.isNotBlank() || tagSeleccionado.isNotBlank()) {
            userViewModel.buscarUsuarios(busqueda, tagSeleccionado)
            jamViewModel.buscarJams(busqueda, tagSeleccionado)
        } else {
            userViewModel.detenerBusqueda()
            jamViewModel.detenerBusquedaJams()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            userViewModel.detenerBusqueda()
            jamViewModel.detenerBusquedaJams()
        }
    }

    var isRefreshingBusqueda by remember { mutableStateOf(false) }
    val pullRefreshBusqState = rememberPullRefreshState(
        refreshing = isRefreshingBusqueda,
        onRefresh = {
            isRefreshingBusqueda = true
            if (busqueda.isNotBlank() || tagSeleccionado.isNotBlank()) {
                userViewModel.refrescarBusqueda(busqueda, tagSeleccionado)
                jamViewModel.refrescarBusquedaJams(busqueda, tagSeleccionado)
            }
            isRefreshingBusqueda = false
        }
    )

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshBusqState)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search bar
        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            placeholder = { Text("Buscar personas o jams...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            leadingIcon = { Icon(Icons.Filled.Search, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = {
                if (tagSeleccionado.isNotBlank()) {
                    TextButton(onClick = { tagSeleccionado = "" },
                        contentPadding = PaddingValues(0.dp)) {
                        Text("✕ ${tagSeleccionado}", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Filter button
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (tagSeleccionado.isNotBlank()) {
                FilterChip(
                    selected = true,
                    onClick = { tagSeleccionado = "" },
                    label = { Text(tagSeleccionado, fontSize = 11.sp) },
                    trailingIcon = {
                        Text("✕", fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            OutlinedButton(
                onClick = { mostrarSelectorTag = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.Search, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Filtro de tags", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tabs: Personas | Jams
        TabRow(selectedTabIndex = tabBusqueda) {
            Tab(selected = tabBusqueda == 0, onClick = { tabBusqueda = 0 },
                text = { Text("Personas (${resultadosPersonas.size})") })
            Tab(selected = tabBusqueda == 1, onClick = { tabBusqueda = 1 },
                text = { Text("Jams (${resultadosJams.size})") })
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (busqueda.isBlank() && tagSeleccionado.isBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Search, null, modifier = Modifier.size(48.dp),
                        tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Busca por nombre, etiqueta o descripción",
                        color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else if (tabBusqueda == 0) {
            // Personas
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(resultadosPersonas, key = { it.uid }) { usuario ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onVerPerfil(usuario.uid) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AvatarIniciales(
                                        nombre = usuario.username,
                                        fotoBase64 = usuario.fotoPerfilUrl,
                                        modifier = Modifier.size(44.dp),
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(usuario.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (usuario.esVerificado) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("\uD83C\uDF4B", fontSize = 14.sp)
                                    }
                                    if (usuario.apoyoBeta) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("\u271A", fontSize = 14.sp, color = Color(0xFFE53935))
                                    }
                                }
                                if (usuario.bio.isNotBlank())
                                    Text(usuario.bio, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                                if (usuario.etiquetas.isNotEmpty())
                                    Text(usuario.etiquetas.joinToString(" · "), fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary)
                            }
                            if (userViewModel.loSigo(usuario.uid)) {
                                Text("Siguiendo", fontSize = 10.sp, color = Color(0xFF4CAF50))
                            }
                        }
                    }
                }
                if (resultadosPersonas.isEmpty() && busqueda.isNotBlank()) {
                    item {
                        Text("No se encontraron personas", color = Color.Gray,
                            modifier = Modifier.padding(16.dp))
                    }
                }
            }
        } else {
            // Jams
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(resultadosJams, key = { it.id }) { jam ->
                    val esMiembro = jam.asistentes.contains(currentUid) || jam.creadoPor == currentUid
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onJamClick(jam) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (jam.imagenBase64.isNotBlank()) {
                                val jamBanner = remember(jam.imagenBase64) {
                                    try {
                                        val bytes = Base64.decode(jam.imagenBase64, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    } catch (e: Exception) { null }
                                }
                                if (jamBanner != null) {
                                    Image(bitmap = jamBanner.asImageBitmap(), contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().height(100.dp),
                                        contentScale = ContentScale.Crop)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                            Text(jam.titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (jam.descripcion.isNotBlank())
                                Text(jam.descripcion, fontSize = 11.sp, color = Color.Gray, maxLines = 2)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("👤 ", fontSize = 10.sp, color = Color.Gray)
                                Text(jam.creadorUsername, fontSize = 10.sp, color = Color.Gray)
                                if (jam.creadorUsername.equals("oscar2puerta", ignoreCase = true)) {
                                    Icon(Icons.Filled.CheckCircle, null,
                                        Modifier.size(10.dp), tint = Color(0xFF1DA1F2))
                                }
                                Text(" · ${jam.asistentes.size}/${jam.maxParticipantes}",
                                    fontSize = 10.sp, color = Color.Gray)
                                if (jam.direccion.isNotBlank() && esMiembro) {
                                    Text(" 📍${jam.direccion}", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            if (jam.etiquetas.isNotEmpty()) {
                                Text(jam.etiquetas.joinToString(" · "), fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            when {
                                jam.creadoPor == currentUid -> {
                                    Text("Eres el creador", fontSize = 11.sp, color = Color(0xFF4CAF50))
                                }
                                jam.asistentes.contains(currentUid) -> {
                                    Text("Ya eres parte de esta Jam", fontSize = 11.sp, color = Color(0xFF4CAF50))
                                }
                                else -> {
                                    Text("Toca para ver detalles y unirte", fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                if (resultadosJams.isEmpty() && busqueda.isNotBlank()) {
                    item {
                        Text("No se encontraron jams", color = Color.Gray,
                            modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
        // Cierra la Column
        }
        PullRefreshIndicator(
            refreshing = isRefreshingBusqueda,
            state = pullRefreshBusqState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

// ====== AVATAR POR DEFECTO ======
@Composable
fun AvatarIniciales(
    nombre: String,
    modifier: Modifier = Modifier.size(40.dp),
    fotoBase64: String = "",
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp
) {
    val inicial = remember(nombre) {
        nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "J"
    }
    val esUrl = fotoBase64.startsWith("http")
    val fotoBitmap = remember(fotoBase64) {
        if (esUrl) null else try {
            if (fotoBase64.isNotBlank()) {
                val bytes = Base64.decode(fotoBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else null
        } catch (e: Exception) { null }
    }
    val imageBitmap = remember(fotoBitmap) { fotoBitmap?.asImageBitmap() }
    Box(modifier = modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center) {
        if (imageBitmap != null) {
            Image(bitmap = imageBitmap, contentDescription = null,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else if (esUrl) {
            AsyncImage(model = fotoBase64, contentDescription = null,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Text(inicial, color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold, fontSize = fontSize)
        }
    }
}

// ====== DETALLE DE JAM ======
@Composable
fun DetalleJamScreen(
    jam: JamData,
    jamViewModel: JamViewModel,
    onVolver: () -> Unit,
    onVerPerfil: (String) -> Unit = {},
    onUnirse: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val misSolicitudes by jamViewModel.misSolicitudes.collectAsState()
    val jamsActivos by jamViewModel.jamsActivos.collectAsState()

    if (jam == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Jam no encontrada", color = Color.Gray)
        }
        return
    }

    val esCreador = jam.creadoPor == currentUid
    val esParticipante = jam.asistentes.contains(currentUid) || esCreador
    val yaSolicite = misSolicitudes.contains(jam.id)

    var mostrarParticipantes by remember { mutableStateOf(false) }
    if (mostrarParticipantes) {
        ListaUsuariosDialog(
            titulo = "Participantes",
            uids = jam.asistentes,
            onDismiss = { mostrarParticipantes = false },
            onVerPerfil = { uid -> mostrarParticipantes = false; onVerPerfil(uid) }
        )
    }
    val otraJam = jamsActivos.find { j ->
        (j.asistentes.contains(currentUid) || j.creadoPor == currentUid) && j.id != jam.id
    }
    var mostrarCambioJam by remember { mutableStateOf(false) }

    if (mostrarCambioJam && otraJam != null) {
        AlertDialog(
            onDismissRequest = { mostrarCambioJam = false },
            title = { Text("Cambiar de Jam") },
            text = { Text("Ya estás en otra Jam activa \"${otraJam.titulo}\". ¿Seguro que quieres salir de esa y unirte a esta?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarCambioJam = false
                    jamViewModel.salirDeJam(otraJam.id,
                        onSuccess = {
                            jamViewModel.solicitarUnirse(jam,
                                onSuccess = {
                                    Toast.makeText(ctx, "Solicitud enviada", Toast.LENGTH_SHORT).show()
                                    onVolver()
                                },
                                onError = { error ->
                                    Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onError = { error ->
                            Toast.makeText(ctx, "Error al salir: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                }) { Text("Sí, cambiar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCambioJam = false }) { Text("Cancelar") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Banner
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(MaterialTheme.colorScheme.surfaceVariant)) {
            if (jam.imagenBase64.isNotBlank()) {
                val jamBanner = remember(jam.imagenBase64) {
                    try {
                        val bytes = Base64.decode(jam.imagenBase64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) { null }
                }
                if (jamBanner != null) {
                    Image(bitmap = jamBanner.asImageBitmap(), contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
            IconButton(onClick = onVolver,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(
                    Color.Black.copy(alpha = 0.4f), CircleShape)) {
                Text("←", fontSize = 20.sp, color = Color.White)
            }
            if (jam.esPrivada) {
                Surface(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF9800).copy(alpha = 0.8f)) {
                    Text(" Privada ", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarIniciales(
                    nombre = jam.creadorUsername,
                    fotoBase64 = jam.creadorFotoUrl,
                    modifier = Modifier.size(36.dp),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(jam.creadorUsername, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        if (jam.creadorUsername.equals("oscar2puerta", ignoreCase = true)) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.CheckCircle, null,
                                Modifier.size(14.dp), tint = Color(0xFF1DA1F2))
                        }
                    }
                    Text("Organizador", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(jam.titulo, fontWeight = FontWeight.Bold, fontSize = 22.sp)

            if (jam.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(jam.descripcion, fontSize = 14.sp, color = Color.Gray)
            }

            if (jam.etiquetas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    jam.etiquetas.forEach { tag ->
                        Surface(shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                            Text(tag, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (esParticipante && jam.direccion.isNotBlank()) {
                        InfoRow("📍", jam.direccion)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth().clickable { mostrarParticipantes = true },
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("👥", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${jam.asistentes.size}/${jam.maxParticipantes} participantes",
                            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (jam.esPrivada) {
                        Spacer(modifier = Modifier.height(6.dp))
                        InfoRow("🔒", "Jam privada (solo por invitación)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            when {
                esCreador -> {
                    Text("Eres el organizador de esta Jam",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                esParticipante -> {
                    Text("Ya eres parte de esta Jam",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                yaSolicite -> {
                    OutlinedButton(onClick = {
                        jamViewModel.cancelarSolicitud(jam.id)
                    }, modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50)) {
                        Text("Cancelar solicitud", color = Color(0xFFFF9800))
                    }
                }
                else -> {
                    Button(onClick = {
                        if (otraJam != null) mostrarCambioJam = true
                        else onUnirse()
                    }, modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50)) {
                        Text("Unirse a la Jam", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun InfoRow(icono: String, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icono, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(texto, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ====== LISTA DE USUARIOS (seguidores/siguiendo) ======
@Composable
fun ListaUsuariosDialog(
    titulo: String,
    uids: List<String>,
    onDismiss: () -> Unit,
    onVerPerfil: (String) -> Unit = {}
) {
    val usuarios = remember(uids) {
        mutableStateOf<List<UsuarioData>>(emptyList())
    }
    LaunchedEffect(uids) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val cargados = uids.mapNotNull { uid ->
            try {
                val doc = db.collection("usuarios").document(uid).get().await()
                if (doc.exists()) UsuarioData(
                    uid = uid,
                    username = doc.getString("username") ?: "",
                    fotoPerfilUrl = doc.getString("fotoPerfilUrl") ?: "",
                    bio = doc.getString("bio") ?: "",
                    email = doc.getString("email") ?: "",
                    etiquetas = (doc.get("etiquetas") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    esVerificado = (doc.getString("email") ?: "") == CREADOR_EMAIL
                ) else null
            } catch (_: Exception) { null }
        }
        usuarios.value = cargados
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$titulo (${uids.size})", fontWeight = FontWeight.Bold) },
        text = {
            if (uids.isEmpty()) {
                Text("Sin usuarios", color = Color.Gray)
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    usuarios.value.forEach { user ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { onVerPerfil(user.uid) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarIniciales(
                                nombre = user.username,
                                fotoBase64 = user.fotoPerfilUrl,
                                modifier = Modifier.size(40.dp),
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.username, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    if (user.esVerificado) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("\uD83C\uDF4B", fontSize = 14.sp)
                                    }
                                    if (user.apoyoBeta) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("\u271A", fontSize = 14.sp, color = Color(0xFFE53935))
                                    }
                                }
                                if (user.bio.isNotBlank())
                                    Text(user.bio, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
fun PerfilPublicoScreen(
    uid: String,
    userViewModel: UserViewModel,
    conversacionViewModel: ConversacionViewModel? = null,
    onVolver: () -> Unit,
    onVerPerfil: (String) -> Unit = {},
    onIniciarChat: ((String) -> Unit)? = null
) {
    val ctx = LocalContext.current
    val perfil by userViewModel.perfilPublico.collectAsState()
    val miUsuario by userViewModel.usuario.collectAsState()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(uid) { userViewModel.cargarUsuarioPublico(uid) }
    DisposableEffect(Unit) { onDispose { userViewModel.limpiarPerfilPublico() } }

    val loSigo = miUsuario?.siguiendo?.contains(uid) ?: false
    val loBloquee = miUsuario?.bloqueados?.contains(uid) ?: false
    val esMiPerfil = uid == currentUid

    if (perfil == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val usuario = perfil!!

    // Decodificar imágenes fuera del árbol composable
    val bannerBitmap = remember(usuario.bannerUrl) {
        try {
            if (usuario.bannerUrl.isNotBlank()) {
                val bytes = android.util.Base64.decode(usuario.bannerUrl, android.util.Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else null
        } catch (e: Exception) { null }
    }
    val fotoBitmap = remember(usuario.fotoPerfilUrl) {
        try {
            if (usuario.fotoPerfilUrl.isNotBlank()) {
                val bytes = android.util.Base64.decode(usuario.fotoPerfilUrl, android.util.Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else null
        } catch (e: Exception) { null }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Banner
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(MaterialTheme.colorScheme.surfaceVariant)) {
            if (bannerBitmap != null) {
                Image(bitmap = bannerBitmap.asImageBitmap(), contentDescription = null,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            IconButton(onClick = onVolver,
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp)) {
                Text("←", fontSize = 20.sp, color = Color.White)
            }
        }

        // Avatar
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.3f))
                .align(Alignment.CenterHorizontally)
                .offset(y = (-44).dp),
            contentAlignment = Alignment.Center
        ) {
            if (fotoBitmap != null) {
                Image(bitmap = fotoBitmap.asImageBitmap(), contentDescription = null,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Filled.Person, null, modifier = Modifier.size(40.dp), tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Info
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(usuario.username, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                if (usuario.esVerificado) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("\uD83C\uDF4B", fontSize = 18.sp)
                }
                if (usuario.apoyoBeta) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("\u271A", fontSize = 18.sp, color = Color(0xFFE53935))
                }
            }
            if (usuario.bio.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(usuario.bio, fontSize = 14.sp, color = Color.Gray)
            }
            if (usuario.etiquetas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    usuario.etiquetas.forEach { tag ->
                        Surface(shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                            Text(tag, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Stats
            var mostrarSeguidores by remember { mutableStateOf(false) }
            var mostrarSiguiendo by remember { mutableStateOf(false) }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { mostrarSeguidores = true }) {
                    Text("${usuario.seguidores.size}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Seguidores", fontSize = 11.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { mostrarSiguiendo = true }) {
                    Text("${usuario.siguiendo.size}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Siguiendo", fontSize = 11.sp, color = Color.Gray)
                }
            }

            if (mostrarSeguidores) {
                ListaUsuariosDialog(
                    titulo = "Seguidores",
                    uids = usuario.seguidores,
                    onDismiss = { mostrarSeguidores = false },
                    onVerPerfil = { puid ->
                        mostrarSeguidores = false
                        onVerPerfil(puid)
                    }
                )
            }
            if (mostrarSiguiendo) {
                ListaUsuariosDialog(
                    titulo = "Siguiendo",
                    uids = usuario.siguiendo,
                    onDismiss = { mostrarSiguiendo = false },
                    onVerPerfil = { puid ->
                        mostrarSiguiendo = false
                        onVerPerfil(puid)
                    }
                )
            }

            // Follow / Chat / Block section
            Spacer(modifier = Modifier.height(16.dp))
            if (!esMiPerfil) {
                var mostrarMenuAcciones by remember { mutableStateOf(false) }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (loSigo) {
                                    userViewModel.dejarDeSeguir(uid,
                                        onResult = { if (!it) Toast.makeText(ctx, "Error al dejar de seguir", Toast.LENGTH_SHORT).show() })
                                } else {
                                    userViewModel.seguirUsuario(uid,
                                        onResult = { if (!it) Toast.makeText(ctx, "Error al seguir", Toast.LENGTH_SHORT).show() })
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (loSigo) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(if (loSigo) "Siguiendo" else "Seguir",
                                color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        if (loSigo && onIniciarChat != null) {
                            OutlinedButton(
                                onClick = { onIniciarChat(uid) },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Chat", fontWeight = FontWeight.Bold)
                            }
                        }
                        IconButton(onClick = { mostrarMenuAcciones = true }) {
                            Icon(Icons.Filled.MoreVert, null, tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    if (loBloquee) {
                        Text("Bloqueado", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }

                DropdownMenu(
                    expanded = mostrarMenuAcciones,
                    onDismissRequest = { mostrarMenuAcciones = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Bloquear usuario") },
                        onClick = {
                            mostrarMenuAcciones = false
                            userViewModel.bloquearUsuario(uid)
                        },
                        leadingIcon = { Icon(Icons.Filled.Block, null) }
                    )
                    if (loBloquee) {
                        DropdownMenuItem(
                            text = { Text("Desbloquear usuario") },
                            onClick = {
                                mostrarMenuAcciones = false
                                userViewModel.desbloquearUsuario(uid)
                            },
                            leadingIcon = { Icon(Icons.Filled.LockOpen, null) }
                        )
                    }
                    if (miUsuario?.seguidores?.contains(uid) == true) {
                        DropdownMenuItem(
                            text = { Text("Eliminar seguidor") },
                            onClick = {
                                mostrarMenuAcciones = false
                                userViewModel.eliminarSeguidor(uid)
                            },
                            leadingIcon = { Icon(Icons.Filled.PersonRemove, null) }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}



@OptIn(ExperimentalMaterialApi::class)
@Composable

fun JamsActivosContent(
    jamViewModel: JamViewModel,
    onGestionar: (JamData) -> Unit = {},
    onInvitados: (JamData) -> Unit = {},
    onChat: (JamData) -> Unit = {},
    onAmigos: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val jamsActivos by jamViewModel.jamsActivos.collectAsState()
    val misSolicitudes by jamViewModel.misSolicitudes.collectAsState()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    var jamSalir by remember { mutableStateOf<JamData?>(null) }
    LaunchedEffect(Unit) { jamViewModel.cargarJamsActivos() }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = false,
        onRefresh = { jamViewModel.refrescarJamsActivos() }
    )
    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Jams en ejecucion",
                fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
            if (onAmigos != null) {
                OutlinedButton(
                    onClick = onAmigos,
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Amigos", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (jamsActivos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No estas en ninguna Jam activa", color = Color.Gray)
                        Text("Unete a una desde Inicio!", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn {
                    items(jamsActivos, key = { it.id }) { jam ->
                        val esCreador = jam.creadoPor == currentUid
                        val esAceptado = jam.asistentes.contains(currentUid)
                        val esPendiente = misSolicitudes.contains(jam.id)
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    esCreador -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    esPendiente -> Color(0xFFFF9800).copy(alpha = 0.08f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(jam.titulo, fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(jam.direccion, fontSize = 12.sp, color = Color.Gray)
                                    }
                                    when {
                                        esCreador -> Text("Gestion", color = Color.White,
                                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                            modifier = Modifier.background(Color(0xFF4CAF50), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp))
                                        esPendiente -> Text("Pendiente", color = Color.White,
                                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                            modifier = Modifier.background(Color(0xFFFF9800), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp))
                                        esAceptado -> Text("Activo", color = Color.White,
                                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                            modifier = Modifier.background(Color(0xFF4CAF50), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("/ participantes", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                when {
                                    esCreador -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(onClick = { onInvitados(jam) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) { Text("Invitados", fontSize = 12.sp, maxLines = 1) }
                                                Button(onClick = { onChat(jam) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) { Text("Chat", fontSize = 12.sp, maxLines = 1) }
                                                OutlinedButton(onClick = { onGestionar(jam) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) { Text("Configurar", fontSize = 12.sp, maxLines = 1) }
                                            }
                                        }
                                    }
                                    esPendiente -> {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedButton(onClick = {
                                                jamViewModel.cancelarSolicitud(jam.id,
                                                    onSuccess = { Toast.makeText(context, "Solicitud cancelada", Toast.LENGTH_SHORT).show() },
                                                    onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                                )
                                            }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) { Text("Cancelar solicitud", fontSize = 12.sp, maxLines = 1) }
                                            Text("Espera a ser aceptado...", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                    esAceptado -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(onClick = { onChat(jam) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) { Text("Chat grupal", fontSize = 12.sp, maxLines = 1) }
                                                if (!esCreador) {
                                                    OutlinedButton(onClick = { jamSalir = jam }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF44336)), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) { Text("Salir", fontSize = 12.sp, maxLines = 1) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (jamSalir != null) {
        AlertDialog(
            onDismissRequest = { jamSalir = null },
            title = { Text("Salir de la Jam") },
            text = { Text("Seguro que quieres salir de esta Jam?") },
            confirmButton = { TextButton(onClick = { jamViewModel.salirDeJam(jamSalir!!.id); jamSalir = null }) { Text("Salir", color = Color(0xFFF44336)) } },
            dismissButton = { TextButton(onClick = { jamSalir = null }) { Text("Cancelar") } }
        )
    }
}

// ====== AD CARD ======
@Composable
fun AdJamCard(onNavigateToComunidad: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onNavigateToComunidad() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💪", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apoya",
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Cansado de los limites?",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Apoya el proyecto: Jams ilimitadas, 150 participantes y sin anuncios.",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onNavigateToComunidad,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) { Text("Apoyar", fontSize = 12.sp) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JamPostCard(
    jam: JamData,
    jamViewModel: JamViewModel,
    onJoinClick: () -> Unit,
    onJamClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    val esMio = jam.creadoPor == currentUid
    val estaAceptado = jamViewModel.estaAceptado(jam)
    var isExpanded by remember { mutableStateOf(false) }
    var mostrarMenu by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarDialogoInvitar by remember { mutableStateOf(false) }

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar Jam") },
            text = { Text("¿Seguro que quieres eliminar esta Jam? No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    jamViewModel.eliminarJam(
                        jamId = jam.id,
                        onSuccess = {
                            Toast.makeText(context, "Jam eliminada", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                    mostrarDialogoEliminar = false
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoInvitar) {
        val inviteLink = "jam://invite/${jam.id}"
        AlertDialog(
            onDismissRequest = { mostrarDialogoInvitar = false },
            title = { Text("Invitar a la Jam") },
            text = {
                Column {
                    Text(
                        "Comparte este link con tus amigos:",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = inviteLink,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(
                        Context.CLIPBOARD_SERVICE
                    ) as ClipboardManager
                    val clip = ClipData.newPlainText("Invite Link", inviteLink)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Link copiado", Toast.LENGTH_SHORT).show()
                    mostrarDialogoInvitar = false
                }) {
                    Text("Copiar link")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoInvitar = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize()
            .clickable { onJamClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AvatarIniciales(
                    nombre = jam.creadorUsername,
                    fotoBase64 = jam.creadorFotoUrl,
                    modifier = Modifier.size(40.dp),
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        jam.titulo,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("@${jam.creadorUsername}", fontSize = 12.sp, color = Color.Gray)
                        if (jam.creadorUsername.equals("oscar2puerta", ignoreCase = true)) {
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.Filled.CheckCircle, null,
                                Modifier.size(12.dp), tint = Color(0xFF1DA1F2))
                        }
                    }
                }

                IconButton(onClick = { mostrarDialogoInvitar = true }) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "Compartir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (esMio) {
                    Box {
                        IconButton(onClick = { mostrarMenu = true }) {
                            Text(
                                "•••",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        DropdownMenu(
                            expanded = mostrarMenu,
                            onDismissRequest = { mostrarMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (jam.visible) "Ocultar del feed"
                                        else "Mostrar en feed"
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        if (jam.visible) Icons.Filled.VisibilityOff
                                        else Icons.Filled.Visibility,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    jamViewModel.toggleVisibilidad(jam.id, !jam.visible)
                                    Toast.makeText(
                                        context,
                                        if (jam.visible) "Jam ocultada del feed"
                                        else "Jam visible en el feed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    mostrarMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (jam.esPrivada) "Hacer pública"
                                        else "Hacer privada"
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        if (jam.esPrivada) Icons.Filled.LockOpen
                                        else Icons.Filled.Lock,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    jamViewModel.togglePrivacidad(jam.id, !jam.esPrivada)
                                    Toast.makeText(
                                        context,
                                        if (jam.esPrivada) "Jam ahora pública"
                                        else "Jam ahora privada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    mostrarMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Eliminar",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    mostrarMenu = false
                                    mostrarDialogoEliminar = true
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (jam.imagenBase64.isNotBlank()) {
                val imageBytes = Base64.decode(jam.imagenBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Imagen Jam",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin imagen", color = Color.Gray, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (jam.etiquetas.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    jam.etiquetas.forEach { etiqueta ->
                        AssistChip(
                            onClick = {},
                            label = { Text(etiqueta, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isExpanded) {
                Text(
                    text = jam.descripcion,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (estaAceptado && jam.direccion.isNotBlank()) {
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
                                jam.direccion,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else if (!estaAceptado) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Gray.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔒", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Dirección visible al ser aceptado",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        when {
                            esMio -> {
                                Text("✅ Tu Jam",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold)
                            }
                            estaAceptado -> {
                                Text("✅ Aceptado",
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold)
                            }
                            jam.solicitantes.contains(currentUid) -> {
                                Text("⏳ Solicitud enviada",
                                    color = Color(0xFFFF9800),
                                    fontWeight = FontWeight.Bold)
                            }
                            jam.asistentes.size >= jam.maxParticipantes -> {
                                Text("❌ Completa",
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold)
                            }
                            else -> {
                                Button(onClick = onJoinClick,
                                    shape = RoundedCornerShape(16.dp)) {
                                    Text("let's Jam!",
                                        color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                        Text("${jam.asistentes.size}/${jam.maxParticipantes} participantes",
                            fontSize = 11.sp, color = Color.Gray)
                    }
                    TextButton(onClick = { isExpanded = false }) {
                        Text("Leer menos...", color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        jam.descripcion.take(60) +
                                if (jam.descripcion.length > 60) "..." else "",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { isExpanded = true }) {
                        Text("Leer más...", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}