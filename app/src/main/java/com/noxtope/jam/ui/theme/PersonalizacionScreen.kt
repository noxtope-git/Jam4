package com.noxtope.jam.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noxtope.jam.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonalizacionScreen(
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
    lucesActivas: Boolean,
    onLucesChange: (Boolean) -> Unit,
    onFinish: () -> Unit,
    userViewModel: UserViewModel = viewModel(),
    jamViewModel: JamViewModel = viewModel()
) {
    val context = LocalContext.current
    val tagsGlobales by jamViewModel.tagsGlobales.collectAsState()

    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    var etiquetasSeleccionadas by remember { mutableStateOf(setOf<String>()) }
    var mostrarDialogoEtiqueta by remember { mutableStateOf(false) }
    var nuevaEtiqueta by remember { mutableStateOf("") }
    var busquedaTag by remember { mutableStateOf("") }

    var mostrarSelectorColor by remember { mutableStateOf(false) }

    var mostrarDialogoEtiquetas by remember { mutableStateOf(false) }
    var tagSeleccionTemp by remember { mutableStateOf(setOf<String>()) }
    var tagBusqueda by remember { mutableStateOf("") }
    var mostrarDialogoCrearTag by remember { mutableStateOf(false) }
    var nuevaTag by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        jamViewModel.cargarTags()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.pers_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.pers_subtitle), fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))

        // USERNAME
        Text(
            stringResource(R.string.pers_username),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(R.string.pers_username_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // BIOGRAFÍA
        Text(
            stringResource(R.string.pers_bio),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text(stringResource(R.string.pers_bio_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        // COLOR (selector de paleta)
        Text(
            stringResource(R.string.color_vibra),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        val colorSecundario = remember(selectedColor, isDarkMode) {
            calcularColorSecundario(selectedColor, isDarkMode)
        }

        Card(
            onClick = { mostrarSelectorColor = true },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(selectedColor)
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.pers_toca_color),
                        fontWeight = FontWeight.Medium, fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.pers_color_sec),
                        fontSize = 11.sp, color = Color.Gray)
                }
                Box(
                    modifier = Modifier.size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colorSecundario)
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ETIQUETAS (tarjeta que abre diálogo, como PerfilScreen)
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
                    .padding(12.dp)
                    .clickable {
                        tagSeleccionTemp = etiquetasSeleccionadas
                        tagBusqueda = ""
                        mostrarDialogoEtiquetas = true
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.intereses), fontWeight = FontWeight.Bold, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (etiquetasSeleccionadas.isEmpty()) stringResource(R.string.toca_anadir_etiquetas)
                        else etiquetasSeleccionadas.joinToString(", "),
                        fontSize = 11.sp, color = Color.Gray, maxLines = 1
                    )
                }
                Icon(Icons.Filled.Add, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MODO OSCURO + COLOR (juntos como en PerfilScreen)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.modo_oscuro), fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.pers_modo_oscuro_desc), fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(checked = isDarkMode, onCheckedChange = { onThemeChange(it) })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.color_vibra), fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.pers_toca_elegir), fontSize = 12.sp, color = Color.Gray)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(selectedColor)
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .clickable { mostrarSelectorColor = true }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // MODO LUCES
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
                    Text(stringResource(R.string.modo_luces), fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.pers_luces_desc),
                        fontSize = 12.sp, color = Color.Gray)
                }
                Switch(checked = lucesActivas, onCheckedChange = { onLucesChange(it) })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val colorSec = calcularColorSecundario(selectedColor, isDarkMode)
                userViewModel.guardarPerfil(
                    context = context,
                    username = username.ifBlank { "usuario" },
                    bio = bio,
                    etiquetas = etiquetasSeleccionadas.toList(),
                    mostrarNombreReal = false,
                    mostrarEmail = false,
                    onSuccess = {
                        userViewModel.guardarPreferencias(
                            colorPrimario = selectedColor.toArgbLong(),
                            colorSecundario = colorSec.toArgbLong(),
                            modoOscuro = isDarkMode,
                            lucesActivas = lucesActivas,
                            onSuccess = { onFinish() }
                        )
                    },
                    onError = {
                        userViewModel.guardarPreferencias(
                            colorPrimario = selectedColor.toArgbLong(),
                            colorSecundario = colorSec.toArgbLong(),
                            modoOscuro = isDarkMode,
                            lucesActivas = lucesActivas,
                            onSuccess = { onFinish() }
                        )
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                stringResource(R.string.pers_listo),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Diálogo de selección de etiquetas (estilo PerfilScreen)
    if (mostrarDialogoEtiquetas) {
        val tagsFiltrados = remember(tagBusqueda, tagsGlobales) {
            tagsGlobales.filter { it.nombre.contains(tagBusqueda, ignoreCase = true) }
        }
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoEtiquetas = false
                tagSeleccionTemp = etiquetasSeleccionadas
            },
            title = {
                Column {
                    Text(stringResource(R.string.intereses), fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.pers_selecciona_10), fontSize = 12.sp, color = Color.Gray)
                        Text("${tagSeleccionTemp.size}/10", fontSize = 12.sp,
                            color = if (tagSeleccionTemp.size >= 10)
                                MaterialTheme.colorScheme.error else Color.Gray)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp)) {
                    OutlinedTextField(
                        value = tagBusqueda,
                        onValueChange = { tagBusqueda = it },
                        placeholder = { Text(stringResource(R.string.pers_buscar_etiqueta)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        if (tagsFiltrados.isEmpty() && tagBusqueda.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = {
                                if (tagSeleccionTemp.size < 10) {
                                    jamViewModel.agregarTagGlobal(
                                        nuevoTag = tagBusqueda,
                                        onYaExiste = {
                                            val existente = tagsGlobales.firstOrNull {
                                                it.nombre.equals(tagBusqueda.trim(), ignoreCase = true)
                                            }?.nombre
                                            if (existente != null && !tagSeleccionTemp.contains(existente))
                                                tagSeleccionTemp = tagSeleccionTemp + existente
                                        },
                                        onAgregado = {
                                            val nuevo = tagBusqueda.trim()
                                            if (!tagSeleccionTemp.contains(nuevo))
                                                tagSeleccionTemp = tagSeleccionTemp + nuevo
                                        }
                                    )
                                }
                                tagBusqueda = ""
                            }) {
                                Text(stringResource(R.string.pers_crear_tag, tagBusqueda.trim()), fontWeight = FontWeight.Bold)
                            }
                        }
                        tagsFiltrados.forEach { tagInfo ->
                            val isSelected = tagSeleccionTemp.contains(tagInfo.nombre)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tagSeleccionTemp = if (isSelected)
                                            tagSeleccionTemp - tagInfo.nombre
                                        else if (tagSeleccionTemp.size < 10)
                                            tagSeleccionTemp + tagInfo.nombre
                                        else tagSeleccionTemp
                                    }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tagInfo.nombre,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp)
                                if (isSelected) {
                                    Icon(Icons.Filled.Check, null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    etiquetasSeleccionadas = tagSeleccionTemp
                    mostrarDialogoEtiquetas = false
                }) { Text(stringResource(R.string.guardar)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoEtiquetas = false
                    tagSeleccionTemp = etiquetasSeleccionadas
                }) { Text(stringResource(R.string.cancelar), color = Color.Gray) }
            }
        )
    }

    // Selector de color (paleta con secciones)
    if (mostrarSelectorColor) {
        SelectorColorDialog(
            titulo = stringResource(R.string.pers_elegir_color),
            colorActual = selectedColor,
            onColorSelected = { color ->
                onColorChange(color)
                mostrarSelectorColor = false
            },
            onDismiss = { mostrarSelectorColor = false }
        )
    }
}