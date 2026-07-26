package com.noxtope.jam.ui.theme

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegistroScreen(
    onNavigateToCustomization: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Crea tu cuenta",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Solo necesitas un correo y una contraseña para empezar",
            fontSize = 13.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it.trim()
                emailError = it.isNotEmpty() &&
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
            },
            label = { Text("Correo Electrónico") },
            isError = emailError,
            supportingText = {
                if (emailError) Text("Ingresa un correo válido")
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                val hasUppercase = it.any { c -> c.isUpperCase() }
                val hasNumber = it.any { c -> c.isDigit() }
                passwordError = it.isNotEmpty() &&
                        (it.length < 8 || !hasUppercase || !hasNumber)
            },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError,
            supportingText = {
                if (passwordError) Text("Mínimo 8 caracteres, 1 mayúscula y 1 número")
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                "Acepto los Términos y Condiciones",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                when {
                    email.isBlank() || password.isBlank() ->
                        Toast.makeText(
                            context,
                            "Rellena todos los campos",
                            Toast.LENGTH_SHORT
                        ).show()
                    emailError || passwordError ->
                        Toast.makeText(
                            context,
                            "Corrige los errores antes de continuar",
                            Toast.LENGTH_SHORT
                        ).show()
                    !termsAccepted ->
                        Toast.makeText(
                            context,
                            "Debes aceptar los términos",
                            Toast.LENGTH_SHORT
                        ).show()
                    else -> {
                        isLoading = true
                        authViewModel.crearCuenta(
                            email = email,
                            contrasena = password,
                            onSuccess = {
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    "¡Cuenta creada!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onNavigateToCustomization()
                            },
                            onError = { mensajeError ->
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    "Error: $mensajeError",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Siguiente: Verificar identidad",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}