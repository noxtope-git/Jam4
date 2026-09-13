package com.noxtope.jam.ui.theme

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noxtope.jam.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatosPersonalesScreen(
    onFinish: () -> Unit,
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoading by userViewModel.isLoading.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var paisSeleccionado by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var numeroIdentidad by remember { mutableStateOf("") }
    var verificando by remember { mutableStateOf(false) }
    var mostrarDialogoPais by remember { mutableStateOf(false) }

    val paises = remember { obtenerListaPaises() }
    val paisFiltrado = paises.firstOrNull { it.nombre == paisSeleccionado }

    // Diálogo selector de país con buscador
    if (mostrarDialogoPais) {
        var busquedaPais by remember { mutableStateOf("") }
        val paisesFiltrados = paises.filter {
            it.nombre.contains(busquedaPais, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { mostrarDialogoPais = false },
            title = { Text(stringResource(R.string.datos_seleccionar_pais)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = busquedaPais,
                        onValueChange = { busquedaPais = it },
                        label = { Text(stringResource(R.string.datos_buscar_pais)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(paisesFiltrados) { pais ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        paisSeleccionado = pais.nombre
                                        mostrarDialogoPais = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${pais.bandera}  ", fontSize = 20.sp)
                                Text(
                                    pais.nombre,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    pais.codigoTel,
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoPais = false }) {
                    Text(stringResource(R.string.cerrar))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.datos_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.datos_subtitle),
            fontSize = 13.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text(stringResource(R.string.datos_nombre)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = apellidos,
            onValueChange = { apellidos = it },
            label = { Text(stringResource(R.string.datos_apellidos)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Selector de país (abre diálogo con buscador)
        OutlinedTextField(
            value = paisSeleccionado,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.datos_pais)) },
            placeholder = { Text(stringResource(R.string.datos_toca_pais)) },
            leadingIcon = {
                if (paisFiltrado != null) Text("  ${paisFiltrado.bandera}", fontSize = 20.sp)
                else Icon(Icons.Filled.Search, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { mostrarDialogoPais = true },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onBackground,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = Color.Gray,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onBackground,
                disabledPlaceholderColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it.filter { c -> c.isDigit() } },
            label = { Text(stringResource(R.string.datos_telefono)) },
            prefix = {
                if (paisFiltrado != null) Text("${paisFiltrado.codigoTel} ")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = numeroIdentidad,
            onValueChange = { numeroIdentidad = it },
            label = {
                Text(
                    if (paisSeleccionado == "Chile") stringResource(R.string.datos_rut)
                    else stringResource(R.string.datos_identidad)
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.datos_identidad_unica),
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                when {
                    nombre.isBlank() ->
                        Toast.makeText(context, context.getString(R.string.datos_ingresa_nombre), Toast.LENGTH_SHORT).show()
                    apellidos.isBlank() ->
                        Toast.makeText(context, context.getString(R.string.datos_ingresa_apellidos), Toast.LENGTH_SHORT).show()
                    paisSeleccionado.isBlank() ->
                        Toast.makeText(context, context.getString(R.string.datos_selecciona_pais), Toast.LENGTH_SHORT).show()
                    telefono.length < 6 ->
                        Toast.makeText(context, context.getString(R.string.datos_telefono_valido), Toast.LENGTH_SHORT).show()
                    numeroIdentidad.isBlank() ->
                        Toast.makeText(context, context.getString(R.string.datos_ingresa_identidad), Toast.LENGTH_SHORT).show()
                    paisSeleccionado == "Chile" && !validarRutChileno(numeroIdentidad) ->
                        Toast.makeText(context, context.getString(R.string.datos_rut_invalido), Toast.LENGTH_SHORT).show()
                    else -> {
                        verificando = true
                        val telefonoCompleto = "${paisFiltrado?.codigoTel ?: ""} $telefono"
                        val idNormalizado = numeroIdentidad.trim().uppercase()

                        userViewModel.guardarDatosPersonales(
                            nombre = nombre.trim(),
                            apellidos = apellidos.trim(),
                            pais = paisSeleccionado,
                            telefono = telefonoCompleto,
                            numeroIdentidad = idNormalizado,
                            onSuccess = {
                                verificando = false
                                Toast.makeText(
                                    context, context.getString(R.string.datos_verificada), Toast.LENGTH_SHORT
                                ).show()
                                onFinish()
                            },
                            onError = { error ->
                                verificando = false
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
            },
            enabled = !isLoading && !verificando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50)
        ) {
            if (isLoading || verificando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    stringResource(R.string.continuar),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}