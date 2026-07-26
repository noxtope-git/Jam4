package com.noxtope.jam.ui.theme

data class PaisInfo(
    val nombre: String,
    val bandera: String,
    val codigoTel: String
)

// Valida el RUT chileno con dígito verificador
fun validarRutChileno(rut: String): Boolean {
    val limpio = rut.replace(".", "").replace("-", "").trim().uppercase()
    if (limpio.length < 2) return false

    val cuerpo = limpio.dropLast(1)
    val dv = limpio.last()

    if (!cuerpo.all { it.isDigit() }) return false

    var suma = 0
    var multiplicador = 2
    for (i in cuerpo.reversed()) {
        suma += Character.getNumericValue(i) * multiplicador
        multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
    }

    val resto = 11 - (suma % 11)
    val dvEsperado = when (resto) {
        11 -> '0'
        10 -> 'K'
        else -> resto.toString()[0]
    }

    return dv == dvEsperado
}

// Lista de países (puedes expandirla cuando quieras)
fun obtenerListaPaises(): List<PaisInfo> {
    return listOf(
        PaisInfo("Chile", "🇨🇱", "+56"),
        PaisInfo("Argentina", "🇦🇷", "+54"),
        PaisInfo("Perú", "🇵🇪", "+51"),
        PaisInfo("Bolivia", "🇧🇴", "+591"),
        PaisInfo("Colombia", "🇨🇴", "+57"),
        PaisInfo("Ecuador", "🇪🇨", "+593"),
        PaisInfo("Venezuela", "🇻🇪", "+58"),
        PaisInfo("Uruguay", "🇺🇾", "+598"),
        PaisInfo("Paraguay", "🇵🇾", "+595"),
        PaisInfo("Brasil", "🇧🇷", "+55"),
        PaisInfo("México", "🇲🇽", "+52"),
        PaisInfo("Estados Unidos", "🇺🇸", "+1"),
        PaisInfo("Canadá", "🇨🇦", "+1"),
        PaisInfo("España", "🇪🇸", "+34"),
        PaisInfo("Francia", "🇫🇷", "+33"),
        PaisInfo("Italia", "🇮🇹", "+39"),
        PaisInfo("Alemania", "🇩🇪", "+49"),
        PaisInfo("Reino Unido", "🇬🇧", "+44"),
        PaisInfo("Portugal", "🇵🇹", "+351"),
        PaisInfo("Costa Rica", "🇨🇷", "+506"),
        PaisInfo("Panamá", "🇵🇦", "+507"),
        PaisInfo("Guatemala", "🇬🇹", "+502"),
        PaisInfo("República Dominicana", "🇩🇴", "+1"),
        PaisInfo("Honduras", "🇭🇳", "+504"),
        PaisInfo("El Salvador", "🇸🇻", "+503"),
        PaisInfo("Nicaragua", "🇳🇮", "+505"),
        PaisInfo("Cuba", "🇨🇺", "+53"),
        PaisInfo("Japón", "🇯🇵", "+81"),
        PaisInfo("China", "🇨🇳", "+86"),
        PaisInfo("Corea del Sur", "🇰🇷", "+82"),
        PaisInfo("Australia", "🇦🇺", "+61")
    ).sortedBy { it.nombre }
}