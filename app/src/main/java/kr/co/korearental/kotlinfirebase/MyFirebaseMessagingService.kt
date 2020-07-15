package kr.co.korearental.kotlinfirebase

import android.util.Log
import androidx.annotation.NonNull
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {
    override fun onNewToken(@NonNull token: String) {
        // Get updated InstanceID token.
        Log.d(Companion.TAG, "Refreshed token: $token")

        // TODO: Implement this method to send any registration to your app's servers.
        // sendRegistrationToServer(token);
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        var msgBody: String? = ""

        // TODO(developer): Handle FCM messages here.
        Log.d(Companion.TAG, "From: " + remoteMessage.from  )

        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            Log.d(Companion.TAG, "Message data payload: " + remoteMessage.data )
        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            msgBody = remoteMessage.notification!!.body
            Log.d(Companion.TAG,"Message Notification Body: $msgBody" )
        }
    }

    companion object {private const val TAG = "MyFirebaseMsgService"}
}