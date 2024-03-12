package app.blinkshare.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import app.blinkshare.android.databinding.ActivitySignUpBinding
import app.blinkshare.android.utills.AppUtils
import app.blinkshare.android.utills.isNetworkAvailable
import app.blinkshare.android.utills.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        initListeners()
    }

    private fun initListeners() {
        binding.btnCreateAccount.setOnClickListener {
            if (validate()) {
                if(isNetworkAvailable()) {
                    hideShowLoading(true)
                    isUserNameExist()
                }
                else{
                    val dialog = NetworkPopUp()
                    dialog.show(supportFragmentManager, "NetworkPopUp")
                }
            }
        }
        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun isUserNameExist() {

        db.collection("Users").whereEqualTo("userName", binding.etUserName.text.toString())
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result.isEmpty) {
                        if(isNetworkAvailable()) {
                            createWithEmailAndPassword()
                        }
                        else{
                            hideShowLoading(false)
                            val dialog = NetworkPopUp()
                            dialog.show(supportFragmentManager, "NetworkPopUp")
                        }
                    } else {
                        hideShowLoading(false)
                        Toast.makeText(
                            applicationContext,
                            "Username is already exist",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    createWithEmailAndPassword()
                }
            }
            .addOnFailureListener {
                createWithEmailAndPassword()
            }
    }

    private fun createWithEmailAndPassword() {
        auth.createUserWithEmailAndPassword(
            binding.etEmail.text.toString(),
            binding.etPassword.text.toString()
        )
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("createWithEmail", "createUserWithEmail:success")
                    val user = auth.currentUser
                    addUserData(user?.uid ?: auth.uid ?: "", user)

                    //hideShowLoading(false)
//                    startActivity(Intent(applicationContext, LoginActivity::class.java))
//                    finish()
                } else {
                    hideShowLoading(false)
                    // If sign in fails, display a message to the user.
                    Log.w("createWithEmail", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, task.exception?.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }.addOnFailureListener {
                hideShowLoading(false)
                Toast.makeText(
                    baseContext, it.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

    }

    private fun addUserData(uid: String, firebaseUser: FirebaseUser?) {
        //val tag = binding.etTags.text.toString().split("\\s".toRegex())
        //var tags = arrayListOf<String>()
        //tags.addAll(tag)
        val user = hashMapOf(
            "age" to "28",
            "authToken" to uid,
            "avatar" to "",
            "currency" to "USD",
            "firstName" to "",
            "lastName" to "",
            "isNewUser" to true,
            "location" to null,
            "userName" to binding.etUserName.text.toString(),
            "email" to binding.etEmail.text.toString().trim(),
            "isAdmin" to false
        )
        db.collection("Users").document(auth.currentUser?.uid!!)
            .set(user)
            .addOnSuccessListener {
                firebaseUser?.sendEmailVerification()?.addOnCompleteListener {
//                    Toast.makeText(
//                        baseContext,
//                        "Email verification link send to your email. Please verify your email.",
//                        Toast.LENGTH_SHORT
//                    ).show()
                    hideShowLoading(false)
                    val dialog = VerificationPopup(object: VerificationPopup.OnItemClickListener{
                        override fun onResendClick() {

                        }

                        override fun onCloseClick() {
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finish()
                        }

                    }, true)
                    dialog.show(supportFragmentManager, "VerificationPopup")
                }?.addOnFailureListener {
                    hideShowLoading(false)
                    startActivity(Intent(applicationContext, LoginActivity::class.java))
                    finish()
                }
//                hideShowLoading(false)
//                startActivity(Intent(applicationContext, LoginActivity::class.java))
//                finish()
                Log.d(
                    "FireStoreSuccess",
                    "DocumentSnapshot successfully written!" + auth.currentUser?.uid!!
                )
            }
            .addOnFailureListener { e ->
                hideShowLoading(false)
                Log.w(
                    "FireStoreFailure",
                    "Error writing document",
                    e
                )
                Toast.makeText(
                    baseContext, e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun hideShowLoading(show: Boolean) {
        if (show) {
            binding.rlProgressLoading.visibility = View.VISIBLE
            binding.animationView.visibility = View.VISIBLE
        } else {
            binding.rlProgressLoading.visibility = View.GONE
            binding.animationView.visibility = View.GONE
        }
    }

    private fun validate(): Boolean {
        if (binding.etUserName.text.isNullOrBlank()) {
            this.toast("Please enter Username")
            return false
        }
        if (!AppUtils().isValidUserName(binding.etUserName.text.toString())) {
            this.toast("Please enter a valid Username")
            return false
        }
        if (binding.etEmail.text.isNullOrBlank()) {
            this.toast("Please enter email id")
            return false
        } else if (!AppUtils().isValidEmail(binding.etEmail.text.toString())) {
            this.toast("Please enter a valid email id")
            return false
        } else if (binding.etPassword.text.isNullOrBlank()) {
            this.toast("Please enter password")
            return false
        } else if (binding.etPassword.text.length < 6) {
            this.toast("Password should be at least 6 characters long")
            return false
        } else if (binding.etConfirmPassword.text.isNullOrBlank()) {
            this.toast("Please confirm your password")
            return false
        } else if (binding.etPassword.text.toString()
                .trim() != binding.etConfirmPassword.text.toString().trim()
        ) {
            this.toast("Passwords do not match")
            return false
        } else
            return true

    }
}