package app.blinkshare.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import app.blinkshare.android.databinding.ActivityWelcomeEulaBinding
import app.blinkshare.android.utills.AppUtils

class WelcomeEulaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeEulaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeEulaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }
        }
        binding.webview.loadUrl("https://blinkshare.app/eula")
        initListeners()
    }

    private fun initListeners(){
        binding.btnContinue.setOnClickListener {
            val builder = AlertDialog.Builder(this@WelcomeEulaActivity)
            /* builder.setTitle("Success")*/
            builder.setMessage("Do you agree with EULA?")
            builder.setPositiveButton("I AGREE") { dialog, which ->
                dialog.dismiss()
                AppUtils().setEulaAgreed(this@WelcomeEulaActivity)
                startActivity(Intent(this, SignUpActivity::class.java))
                finish()
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                val builder = AlertDialog.Builder(this@WelcomeEulaActivity)
                builder.setMessage("We sorry you feel that way, goodbye")
                builder.setPositiveButton("Ok") { dialog, which ->
                    dialog.dismiss()
                    finish()
                }
                builder.show()
            }
            builder.show()
        }
    }
}