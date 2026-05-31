package com.ebchat.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.ebchat.MainActivity
import com.ebchat.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class EBChatMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        showMessageNotification(
            title = message.notification?.title ?: message.data["title"] ?: "EB Chat",
            body = message.notification?.body ?: message.data["body"] ?: "নতুন মেসেজ এসেছে",
            chatId = message.data["chatId"] ?: "",
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        getSharedPreferences("eb_chat", MODE_PRIVATE).edit().putString("fcm_token", token).apply()
    }

    private fun showMessageNotification(title: String, body: String, chatId: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_MESSAGES, "Messages", NotificationManager.IMPORTANCE_HIGH).apply {
                    enableVibration(true)
                }
            )
        }

        val openIntent = PendingIntent.getActivity(
            this,
            chatId.hashCode(),
            Intent(this, MainActivity::class.java).putExtra("chatId", chatId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val replyInput = RemoteInput.Builder(KEY_INLINE_REPLY).setLabel("Reply").build()
        val replyIntent = PendingIntent.getBroadcast(
            this,
            chatId.hashCode(),
            Intent(ACTION_INLINE_REPLY).putExtra("chatId", chatId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
        val replyAction = NotificationCompat.Action.Builder(R.drawable.ic_launcher, "Reply", replyIntent)
            .addRemoteInput(replyInput)
            .build()

        val notification = NotificationCompat.Builder(this, CHANNEL_MESSAGES)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 180, 80, 180))
            .addAction(replyAction)
            .build()

        manager.notify(chatId.ifBlank { title }.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_MESSAGES = "eb_chat_messages"
        const val KEY_INLINE_REPLY = "inline_reply"
        const val ACTION_INLINE_REPLY = "com.ebchat.ACTION_INLINE_REPLY"
    }
}

