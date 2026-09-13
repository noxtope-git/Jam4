package com.noxtope.jam.ui.theme

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

data class Idioma(val codigo: String, val nombre: String)

val IDIOMAS_SOPORTADOS = listOf(
    Idioma("es", "Español"),
    Idioma("en", "English")
)

private const val PREFS = "jam_prefs"
private const val KEY_IDIOMA = "idioma"

fun obtenerIdioma(context: Context): String =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(KEY_IDIOMA, "es") ?: "es"

fun guardarIdioma(context: Context, codigo: String) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit().putString(KEY_IDIOMA, codigo).apply()
}

fun aplicarIdioma(context: Context): Context {
    val codigo = obtenerIdioma(context)
    val locale = Locale.forLanguageTag(codigo)
    Locale.setDefault(locale)
    val config = Configuration(context.resources.configuration)
    config.setLocales(LocaleList.forLanguageTags(codigo))
    return context.createConfigurationContext(config)
}
