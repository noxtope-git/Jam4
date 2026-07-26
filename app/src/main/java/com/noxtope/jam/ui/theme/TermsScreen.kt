package com.noxtope.jam.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(onVolver: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Términos y Condiciones") },
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Términos y Condiciones de Uso",
                fontSize = 14.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Última actualización: Junio 2026",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(20.dp))

            val sections = listOf(
                "1. Aceptación de los Términos" to
                "Al descargar, acceder o utilizar la aplicación Jam! (\"la App\"), aceptas estar sujeto a estos Términos y Condiciones. Si no estás de acuerdo, no uses la App.",

                "2. Descripción del Servicio" to
                "Jam! es una plataforma social que permite a los usuarios crear y unirse a eventos sociales temporales llamados 'Jams'. Los usuarios pueden conectarse con personas cercanas, chatear en grupo y coordinar encuentros en persona.",

                "3. Cuentas de Usuario" to
                "Para usar la App debes crear una cuenta proporcionando información veraz. Eres responsable de mantener la confidencialidad de tus credenciales. Debes ser mayor de 13 años (o la edad mínima en tu país) para usar la App.",

                "4. Plan Gratuito y Apoyo" to
                "El plan gratuito permite crear y unirse a un máximo de 10 Jams por semana, con un límite de 15 participantes por Jam. El apoyo voluntario a la comunidad elimina estas restricciones y ofrece funciones adicionales como hasta 150 participantes, sin publicidad y acceso prioritario al desarrollo del proyecto.",

                "5. Donaciones" to
                "Las donaciones son voluntarias y se realizan mediante la compra de puntos de apoyo dentro de la App. Al adquirir puntos, obtendrás automáticamente acceso a funciones mejoradas como agradecimiento. Las donaciones no son reembolsables.",

                "6. Contenido Generado por el Usuario" to
                "Eres responsable del contenido que publicas. No debes publicar contenido ilegal, ofensivo, discriminatorio o que infrinja derechos de terceros. Nos reservamos el derecho de eliminar contenido y/o suspender cuentas que violen estos términos.",

                "7. Privacidad y Datos" to
                "La App recopila datos personales como nombre, número de identidad, ubicación y preferencias. Estos datos se almacenan de forma segura en Firebase (Google Cloud). No compartimos tus datos con terceros sin tu consentimiento. Puedes solicitar la eliminación de tus datos en cualquier contacto@jam-app.com.",

                "8. Limitación de Responsabilidad" to
                "Jam! es una plataforma de coordinación social. No nos hacemos responsables por incidentes que ocurran durante encuentros organizados a través de la App. Los usuarios interactúan bajo su propia responsabilidad. Recomendamos reunirse en lugares públicos y tomar precauciones de seguridad.",

                "9. Cancelación y Eliminación de Cuenta" to
                "Puedes eliminar tu cuenta en cualquier momento desde la sección de Perfil. Al eliminar tu cuenta, tus datos personales serán eliminados de nuestros servidores. Las Jams que hayas creado quedarán registradas sin tu información personal.",

                "10. Modificaciones" to
                "Nos reservamos el derecho de modificar estos términos en cualquier momento. Los cambios serán notificados a través de la App. El uso continuado después de los cambios constituye aceptación de los nuevos términos.",

                "11. Contacto" to
                "Para consultas sobre estos términos, contáctanos en: contacto@jam-app.com"
            )

            sections.forEach { (title, body) ->
                Text(title,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text(body,
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
