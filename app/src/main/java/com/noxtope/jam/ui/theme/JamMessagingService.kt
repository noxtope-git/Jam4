package com.noxtope.jam.ui.theme

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class JamMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        guardarToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        mostrarNotificacion(message.notification?.title, message.notification?.body)
    }

    private fun guardarToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("usuarios").document(uid)
            .update("fcmToken", token)
            .addOnFailureListener {
                FirebaseFirestore.getInstance().collection("usuarios").document(uid)
                    .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
            }
    }

    private fun mostrarNotificacion(titulo: String?, cuerpo: String?) {
        val canal = "jam_general"
        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canalNotif = NotificationChannel(
                canal,
                "Notificaciones de Jam!",
                NotificationManager.IMPORTANCE_HIGH
            )
            notifManager.createNotificationChannel(canalNotif)
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(this, canal)
        } else {
            android.app.Notification.Builder(this)
        }

        builder
            .setSmallIcon(com.noxtope.jam.R.drawable.jam_foreground)
            .setContentTitle(titulo ?: "Jam!")
            .setContentText(cuerpo ?: "")
            .setAutoCancel(true)

        notifManager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
    }
}
