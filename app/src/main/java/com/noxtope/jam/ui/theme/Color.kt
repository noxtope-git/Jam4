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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

fun calcularColorSecundario(primary: Color, isDarkMode: Boolean): Color {
    val cr = (1f - primary.red).coerceIn(0f, 1f)
    val cg = (1f - primary.green).coerceIn(0f, 1f)
    val cb = (1f - primary.blue).coerceIn(0f, 1f)
    if (isDarkMode) {
        val lum = 0.299f * cr + 0.587f * cg + 0.114f * cb
        return if (lum >= 0.6f) Color(cr, cg, cb)
        else {
            val boost = (0.6f - lum) + 1f
            Color((cr * boost).coerceIn(0f, 1f), (cg * boost).coerceIn(0f, 1f), (cb * boost).coerceIn(0f, 1f))
        }
    } else {
        val lum = 0.299f * cr + 0.587f * cg + 0.114f * cb
        return if (lum <= 0.4f) Color(cr, cg, cb)
        else {
            val reduce = 0.4f / lum
            Color((cr * reduce).coerceIn(0f, 1f), (cg * reduce).coerceIn(0f, 1f), (cb * reduce).coerceIn(0f, 1f))
        }
    }
}

fun obtenerColorContraste(bg: Color): Color {
    val lum = 0.299f * bg.red + 0.587f * bg.green + 0.114f * bg.blue
    return if (lum > 0.5f) Color.Black else Color.White
}

data class ColorInfo(val nombre: String, val color: Color) {
    val hex: String get() {
        val r = (color.red * 255).toInt().coerceIn(0, 255)
        val g = (color.green * 255).toInt().coerceIn(0, 255)
        val b = (color.blue * 255).toInt().coerceIn(0, 255)
        return "#${r.toString(16).padStart(2, '0').uppercase()}${g.toString(16).padStart(2, '0').uppercase()}${b.toString(16).padStart(2, '0').uppercase()}"
    }
}

val paletaColores: List<ColorInfo> = listOf(
    ColorInfo("Rojo", Color(0xFFE53935)),
    ColorInfo("Rojo ladrillo", Color(0xFFD32F2F)),
    ColorInfo("Rojo oscuro", Color(0xFFB71C1C)),
    ColorInfo("Naranja", Color(0xFFFB8C00)),
    ColorInfo("Naranja oscuro", Color(0xFFEF6C00)),
    ColorInfo("Ámbar", Color(0xFFFFC107)),
    ColorInfo("Amarillo", Color(0xFFFDD835)),
    ColorInfo("Amarillo claro", Color(0xFFFFF176)),
    ColorInfo("Dorado", Color(0xFFFFA000)),
    ColorInfo("Lima", Color(0xFFC0CA33)),
    ColorInfo("Verde claro", Color(0xFF8BC34A)),
    ColorInfo("Verde", Color(0xFF43A047)),
    ColorInfo("Verde oscuro", Color(0xFF2E7D32)),
    ColorInfo("Verde bosque", Color(0xFF1B5E20)),
    ColorInfo("Esmeralda", Color(0xFF00BFA5)),
    ColorInfo("Verde azulado", Color(0xFF00897B)),
    ColorInfo("Cian", Color(0xFF00ACC1)),
    ColorInfo("Turquesa", Color(0xFF4DD0E1)),
    ColorInfo("Azul cielo", Color(0xFF42A5F5)),
    ColorInfo("Azul claro", Color(0xFF90CAF9)),
    ColorInfo("Azul", Color(0xFF1E88E5)),
    ColorInfo("Azul oscuro", Color(0xFF1565C0)),
    ColorInfo("Azul marino", Color(0xFF0D47A1)),
    ColorInfo("Índigo", Color(0xFF3949AB)),
    ColorInfo("Índigo oscuro", Color(0xFF283593)),
    ColorInfo("Púrpura", Color(0xFF7B1FA2)),
    ColorInfo("Violeta", Color(0xFF9C27B0)),
    ColorInfo("Uva", Color(0xFF4A148C)),
    ColorInfo("Magenta", Color(0xFFC2185B)),
    ColorInfo("Rosa fuerte", Color(0xFFE91E63)),
    ColorInfo("Rosa", Color(0xFFF06292)),
    ColorInfo("Rosa claro", Color(0xFFF48FB1)),
    ColorInfo("Lavanda", Color(0xFFCE93D8)),
    ColorInfo("Marrón", Color(0xFF8D6E63)),
    ColorInfo("Gris claro", Color(0xFFBDBDBD)),
    ColorInfo("Gris", Color(0xFF757575)),
    ColorInfo("Gris oscuro", Color(0xFF424242)),
    ColorInfo("Negro", Color(0xFF000000)),
    ColorInfo("Blanco hueso", Color(0xFFF5F5F5))
)

data class ColorSeccion(val nombre: String, val colores: List<ColorInfo>)

