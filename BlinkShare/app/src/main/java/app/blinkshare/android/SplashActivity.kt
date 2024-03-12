package app.blinkshare.android

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import app.blinkshare.android.databinding.ActivitySplashBinding
import app.blinkshare.android.model.User
import app.blinkshare.android.notification.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private var apiService: APIService? = null
    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var productId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //GoogleApiAvailability.makeGooglePlayServicesAvailable()
        apiService = Client.getClient("https://fcm.googleapis.com")?.create(APIService::class.java)
        if(intent != null){
            if(intent.hasExtra("product_id")){
                productId = intent.extras?.get("product_id").toString()
            }
        }
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        subscribe("new_post")
//        sendNotification()
        if(auth.currentUser != null) {

            val docRef = db.collection("Users").document(auth.currentUser?.uid!!)
            docRef.get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                applicationContext.setIsAdmin(user?.isAdmin ?: false)
                val sIntent = Intent(applicationContext, MainActivity::class.java)
                sIntent.putExtra("isFromLogin", true)
                if(productId != null) {
                    sIntent.putExtra("product_id", productId)
                }
                startActivity(sIntent)
                finish()
            }.addOnFailureListener {
                Log.e("Splash->", it.message.toString())
            }
        }else{
            val sIntent = Intent(applicationContext, MainActivity::class.java)
            sIntent.putExtra("isFromLogin", true)
            if(productId != null) {
                sIntent.putExtra("product_id", productId)
            }
            startActivity(sIntent)
            finish()
        }
    }

    private fun sendNotification(){
        val data = Data("", R.drawable.logo_notification_small, "New object is added on map", "")
        val sender = Sender(data, "fyrRi_XUSnePn8G3c2BiGQ:APA91bGxEqoPqVEC4Sv9RRG1yE-eNLC2e6wA4Sk1MIbUFe90F5IsxHRFM_qYoyKA1u5Id3XBZhBrtlZ_hU6fhNHTfSd1O4AIaahLC9PfG7N2n5KMpungcSlmjCNu2gHmupdWt7_MDSSz")
        apiService!!.sendNotification(sender)
            ?.enqueue(object : Callback<MyResponse?> {
                override fun onResponse(
                    call: Call<MyResponse?>,
                    response: Response<MyResponse?>
                ) {
                    if (response.code() == 200) {
                        if (response.body()!!.success !== 1) {
                            //error
                            Log.e("sendNotification e->", response.toString())

                        }
                        else{
                            Log.e("sendNotification s->", response.message().toString())

                        }
                    }
                }

                override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                    Log.e("sendNotification f->", t.message.toString())

                }

            })
    }
    private fun subscribe(topic: String){
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Subscribed"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Log.d("subscribe topic->", msg)
                //Toast.makeText(this@SplashActivity, msg, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                //Toast.makeText(this@SplashActivity, it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }
}