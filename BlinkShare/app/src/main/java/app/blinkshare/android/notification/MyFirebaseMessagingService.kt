package app.blinkshare.android.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import app.blinkshare.android.R
import app.blinkshare.android.SplashActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService: FirebaseMessagingService() {
    private val TAG: String = "FirebaseMessaging->"
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // ...

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.e(TAG, "From: " + remoteMessage.from)

        // Check if message contains a data payload.
        if (!remoteMessage.data.isNullOrEmpty()) {
            Log.e(TAG, "Message data payload: " + remoteMessage.data)
            val productId = remoteMessage.data["product_id"]
            val body = remoteMessage.data["body"].toString()
            val sIntent = Intent(this, SplashActivity::class.java)
            sIntent.putExtra("product_id", productId)
            sIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            @SuppressLint("UnspecifiedImmutableFlag") val pendingIntent = PendingIntent.getActivity(
                this, 0 /* Request code */, sIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            sendNotification(body, pendingIntent)
        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.e(
                TAG, "Message Notification Body: " + remoteMessage.notification!!
                    .body
            )
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private fun sendNotification(messageBody: String?, pendingIntent: PendingIntent) {
        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo_notification_small)
            .setContentTitle("BlinkShare")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        try {
            var auth: FirebaseAuth = FirebaseAuth.getInstance()
            var db: FirebaseFirestore = FirebaseFirestore.getInstance()
            val dataToken = hashMapOf(
                "token" to token,
            )
            db.collection("Tokens").document(auth.currentUser?.uid!!)
                .set(dataToken)
        }catch (ex: Exception){

        }
    }
}