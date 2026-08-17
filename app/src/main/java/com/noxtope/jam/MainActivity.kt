@file:OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)

package com.noxtope.jam

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.android.gms.ads.MobileAds
import com.noxtope.jam.ui.theme.ConversacionViewModel
import com.noxtope.jam.ui.theme.CrearJamScreen
import com.noxtope.jam.ui.theme.DatosPersonalesScreen
import com.noxtope.jam.ui.theme.DetalleJamScreen
import com.noxtope.jam.ui.theme.AmigosScreen
import com.noxtope.jam.ui.theme.ChatDirectoScreen
import com.noxtope.jam.ui.theme.ChatScreen
import com.noxtope.jam.ui.theme.GestionarJamScreen
import com.noxtope.jam.ui.theme.InvitadosScreen
import com.noxtope.jam.ui.theme.JamData
import com.noxtope.jam.ui.theme.JamViewModel
import com.noxtope.jam.ui.theme.LoginScreen
import com.noxtope.jam.ui.theme.MainScreen
import com.noxtope.jam.ui.theme.PerfilPublicoScreen
import com.noxtope.jam.ui.theme.PersonalizacionScreen
import com.noxtope.jam.ui.theme.ComunidadScreen
import com.noxtope.jam.ui.theme.RegistroScreen
import com.noxtope.jam.ui.theme.TermsScreen
import com.noxtope.jam.ui.theme.UserViewModel
import com.noxtope.jam.ui.theme.asegurarContrasteVisible
import com.noxtope.jam.ui.theme.calcularColorSecundario
import com.noxtope.jam.ui.theme.guardarColorSecundario
import com.noxtope.jam.ui.theme.obtenerColorSecundario
import com.noxtope.jam.ui.theme.obtenerRecordarSesion
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        installSplashScreen()
        MobileAds.initialize(this) { }

        setContent {
            val userViewModel: UserViewModel = viewModel()
            val jamViewModel: JamViewModel = viewModel()
            val usuario by userViewModel.usuario.collectAsState()
            val rutaInicial by userViewModel.rutaInicial.collectAsState()

            LaunchedEffect(Unit) {
                val recordarSesion = obtenerRecordarSesion(this@MainActivity)
                userViewModel.decidirRutaInicial(recordarSesion)
            }

            val rainbowList = listOf(
                Color(0xFFFF0000), Color(0xFFFF7F00), Color(0xFFFFFF00),
                Color(0xFF00FF00), Color(0xFF00FFFF), Color(0xFF0000FF),
                Color(0xFF8B00FF)
            )

            var colorIndex by remember { mutableIntStateOf(0) }
            var lucesActivas by remember { mutableStateOf(false) }
            var isDarkMode by remember { mutableStateOf(true) }
            var userSelectedColor by remember { mutableStateOf(Color.White) }
            var userSecundarioColor by remember { mutableStateOf(Color(0xFF666666)) }

            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            fun Long.corregirColor(): Color {
                val intVal = toInt()
                return if (intVal == 0 && this != 0L) Color((this shr 32).toInt()) else Color(intVal)
            }

            LaunchedEffect(usuario) {
                usuario?.let {
                    isDarkMode = it.modoOscuro
                    lucesActivas = it.lucesActivas
                    if (it.colorPrimario != 0L) {
                        userSelectedColor = it.colorPrimario.corregirColor()
                    }
                    if (it.colorSecundario != 0L) {
                        userSecundarioColor = it.colorSecundario.corregirColor()
                        guardarColorSecundario(this@MainActivity, it.colorSecundario)
                    } else {
                        userSecundarioColor = calcularColorSecundario(userSelectedColor, isDarkMode)
                    }
                }
            }

            LaunchedEffect(lucesActivas, colorIndex) {
                if (lucesActivas) {
                    delay(800)
                    colorIndex = (colorIndex + 1) % rainbowList.size
                }
            }

            val animatedRainbowColor by animateColorAsState(
                targetValue = rainbowList[colorIndex],
                animationSpec = tween(durationMillis = 800, easing = LinearEasing),
                label = "RainbowStep"
            )

            val savedLoginColor = remember {
                obtenerColorSecundario(this@MainActivity).corregirColor()
            }
            val loginBgColor = if (lucesActivas) animatedRainbowColor else savedLoginColor

            val colorPrimario = when {
                lucesActivas -> animatedRainbowColor
                else -> asegurarContrasteVisible(
                    if (currentRoute == "login" || currentRoute == "registro") savedLoginColor
                    else userSecundarioColor, isDarkMode
                )
            }

            val colorSecundario = when {
                lucesActivas -> animatedRainbowColor.copy(alpha = 0.7f)
                else -> userSecundarioColor.copy(alpha = 0.8f)
            }

            fun Color.isLight(): Boolean = 0.299f * red + 0.587f * green + 0.114f * blue > 0.5f
            val onPrimaryColor = if (colorPrimario.isLight()) Color.Black else Color.White

            val onSurfaceVariantColor = if (isDarkMode) Color.White.copy(alpha = 0.7f)
            else Color.Black.copy(alpha = 0.55f)

            val colorScheme = if (isDarkMode) {
                darkColorScheme(
                    background = Color.Black,
                    surface = Color.Black,
                    surfaceVariant = Color(0xFF1E1E1E),
                    onBackground = Color.White,
                    onSurface = Color.White,
                    onSurfaceVariant = onSurfaceVariantColor,
                    onPrimary = onPrimaryColor,
                    primary = colorPrimario,
                    secondary = colorSecundario,
                    tertiary = colorSecundario.copy(alpha = 0.5f),
                    outline = Color.White.copy(alpha = 0.3f),
                    outlineVariant = Color.White.copy(alpha = 0.15f)
                )
            } else {
                lightColorScheme(
                    background = Color(0xFFF5F5F5),
                    surface = Color.White,
                    surfaceVariant = Color.White,
                    onBackground = Color.Black,
                    onSurface = Color.Black,
                    onSurfaceVariant = onSurfaceVariantColor,
                    onPrimary = onPrimaryColor,
                    primary = colorPrimario,
                    secondary = colorSecundario,
                    tertiary = colorSecundario.copy(alpha = 0.5f),
                    outline = Color.Black.copy(alpha = 0.22f),
                    outlineVariant = Color.Black.copy(alpha = 0.1f)
                )
            }

            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (rutaInicial == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(com.noxtope.jam.R.drawable.jam_foreground),
                                    contentDescription = "Jam!",
                                    modifier = Modifier.size(80.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    } else {
                        val windowSizeClass = calculateWindowSizeClass(this@MainActivity)
                        val isWideScreen = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = if (isWideScreen) Alignment.TopCenter else Alignment.TopStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (isWideScreen) Modifier.widthIn(max = 600.dp)
                                        else Modifier
                                    )
                            ) {
                            NavHost(
                                navController = navController,
                                startDestination = rutaInicial!!
                            ) {
                                composable("login") {
                                    LoginScreen(
                                        backgroundColor = loginBgColor,
                                        onNavigateToRegister = {
                                            navController.navigate("registro")
                                        },
                                        onLoginSuccess = {
                                            userViewModel.cargarUsuario()
                                            navController.navigate("home") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        },
                                        onNecesitaDatos = {
                                            navController.navigate("datos_personales") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    )
                                }
                                composable("registro") {
                                    RegistroScreen(
                                        onNavigateToCustomization = {
                                            navController.navigate("datos_personales") {
                                                popUpTo("registro") { inclusive = true }
                                            }
                                        }
                                    )
                                }
                                composable("datos_personales") {
                                    DatosPersonalesScreen(
                                        onFinish = {
                                            navController.navigate("personalizacion") {
                                                popUpTo("datos_personales") { inclusive = true }
                                            }
                                        },
                                        userViewModel = userViewModel
                                    )
                                }
                                composable("personalizacion") {
                                    PersonalizacionScreen(
                                        isDarkMode = isDarkMode,
                                        onThemeChange = { isDarkMode = it },
                                        selectedColor = userSelectedColor,
                                        onColorChange = { userSelectedColor = it
                                            userSecundarioColor = calcularColorSecundario(it, isDarkMode) },
                                        lucesActivas = lucesActivas,
                                        onLucesChange = { lucesActivas = it },
                                        onFinish = {
                                            navController.navigate("home") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        },
                                        userViewModel = userViewModel
                                    )
                                }
                                composable("home") {
                                    MainScreen(
                                        onCreateJamClick = {
                                            navController.navigate("crear_jam")
                                        },
                                        onNavigateToGestionar = { jam ->
                                            navController.navigate("gestionar_jam/${jam.id}")
                                        },
                                        onNavigateToInvitados = { jam ->
                                            navController.navigate("invitados/${jam.id}")
                                        },
                                        onNavigateToChat = { jam ->
                                            navController.navigate("chat/${jam.id}/${java.net.URLEncoder.encode(jam.titulo, "UTF-8")}")
                                        },
                                        onNavigateToPerfilPublico = { uid ->
                                            navController.navigate("perfil_publico/$uid")
                                        },
                                        onNavigateToDetalleJam = { jam ->
                                            navController.navigate("detalle_jam/${jam.id}")
                                        },
                                        onNavigateToChatDirecto = { uid ->
                                            navController.navigate("amigos")
                                        },
                                        onNavigateToComunidad = {
                                            navController.navigate("comunidad")
                                        },
                                        onCerrarSesion = {
                                            val prefs = getSharedPreferences(
                                                "jam_prefs", MODE_PRIVATE
                                            )
                                            prefs.edit()
                                                .putBoolean("recordar_sesion", false)
                                                .apply()
                                            navController.navigate("login") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        },
                                        onCuentaEliminada = {
                                            val prefs = getSharedPreferences(
                                                "jam_prefs", MODE_PRIVATE
                                            )
                                            prefs.edit()
                                                .putBoolean("recordar_sesion", false)
                                                .apply()
                                            navController.navigate("login") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        },
                                        userViewModel = userViewModel,
                                        jamViewModel = jamViewModel
                                    )
                                }
                                composable("comunidad") {
                                    ComunidadScreen(
                                        userViewModel = userViewModel,
                                        onVolver = { navController.popBackStack() }
                                    )
                                }
                                composable("terminos") {
                                    TermsScreen(
                                        onVolver = { navController.popBackStack() }
                                    )
                                }
                                composable("crear_jam") {
                                    CrearJamScreen(
                                        onVolver = { navController.popBackStack() },
                                        jamViewModel = jamViewModel,
                                        userViewModel = userViewModel
                                    )
                                }
                                composable("invitados/{jamId}") {
                                    val jamId = it.arguments?.getString("jamId") ?: return@composable
                                    val jamsActivos by jamViewModel.jamsActivos.collectAsState()
                                    val misJams by jamViewModel.misJams.collectAsState()
                                    val jam = (jamsActivos + misJams).find { j -> j.id == jamId }
                                    if (jam != null) {
                                        InvitadosScreen(
                                            jam = jam,
                                            jamViewModel = jamViewModel,
                                            onVolver = { navController.popBackStack() },
                                            onVerPerfil = { otroUid ->
                                                navController.navigate("perfil_publico/$otroUid")
                                            }
                                        )
                                    } else {
                                        Box(modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                                composable("chat/{jamId}/{jamTitulo}") {
                                    val jamId = it.arguments?.getString("jamId") ?: return@composable
                                    val jamTitulo = it.arguments?.getString("jamTitulo")?.let {
                                        java.net.URLDecoder.decode(it, "UTF-8") } ?: ""
                                    ChatScreen(
                                        jamId = jamId,
                                        jamTitulo = jamTitulo,
                                        jamViewModel = jamViewModel,
                                        onVolver = { navController.popBackStack() }
                                    )
                                }
                                composable("gestionar_jam/{jamId}") {
                                    val jamId = it.arguments?.getString("jamId") ?: return@composable
                                    val jamsActivos by jamViewModel.jamsActivos.collectAsState()
                                    val misJams by jamViewModel.misJams.collectAsState()
                                    val jam = (jamsActivos + misJams).find { j -> j.id == jamId }
                                    if (jam != null) {
                                        GestionarJamScreen(
                                            jam = jam,
                                            jamViewModel = jamViewModel,
                                            onVolver = { navController.popBackStack() },
                                            onInvitados = {
                                                navController.navigate("invitados/${jam.id}")
                                            },
                                            onChat = {
                                                navController.navigate("chat/${jam.id}/${java.net.URLEncoder.encode(jam.titulo, "UTF-8")}")
                                            },
                                            onVerPerfil = { otroUid ->
                                                navController.navigate("perfil_publico/$otroUid")
                                            }
                                        )
                                    } else {
                                        Box(modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                                composable(
                                    "perfil_publico/{uid}",
                                    enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
                                    exitTransition = { fadeOut(animationSpec = tween(300)) }
                                ) {
                                    val uid = it.arguments?.getString("uid") ?: return@composable
                                    PerfilPublicoScreen(
                                        uid = uid,
                                        userViewModel = userViewModel,
                                        onVolver = { navController.popBackStack() },
                                        onVerPerfil = { otroUid ->
                                            navController.navigate("perfil_publico/$otroUid")
                                        },
                                        onIniciarChat = { otroUid ->
                                            navController.navigate("chat_directo/$otroUid")
                                        }
                                    )
                                }
                                composable(
                                    "amigos",
                                    enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
                                    exitTransition = { fadeOut(animationSpec = tween(300)) }
                                ) {
                                    AmigosScreen(
                                        conversacionViewModel = viewModel(),
                                        onVolver = { navController.popBackStack() },
                                        onAbrirChat = { otroUid ->
                                            navController.navigate("chat_directo/$otroUid")
                                        }
                                    )
                                }
                                composable(
                                    "chat_directo/{otroUid}",
                                    enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
                                    exitTransition = { fadeOut(animationSpec = tween(300)) }
                                ) {
                                    val otroUid = it.arguments?.getString("otroUid") ?: return@composable
                                    ChatDirectoScreen(
                                        otroUid = otroUid,
                                        conversacionViewModel = viewModel(),
                                        onVolver = { navController.popBackStack() }
                                    )
                                }
                                composable("detalle_jam/{jamId}") {
                                    val jamId = it.arguments?.getString("jamId") ?: return@composable
                                    val jamsFeed by jamViewModel.jams.collectAsState()
                                    val misJams by jamViewModel.misJams.collectAsState()
                                    val jamsActivos by jamViewModel.jamsActivos.collectAsState()
                                    val jam = (jamsFeed + misJams + jamsActivos).find { j -> j.id == jamId }
                                    if (jam != null) {
                                        DetalleJamScreen(
                                            jam = jam,
                                            jamViewModel = jamViewModel,
                                            onVolver = { navController.popBackStack() },
                                            onVerPerfil = { otroUid ->
                                                navController.navigate("perfil_publico/$otroUid")
                                            },
                                            onUnirse = {
                                                jamViewModel.solicitarUnirse(jam,
                                                    onSuccess = {
                                                        Toast.makeText(this@MainActivity,
                                                            "Solicitud enviada", Toast.LENGTH_SHORT).show()
                                                        navController.popBackStack()
                                                    },
                                                    onError = { error ->
                                                        Toast.makeText(this@MainActivity,
                                                            error, Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            }
                                        )
                                    } else {
                                        Box(modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                            }

                            if (currentRoute == "login" || currentRoute == "registro") {
                                TextButton(
                                    onClick = { lucesActivas = !lucesActivas },
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 16.dp)
                                ) {
                                    Text(
                                        text = if (lucesActivas) "Pausar luces 🛑"
                                        else "Reanudar luces 🪩",
                                        color = Color.Gray
                                    )
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