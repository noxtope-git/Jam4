package com.noxtope.jam.ui.theme

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.noxtope.jam.R

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNecesitaDatos: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    backgroundColor: Color = Color(0xFF666666)
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var recordarSesion by remember { mutableStateOf(true) }
    var animacionLista by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { animacionLista = true }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                isGoogleLoading = true
                authViewModel.iniciarSesionConGoogle(
                    idToken = idToken,
                    onResultado = { necesitaDatos ->
                        isGoogleLoading = false
                        guardarRecordarSesion(context, recordarSesion)
                        if (necesitaDatos) onNecesitaDatos()
                        else onLoginSuccess()
                    },
                    onError = { error ->
                        isGoogleLoading = false
                        Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                )
            }
        } catch (e: ApiException) {
            isGoogleLoading = false
            Toast.makeText(context, "Error Google: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    val bgColor = backgroundColor.copy(alpha = 1f)
    val esClaro = bgColor.esClaro()
    val textoSobreFondo: Color
    val textoSecundarioSobreFondo: Color
    val contenedorInput: Color
    val bordeInput: Color
    val textoInput: Color
    val botonBg: Color
    val botonText: Color
    val divColor: Color

    if (esClaro) {
        textoSobreFondo = Color.Black
        textoSecundarioSobreFondo = Color.Black.copy(alpha = 0.6f)
        contenedorInput = Color.Black.copy(alpha = 0.06f)
        bordeInput = Color.Black.copy(alpha = 0.25f)
        textoInput = Color.Black
        botonBg = Color.Black
        botonText = Color.White
        divColor = Color.Black.copy(alpha = 0.2f)
    } else {
        textoSobreFondo = Color.White
        textoSecundarioSobreFondo = Color.White.copy(alpha = 0.65f)
        contenedorInput = Color.White.copy(alpha = 0.1f)
        bordeInput = Color.White.copy(alpha = 0.35f)
        textoInput = Color.White
        botonBg = Color.White
        botonText = bgColor
        divColor = Color.White.copy(alpha = 0.25f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        bgColor,
                        bgColor.copy(
                            red = (bgColor.red * 0.88f + if (esClaro) 0.12f else 0f).coerceIn(0f, 1f),
                            green = (bgColor.green * 0.88f + if (esClaro) 0.12f else 0f).coerceIn(0f, 1f),
                            blue = (bgColor.blue * 0.88f + if (esClaro) 0.12f else 0f).coerceIn(0f, 1f)
                        )
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(
                visible = animacionLista,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 3 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.jam_foreground),
                        contentDescription = "Jam! Logo",
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Jam!",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = textoSobreFondo,
                        letterSpacing = (-1.5).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "Conecta · Comparte · Crea",
                        fontSize = 13.sp,
                        color = textoSecundarioSobreFondo,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedVisibility(
                visible = animacionLista,
                enter = fadeIn(animationSpec = tween(delayMillis = 200)) + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                Column {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        placeholder = { Text("Correo electrónico", color = textoSecundarioSobreFondo) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textoInput,
                            unfocusedTextColor = textoInput,
                            cursorColor = textoInput,
                            unfocusedBorderColor = bordeInput,
                            focusedBorderColor = textoSobreFondo,
                            unfocusedContainerColor = contenedorInput,
                            focusedContainerColor = contenedorInput
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Contraseña", color = textoSecundarioSobreFondo) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textoInput,
                            unfocusedTextColor = textoInput,
                            cursorColor = textoInput,
                            unfocusedBorderColor = bordeInput,
                            focusedBorderColor = textoSobreFondo,
                            unfocusedContainerColor = contenedorInput,
                            focusedContainerColor = contenedorInput
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            Toast.makeText(context, "Recuperación en construcción 🛠️", Toast.LENGTH_SHORT).show()
                        }) {
                            Text(
                                "¿Olvidaste tu contraseña?",
                                fontSize = 12.sp,
                                color = textoSecundarioSobreFondo
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = recordarSesion,
                            onCheckedChange = { recordarSesion = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = textoSobreFondo,
                                uncheckedColor = textoSecundarioSobreFondo
                            )
                        )
                        Text(
                            "Recordar sesión",
                            fontSize = 14.sp,
                            color = textoSecundarioSobreFondo
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Ingresa tus datos", Toast.LENGTH_SHORT).show()
                            } else {
                                isLoading = true
                                authViewModel.iniciarSesion(
                                    email = email,
                                    contrasena = password,
                                    onResultado = { necesitaDatos ->
                                        isLoading = false
                                        guardarRecordarSesion(context, recordarSesion)
                                        if (necesitaDatos) onNecesitaDatos()
                                        else onLoginSuccess()
                                    },
                                    onError = { errorMensaje ->
                                        isLoading = false
                                        Toast.makeText(context, "Error: $errorMensaje", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading && !isGoogleLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = botonBg,
                            contentColor = botonText
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = botonText,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Entrar", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = divColor)
                        Text("  o  ", color = textoSecundarioSobreFondo, fontSize = 13.sp)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = divColor)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(context.getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            googleSignInClient.signOut().addOnCompleteListener {
                                googleLauncher.launch(googleSignInClient.signInIntent)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, bordeInput),
                        enabled = !isLoading && !isGoogleLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = textoSobreFondo
                        )
                    ) {
                        if (isGoogleLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = textoSobreFondo
                            )
                        } else {
                            Text("G  Continuar con Google", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    TextButton(onClick = onNavigateToRegister) {
                        Text(
                            "¿No tienes cuenta? Crea una aquí",
                            color = textoSobreFondo.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

fun Color.esClaro(): Boolean = 0.299f * red + 0.587f * green + 0.114f * blue > 0.5f

fun Color.esOscuro(): Boolean = !esClaro()

fun Long.corregirColor(): Color {
    val intVal = toInt()
    return if (intVal == 0 && this != 0L) Color((this shr 32).toInt()) else Color(intVal)
}

fun guardarColorSecundario(context: Context, color: Long) {
    val prefs = context.getSharedPreferences("jam_prefs", Context.MODE_PRIVATE)
    prefs.edit().putLong("color_secundario", color).apply()
}

fun obtenerColorSecundario(context: Context): Long {
    val prefs = context.getSharedPreferences("jam_prefs", Context.MODE_PRIVATE)
    return prefs.getLong("color_secundario", 0xFF666666)
}

fun guardarRecordarSesion(context: Context, recordar: Boolean) {
    val prefs = context.getSharedPreferences("jam_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("recordar_sesion", recordar).apply()
}

fun obtenerRecordarSesion(context: Context): Boolean {
    val prefs = context.getSharedPreferences("jam_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("recordar_sesion", false)
}