val seccionesColor: List<ColorSeccion> = listOf(
    ColorSeccion("Rojos", listOf(
        ColorInfo("Rojo", Color(0xFFE53935)),
        ColorInfo("Rojo ladrillo", Color(0xFFD32F2F)),
        ColorInfo("Rojo oscuro", Color(0xFFB71C1C))
    )),
    ColorSeccion("Naranjas & Amarillos", listOf(
        ColorInfo("Naranja", Color(0xFFFB8C00)),
        ColorInfo("Naranja oscuro", Color(0xFFEF6C00)),
        ColorInfo("Ámbar", Color(0xFFFFC107)),
        ColorInfo("Amarillo", Color(0xFFFDD835)),
        ColorInfo("Amarillo claro", Color(0xFFFFF176)),
        ColorInfo("Dorado", Color(0xFFFFA000))
    )),
    ColorSeccion("Verdes", listOf(
        ColorInfo("Lima", Color(0xFFC0CA33)),
        ColorInfo("Verde claro", Color(0xFF8BC34A)),
        ColorInfo("Verde", Color(0xFF43A047)),
        ColorInfo("Verde oscuro", Color(0xFF2E7D32)),
        ColorInfo("Verde bosque", Color(0xFF1B5E20)),
        ColorInfo("Esmeralda", Color(0xFF00BFA5)),
        ColorInfo("Verde azulado", Color(0xFF00897B))
    )),
    ColorSeccion("Azules & Cian", listOf(
        ColorInfo("Cian", Color(0xFF00ACC1)),
        ColorInfo("Turquesa", Color(0xFF4DD0E1)),
        ColorInfo("Azul cielo", Color(0xFF42A5F5)),
        ColorInfo("Azul claro", Color(0xFF90CAF9)),
        ColorInfo("Azul", Color(0xFF1E88E5)),
        ColorInfo("Azul oscuro", Color(0xFF1565C0)),
        ColorInfo("Azul marino", Color(0xFF0D47A1)),
        ColorInfo("Índigo", Color(0xFF3949AB)),
        ColorInfo("Índigo oscuro", Color(0xFF283593))
    )),
    ColorSeccion("Púrpuras & Rosas", listOf(
        ColorInfo("Púrpura", Color(0xFF7B1FA2)),
        ColorInfo("Violeta", Color(0xFF9C27B0)),
        ColorInfo("Uva", Color(0xFF4A148C)),
        ColorInfo("Magenta", Color(0xFFC2185B)),
        ColorInfo("Rosa fuerte", Color(0xFFE91E63)),
        ColorInfo("Rosa", Color(0xFFF06292)),
        ColorInfo("Rosa claro", Color(0xFFF48FB1)),
        ColorInfo("Lavanda", Color(0xFFCE93D8))
    )),
    ColorSeccion("Neutros", listOf(
        ColorInfo("Marrón", Color(0xFF8D6E63)),
        ColorInfo("Gris claro", Color(0xFFBDBDBD)),
        ColorInfo("Gris", Color(0xFF757575)),
        ColorInfo("Gris oscuro", Color(0xFF424242)),
        ColorInfo("Negro", Color(0xFF000000)),
        ColorInfo("Blanco hueso", Color(0xFFF5F5F5))
    ))
)

fun Color.toHex(): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return "#${r.toString(16).padStart(2, '0').uppercase()}${g.toString(16).padStart(2, '0').uppercase()}${b.toString(16).padStart(2, '0').uppercase()}"
}

fun Color.toArgbLong(): Long {
    val a = (alpha * 255).toInt().coerceIn(0, 255)
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return ((a shl 24) or (r shl 16) or (g shl 8) or b).toLong()
}

fun Long.toColor(): Color = Color(toInt())

fun asegurarContrasteVisible(color: Color, isDarkMode: Boolean): Color {
    val lum = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
    return if (isDarkMode) {
        if (lum < 0.35f) {
            val scale = 0.35f / lum.coerceAtLeast(0.01f)
            Color(
                (color.red * scale).coerceIn(0f, 1f),
                (color.green * scale).coerceIn(0f, 1f),
                (color.blue * scale).coerceIn(0f, 1f)
            )
        } else color
    } else {
        if (lum > 0.65f) {
            val scale = 0.65f / lum.coerceAtLeast(0.01f)
            Color(
                (color.red * scale).coerceIn(0f, 1f),
                (color.green * scale).coerceIn(0f, 1f),
                (color.blue * scale).coerceIn(0f, 1f)
            )
        } else color
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectorColorDialog(
    titulo: String,
    colorActual: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var busqueda by remember { mutableStateOf("") }
    var tempColor by remember { mutableStateOf(colorActual) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.heightIn(max = 520.dp)) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = { Text("Buscar color...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val seccionesFiltradas = seccionesColor.mapNotNull { seccion ->
                        val filtrados = if (busqueda.isBlank()) seccion.colores
                        else seccion.colores.filter {
                            it.nombre.contains(busqueda, ignoreCase = true) ||
                            it.hex.contains(busqueda, ignoreCase = true)
                        }
                        if (filtrados.isNotEmpty()) ColorSeccion(seccion.nombre, filtrados) else null
                    }
                    seccionesFiltradas.forEach { seccion ->
                        Text(
                            seccion.nombre,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            seccion.colores.forEach { colorInfo ->
                                val isSel = tempColor == colorInfo.color
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(colorInfo.color)
                                        .border(if (isSel) 3.dp else 1.dp,
                                            if (isSel) Color.White else Color.Gray.copy(alpha = 0.3f), CircleShape)
                                        .clickable { tempColor = colorInfo.color },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSel) Icon(Icons.Filled.Check, null,
                                        tint = obtenerColorContraste(colorInfo.color), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    if (seccionesFiltradas.isEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Sin resultados", color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(tempColor))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(tempColor.toHex(), fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onColorSelected(tempColor) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Elegir", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
        }
    )
}