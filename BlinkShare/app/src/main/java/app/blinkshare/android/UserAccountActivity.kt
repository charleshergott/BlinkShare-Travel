package app.blinkshare.android

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import app.blinkshare.android.databinding.ActivityUserAccountBinding
import app.blinkshare.android.utills.AppUtils
import app.blinkshare.android.utills.isNetworkAvailable
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UserAccountActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserAccountBinding
    val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"
    private var mCustomTabsServiceConnection: CustomTabsServiceConnection? = null
    private var mClient: CustomTabsClient? = null
    private var mCustomTabsSession: CustomTabsSession? = null
    private lateinit var storageRef: StorageReference
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserAccountBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        setContentView(binding.root)
        initCustomTab()
        initListeners()
        if(isNetworkAvailable()) {
            getAvatar()
        }
        else{
            hideShowLoading(false)
            val dialog = NetworkPopUp()
            dialog.show(supportFragmentManager, "NetworkPopUp")
        }
    }

    private fun getAvatar(){
        hideShowLoading(true)
        val imagesRef = storageRef.child("Avatars/${auth.currentUser?.uid}.jpg")
        imagesRef.downloadUrl.addOnCompleteListener { task ->
            if(task.isSuccessful){
                hideShowLoading(false)
                val downloadUri = task.result
                Glide.with(applicationContext).load(downloadUri).into(binding.imgProfilePic)
            }
        }.addOnFailureListener {
            hideShowLoading(false)
        }
    }

    private fun initListeners(){
        binding.llAccountSettings.setOnClickListener {
            startActivity(Intent(this@UserAccountActivity, AccountSettingsActivity::class.java))
        }
        binding.llMyProducts.setOnClickListener {
            val sIntent = Intent(this@UserAccountActivity, ViewProductActivity::class.java)
            sIntent.putExtra("isFromProfile",true)
            startActivity(sIntent)
        }

        binding.btnLogout.setOnClickListener {
            val builder = AlertDialog.Builder(this@UserAccountActivity)
            builder.setTitle("BlinkShare")
            builder.setMessage("Are you sure you want to logout?")
            builder.setPositiveButton("Yes") { dialog, which ->
                FirebaseAuth.getInstance().signOut()
                AppUtils().logoutUser(this@UserAccountActivity)
                this.setOnBoarding(true)
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            builder.setNegativeButton("No") { dialog, which ->
            }
            builder.show()
        }
        binding.tvFaq.setOnClickListener {
            val customTabsIntent = CustomTabsIntent.Builder(mCustomTabsSession)
                .setToolbarColor(resources.getColor(R.color.colorBg))
                .setShowTitle(true)
                .build()

            customTabsIntent.launchUrl(this, Uri.parse("https://blinkshare.app/faq"))
        }
        binding.tvTermsConditions.setOnClickListener {
            val customTabsIntent = CustomTabsIntent.Builder(mCustomTabsSession)
                .setToolbarColor(resources.getColor(R.color.colorBg))
                .setShowTitle(true)
                .build()

            customTabsIntent.launchUrl(
                this,
                Uri.parse("https://blinkshare.app/terms-and-conditions-1")
            )
        }
        binding.tvPrivacyPolicy.setOnClickListener {
            val customTabsIntent = CustomTabsIntent.Builder(mCustomTabsSession)
                .setToolbarColor(resources.getColor(R.color.colorBg))
                .setShowTitle(true)
                .build()

            customTabsIntent.launchUrl(
                this,
                Uri.parse("https://blinkshare.app/privacy-policy")
            )
        }
        binding.tvContacts.setOnClickListener {
            val customTabsIntent = CustomTabsIntent.Builder(mCustomTabsSession)
                .setToolbarColor(resources.getColor(R.color.colorBg))
                .setShowTitle(true)
                .build()

            customTabsIntent.launchUrl(
                this,
                Uri.parse("https://blinkshare.app/invest")
            )
        }
        binding.tvShareWithFriends.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "http://www.blinkshare.app");
            startActivity(Intent.createChooser(shareIntent, "Select an app to share"))
        }
        binding.imgBackBtn.setOnClickListener {
            onBackPressed()
        }
    }
    private fun initCustomTab() {
        mCustomTabsServiceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                componentName: ComponentName,
                customTabsClient: CustomTabsClient
            ) {
                //Pre-warming
                mClient = customTabsClient
                mClient?.warmup(0L)
                mCustomTabsSession = mClient?.newSession(null)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                mClient = null
            }
        }

        CustomTabsClient.bindCustomTabsService(
            this, CUSTOM_TAB_PACKAGE_NAME,
            mCustomTabsServiceConnection as CustomTabsServiceConnection
        )
    }

    override fun onResume() {
        super.onResume()
        if(isNetworkAvailable()) {
            getAvatar()
        }
        else{
            hideShowLoading(false)
            val dialog = NetworkPopUp()
            dialog.show(supportFragmentManager, "NetworkPopUp")
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
}