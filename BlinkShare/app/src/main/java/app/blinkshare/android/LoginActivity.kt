package app.blinkshare.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import app.blinkshare.android.databinding.ActivityLoginBinding
import app.blinkshare.android.model.User
import app.blinkshare.android.utills.AppUtils
import app.blinkshare.android.utills.isNetworkAvailable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        initListeners()
    }

    private fun initListeners(){
        binding.btnLogin.setOnClickListener {
            hideShowLoading(true)
            if(isNetworkAvailable()) {
                loginWithEmailAndPassword()
            }
            else{
                val dialog = NetworkPopUp()
                dialog.show(supportFragmentManager, "NetworkPopUp")
            }
        }
        binding.tvCreateAccountNow.setOnClickListener {
            startActivity(Intent(applicationContext, WelcomeEulaActivity::class.java))
            finish()
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(applicationContext, PasswordResetActivity::class.java))
        }
        binding.btnVisitAsGuest.setOnClickListener {
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }

    }

    private fun loginWithEmailAndPassword(){
        auth.signInWithEmailAndPassword(binding.etEmail.text.toString().trim(), binding.etPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("signInWithEmail", "signInWithEmail:success")
                    AppUtils().setUserLoggedIn(applicationContext)
                    if(auth.currentUser?.isEmailVerified == true){

                        val docRef = db.collection("Users").document(auth.currentUser?.uid!!)
                        docRef.get().addOnSuccessListener { documentSnapshot ->
                            hideShowLoading(false)
                            val user = documentSnapshot.toObject(User::class.java)
                            applicationContext.setIsAdmin(user?.isAdmin?:false)
                            val sIntent = Intent(applicationContext, MainActivity::class.java)
                            sIntent.putExtra("isFromLogin", true)
                            startActivity(sIntent)
                            finish()
                        }.addOnFailureListener {
                            hideShowLoading(false)
                        }
                    }
                    else{
                        hideShowLoading(false)
                        val dialog = VerificationPopup(object: VerificationPopup.OnItemClickListener{
                            override fun onResendClick() {
                                if(isNetworkAvailable()) {
                                    hideShowLoading(true)
                                    auth.currentUser?.sendEmailVerification()
                                        ?.addOnCompleteListener {
                                            hideShowLoading(false)
                                            Toast.makeText(
                                                baseContext,
                                                "Verification link sent to your email. Please verify your email.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }?.addOnFailureListener {
                                        hideShowLoading(false)
                                        Toast.makeText(
                                            baseContext,
                                            it.message.toString(),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                                else{
                                    val dialog = NetworkPopUp()
                                    dialog.show(supportFragmentManager, "NetworkPopUp")
                                }
                            }

                            override fun onCloseClick() {

                            }

                        }, false)
                        dialog.show(supportFragmentManager, "VerificationPopup")
                    }
                } else {
                    hideShowLoading(false)
                    // If sign in fails, display a message to the user.
                    Log.w("signInWithEmail", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Invalid Email or Password",
                        Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                hideShowLoading(false)
                Toast.makeText(baseContext, it.message.toString(),
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun hideShowLoading(show: Boolean){
        if(show) {
            binding.rlProgressLoading.visibility = View.VISIBLE
            binding.animationView.visibility = View.VISIBLE
        }
        else{
            binding.rlProgressLoading.visibility = View.GONE
            binding.animationView.visibility = View.GONE
        }
    }
}