package com.noxtope.jam.ui.theme

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmigosScreen(
    conversacionViewModel: ConversacionViewModel = viewModel(),
    onVolver: () -> Unit = {},
    onAbrirChat: (String) -> Unit = {}
) {
    val conversaciones by conversacionViewModel.conversaciones.collectAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(Unit) { conversacionViewModel.escucharConversaciones() }
    DisposableEffect(Unit) { onDispose { conversacionViewModel.detenerEscuchaConversaciones() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Amigos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (conversaciones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sin conversaciones aún", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Sigue a alguien para enviarle un mensaje", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(conversaciones, key = { it.id }) { conv ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).clickable {
                            val otroUid = conv.participantes.find { it != uid } ?: return@clickable
                            onAbrirChat(otroUid)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AvatarIniciales(
                                nombre = conv.otroUsername,
                                fotoBase64 = conv.otroFotoUrl,
                                modifier = Modifier.size(48.dp),
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(conv.otroUsername, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (conv.ultimoMensaje.isNotBlank()) {
                                    Text(conv.ultimoMensaje, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val chatDb by lazy { FirebaseFirestore.getInstance() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDirectoScreen(
    otroUid: String,
    conversacionViewModel: ConversacionViewModel = viewModel(),
    onVolver: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val mensajes by conversacionViewModel.mensajes.collectAsState()
    var texto by remember { mutableStateOf("") }
    var conversacionId by remember { mutableStateOf("") }
    var otroUsername by remember { mutableStateOf("Cargando...") }
    var otroFotoUrl by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(otroUid) {
        if (uid.isBlank()) return@LaunchedEffect
        conversacionViewModel.obtenerOCrearConversacion(otroUid) { convId ->
            conversacionId = convId
            conversacionViewModel.escucharMensajes(convId)
            chatDb.collection("conversaciones").document(convId).get().addOnSuccessListener { doc ->
                otroUsername = doc.getString("otroUsername_$uid") ?: "Usuario"
                otroFotoUrl = doc.getString("otroFotoUrl_$uid") ?: ""
            }
        }
    }
    DisposableEffect(Unit) { onDispose { conversacionViewModel.detenerEscuchaMensajes() } }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && conversacionId.isNotBlank()) {
            conversacionViewModel.enviarMensajeConImagen(conversacionId, uri, otroUid)
        }
    }

    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarIniciales(nombre = otroUsername, fotoBase64 = otroFotoUrl,
                            modifier = Modifier.size(32.dp), fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(otroUsername, fontSize = 16.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (mensajes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay mensajes aún. ¡Empieza a chatear!",
                            color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(mensajes, key = { it.id }) { msg ->
                            val esMio = msg.emisorId == uid
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (esMio) Alignment.End else Alignment.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp, topEnd = 16.dp,
                                        bottomStart = if (esMio) 16.dp else 4.dp,
                                        bottomEnd = if (esMio) 4.dp else 16.dp
                                    ),
                                    color = if (esMio) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    shadowElevation = 1.dp
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        if (msg.texto.isNotBlank()) {
                                            Text(msg.texto, fontSize = 14.sp,
                                                color = if (esMio) Color.White else Color.Unspecified)
                                        }
                                        if (msg.imagenUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = msg.imagenUrl,
                                                contentDescription = "Imagen",
                                                modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                                Text("${msg.username}", fontSize = 9.sp, color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Icon(Icons.Filled.Add, "Adjuntar imagen", tint = MaterialTheme.colorScheme.primary)
                }
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        if (texto.isNotBlank() && conversacionId.isNotBlank()) {
                            conversacionViewModel.enviarMensaje(conversacionId, texto.trim(), otroUid)
                            texto = ""
                        }
                    },
                    enabled = texto.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
