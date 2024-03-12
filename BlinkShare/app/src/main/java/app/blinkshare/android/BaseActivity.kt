package app.blinkshare.android

import android.app.Application
import com.google.firebase.FirebaseApp

class BaseActivity: Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}