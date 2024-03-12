package app.blinkshare.android

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.blinkshare.android.databinding.ActivityPasswordResetBinding
import app.blinkshare.android.utills.AppUtils
import app.blinkshare.android.utills.isNetworkAvailable
import com.google.firebase.auth.FirebaseAuth


class PasswordResetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordResetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordResetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()
    }

    private fun initListeners(){
        binding.imgBackBtn.setOnClickListener {
            onBackPressed()
        }
        binding.btnSend.setOnClickListener {
            if(validate()) {
                if(!isNetworkAvailable()){
                    hideShowLoading(false)
                    val dialog = NetworkPopUp()
                    dialog.show(supportFragmentManager, "NetworkPopUp")
                    return@setOnClickListener
                }
                hideShowLoading(true)
                FirebaseAuth.getInstance().sendPasswordResetEmail(binding.etEmail.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            hideShowLoading(false)
                            Toast.makeText(applicationContext, "Email sent.", Toast.LENGTH_LONG)
                                .show()
                        }
                    }.addOnFailureListener {
                        hideShowLoading(false)
                    }
            }
        }
    }

    private fun validate(): Boolean {
        return if (binding.etEmail.text.trim().isNullOrBlank()) {
            Toast.makeText(applicationContext,"Please enter email id", Toast.LENGTH_LONG).show()
            false
        } else if (!AppUtils().isValidEmail(binding.etEmail.text.toString().trim())) {
            Toast.makeText(applicationContext,"Please enter a valid email id", Toast.LENGTH_LONG).show()
            false
        } else
            true
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