package com.noxtope.jam.ui.theme

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComunidadScreen(
    userViewModel: UserViewModel,
    onVolver: () -> Unit
) {
    val ctx = LocalContext.current
    val activity = ctx as? android.app.Activity
    val usuario by userViewModel.usuario.collectAsState()
    val esColaborador = userViewModel.tienePremium()
    val apoyoBeta = usuario?.apoyoBeta ?: false
    val misPuntos = usuario?.puntosApoyo ?: 0

    val billingManager = remember { BillingManager(ctx.applicationContext) }
    val topDonantes by userViewModel.topDonantes.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.cargarTopDonantes()
    }

    val puntosDisponibles = listOf(5, 10, 15, 25, 50, 100)
    var puntos by remember { mutableIntStateOf(5) }

    val metaDolares = 600
    val donacionActual = remember { mutableIntStateOf(0) }
    val progreso = remember(donacionActual.intValue) {
        if (metaDolares > 0) (donacionActual.intValue.toFloat() / metaDolares).coerceIn(0f, 1f) else 0f
    }

    DisposableEffect(Unit) {
        onDispose { billingManager.destroy() }
    }

    // Gradientes
    val gradienteCard = Brush.linearGradient(
        listOf(Color(0xFF1A237E), Color(0xFF4A148C), Color(0xFF6A1B9A))
    )
    val gradienteBtn = Brush.horizontalGradient(
        listOf(Color(0xFFFF6F00), Color(0xFFE65100))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comunidad") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("Volver", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ====== HEADER ======
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradienteCard, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 28.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Star, null,
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Apoya el proyecto \uD83D\uDCAA",
                        fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        color = Color.White)
                    Text("Ay\u00fadanos a salir de la beta y construir algo \u00e9pico juntos",
                        fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center, lineHeight = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ====== MIS PUNTOS ======
            if (esColaborador) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD700).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("\u2B50", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mis puntos de apoyo",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground)
                            Text("Has aportado $${misPuntos} USD al proyecto",
                                fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ====== PUNTOS DE APOYO ======
            Card(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(gradienteCard, RoundedCornerShape(24.dp))
                ) {
                    // Sparkle decorations
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val dots = listOf(
                            Offset(size.width * 0.15f, size.height * 0.1f),
                            Offset(size.width * 0.85f, size.height * 0.15f),
                            Offset(size.width * 0.9f, size.height * 0.85f),
                            Offset(size.width * 0.1f, size.height * 0.8f),
                        )
                        dots.forEach { pos ->
                            drawCircle(Color.White.copy(alpha = 0.15f), radius = 3f, center = pos)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Puntos de apoyo",
                            fontSize = 20.sp, fontWeight = FontWeight.Bold,
                            color = Color.White)
                        Text("1 punto = \$1 USD  |  M\u00ednimo 5",
                            fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))

                        Spacer(modifier = Modifier.height(20.dp))

                        // Selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            FilledIconButton(
                                onClick = {
                                    val idx = puntosDisponibles.indexOf(puntos)
                                    if (idx > 0) puntos = puntosDisponibles[idx - 1]
                                },
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Filled.Remove, "Menos",
                                    tint = Color.White, modifier = Modifier.size(22.dp))
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$puntos",
                                    fontSize = 44.sp, fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.animateContentSize())
                                Text("puntos",
                                    fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            FilledIconButton(
                                onClick = {
                                    val idx = puntosDisponibles.indexOf(puntos)
                                    if (idx < puntosDisponibles.lastIndex) puntos = puntosDisponibles[idx + 1]
                                },
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Filled.Add, "M\u00e1s",
                                    tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Precio
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                billingManager.getPriceForPoints(puntos),
                                fontSize = 26.sp, fontWeight = FontWeight.Bold,
                                color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            puntosDisponibles.forEach { cant ->
                                FilterChip(
                                    selected = puntos == cant,
                                    onClick = { puntos = cant },
                                    label = { Text("$cant", fontSize = 11.sp,
                                        fontWeight = if (puntos == cant) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color.White.copy(alpha = 0.25f),
                                        containerColor = Color.White.copy(alpha = 0.06f),
                                        selectedLabelColor = Color.White,
                                        labelColor = Color.White.copy(alpha = 0.8f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Boton
                        Button(
                            onClick = {
                                activity?.let { act ->
                                    billingManager.launchPurchase(
                                        activity = act,
                                        puntos = puntos,
                                        onSuccess = {
                                            val esPrimera = !apoyoBeta
                                            userViewModel.registrarDonacion(puntos) { ok ->
                                                if (ok) {
                                                    donacionActual.intValue += puntos
                                                    userViewModel.cargarTopDonantes()
                                                    if (esPrimera) {
                                                        Toast.makeText(ctx,
                                                            "\u271A Bienvenido Beta Supporter! Gracias por tu apoyo de \$${puntos} USD",
                                                            Toast.LENGTH_LONG).show()
                                                    } else {
                                                        Toast.makeText(ctx,
                                                            "\u271A +${puntos} puntos a\u00f1adidos! Total: \$${misPuntos + puntos} USD donados. Gracias!",
                                                            Toast.LENGTH_LONG).show()
                                                    }
                                                } else {
                                                    Toast.makeText(ctx,
                                                        "Pago completado. El premium se activar\u00e1 en breve.",
                                                        Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        onError = { error ->
                                            Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White)
                        ) {
                            Text(
                                "Apoyar con ${billingManager.getPriceForPoints(puntos)}",
                                fontWeight = FontWeight.Bold, fontSize = 17.sp,
                                color = Color(0xFF1A237E))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (apoyoBeta) "Otra donaci\u00f3n = m\u00e1s puntos acumulados \u2B50"
                            else "Obt\u00e9n Premium vitalicio + insignia \u271A Beta Supporter",
                            fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ====== META ======
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("\uD83C\uDF1F", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Meta de la comunidad",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Recaudando para mantener servidores y salir de la beta.",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$${donacionActual.intValue}",
                            fontWeight = FontWeight.Bold, fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("de $$metaDolares USD",
                            fontSize = 13.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("${(progreso * 100).toInt()}%",
                        fontSize = 11.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ====== TOP DONANTES ======
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("\uD83C\uDFC6", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ranking de apoyo",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (topDonantes.isEmpty()) {
                        Text("S\u00e9 el primero en aparecer aqu\u00ed \uD83D\uDCAA",
                            fontSize = 13.sp, color = Color.Gray,
                            modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center)
                    } else {
                        topDonantes.take(20).forEachIndexed { idx, donante ->
                            val bgColor = when (idx) {
                                0 -> Color(0xFFFFD700).copy(alpha = 0.12f) // gold
                                1 -> Color(0xFFC0C0C0).copy(alpha = 0.12f) // silver
                                2 -> Color(0xFFCD7F32).copy(alpha = 0.12f) // bronze
                                else -> Color.Transparent
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bgColor, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Posicion
                                Box(
                                    modifier = Modifier.size(28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        when (idx) {
                                            0 -> "\uD83E\uDD47"
                                            1 -> "\uD83E\uDD48"
                                            2 -> "\uD83E\uDD49"
                                            else -> "${idx + 1}"
                                        },
                                        fontSize = if (idx < 3) 16.sp else 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (idx < 3) Color.Unspecified
                                        else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                AvatarIniciales(
                                    nombre = donante.username,
                                    fotoBase64 = donante.fotoUrl,
                                    modifier = Modifier.size(32.dp),
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(donante.username,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onBackground)
                                        if (donante.uid == usuario?.uid) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("(t\u00fa)", fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                                Text("\$${donante.puntos}",
                                    fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                    color = Color(0xFFFFD700))
                            }
                            if (idx < topDonantes.size - 1 && idx < 19) {
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ====== BENEFICIOS ======
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("\uD83C\uDF1F", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Beneficios",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf(
                        "Jams ilimitadas (sin tope semanal)",
                        "Hasta 150 participantes por Jam",
                        "Sin publicidad",
                        "Acceso anticipado a funciones",
                        "Insignia \u271A Beta Supporter en tu perfil"
                    ).forEach { feat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(feat, fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Cada punto = \$1 USD. Al apoyar obtienes Premium vitalicio + insignia \u271A en agradecimiento. Puedes donar tantas veces como quieras \u2014 tus puntos se acumulan y apareces en el ranking.",
                fontSize = 11.sp, color = Color.Gray,
                textAlign = TextAlign.Center, lineHeight = 16.sp)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
