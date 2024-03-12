package app.blinkshare.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ListPopupWindow
import androidx.databinding.DataBindingUtil
import app.blinkshare.android.adapters.DropDownAdapter
import app.blinkshare.android.databinding.ActivityAccountSettingsBinding
import app.blinkshare.android.model.User
import app.blinkshare.android.utills.*
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class AccountSettingsActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityAccountSettingsBinding
    private lateinit var storageRef: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var file_profile_Pic: File? = null
    var user: User? = null
    private var topLeft: LatLng? = null
    private var topRight: LatLng? = null
    private var bottomLeft: LatLng? = null
    private var bottomRight: LatLng? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account_settings)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        user = AppUtils().getUser(this@AccountSettingsActivity)
        setUserDetails(user)
        if(isNetworkAvailable()) {
            getUser()
        }
        else{
            hideShowLoading(false)
            val dialog = NetworkPopUp()
            dialog.show(supportFragmentManager, "NetworkPopUp")
        }

        binding.imgEditPicture.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.imgBackBtn.setOnClickListener(this)

    }


    /*
   * Call API
   * */
    private fun callUpdateProfileAPI() {
        if (isNetworkAvailable()) {
            //viewModel.isNetWorkAvailable.value = true
            //hideKeyboard(binding.etTags)
            addUser()
        } else {
            hideShowLoading(false)
            this.toast("You seems to be offline. Please check internet connection")
            //viewModel.isNetWorkAvailable.value = false
        }
    }

    private fun getUser(){
        hideShowLoading(true)
        val imagesRef = storageRef.child("Avatars/${auth.currentUser?.uid}.jpg")
        imagesRef.downloadUrl.addOnCompleteListener { task ->
            if(task.isSuccessful){
                val downloadUri = task.result
                Glide.with(applicationContext).load(downloadUri).into(binding.imgProfilePic)
            }

        }
        binding.etEmail.setText(auth.currentUser?.email.toString())
        val docRef = db.collection("Users").document(auth.currentUser?.uid!!)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject(User::class.java)
            this.user = user
//            binding.etFirstName.setText(user?.firstName?:"")
//            binding.etLastName.setText(user?.lastName?:"")
            if(user?.userName!=null && !user.userName.isNullOrEmpty()){
                binding.etUsername.setText(user.userName)
                binding.etUsername.visibility = View.VISIBLE
                binding.tvUsername.visibility = View.VISIBLE
            }
            hideShowLoading(false)
        }.addOnFailureListener {
            hideShowLoading(false)
        }
    }

    private fun addUser() {
        hideShowLoading(true)
        if(file_profile_Pic != null){
            val imagesRef = storageRef.child("Avatars/${auth.currentUser?.uid}.jpg")
            val file = Uri.fromFile(file_profile_Pic)
            val uploadTask = imagesRef.putFile(file)

// Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener {
                hideShowLoading(false)
                // Handle unsuccessful uploads
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...
                imagesRef.downloadUrl.addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val downloadUri = task.result
                        if(binding.etUsername.text.toString().trim()!= user?.userName?:"") {
                            addUserData(downloadUri.path.toString())
                        }
                        else{
                            hideShowLoading(false)
                        }
                    }
                    else{
                        hideShowLoading(false)
                        this.toast(task.exception?.message.toString())
                    }
                }
            }
        }
        else{
            addUserData("")
        }
    }

    private fun addUserData(avatar: String = ""){
        //val tag = binding.etTags.text.toString().split("\\s".toRegex())
        //var tags = arrayListOf<String>()
        //tags.addAll(tag)
        val isAdmin = user?.isAdmin?:false
        val user_ = if(avatar.isNotEmpty()) {
            hashMapOf(
                //"age" to "",
                "authToken" to auth.currentUser?.uid,
                "avatar" to avatar,
                "currency" to "USD",
                //"firstName" to "",
                //"lastName" to "",
                "isNewUser" to true,
                "location" to null,
                "userName" to binding.etUsername.text.toString(),
                "email" to auth.currentUser?.email.toString(),
                "isAdmin" to true
            )
        }
        else{
            hashMapOf(
                //"age" to "",
                "authToken" to auth.currentUser?.uid,
                "currency" to "USD",
                //"firstName" to "",
                //"lastName" to "",
                "isNewUser" to false,
                "location" to null,
                "userName" to binding.etUsername.text.toString(),
                "email" to auth.currentUser?.email.toString(),
                "isAdmin" to isAdmin
            )
        }
        db.collection("Users").document(auth.currentUser?.uid!!)
            .set(user_)
            .addOnSuccessListener {
                hideShowLoading(false)
            }
            .addOnFailureListener { e ->
                hideShowLoading(false)
                this.toast(e.message.toString())
            }
    }

    private fun setUserDetails(userData: User?) {
        if (userData == null)
            return
//        binding.etFirstName.setText(userData.firstName)
//        binding.etLastName.setText(userData.lastName)
        binding.etUsername.setText(userData.userName)
        binding.etEmail.setText(userData.email)


        if (userData.avatar != null && userData.avatar.isNotEmpty())
            Glide.with(this@AccountSettingsActivity).load(userData.avatar)
                .into(binding.imgProfilePic)
        else
            Glide.with(this@AccountSettingsActivity).load(R.drawable.placeholder_profile_picture)
                .into(binding.imgProfilePic)


    }

    override fun onClick(view: View?) {
        if (view == binding.imgBackBtn) {
            finish()
        }
        else if (view == binding.imgEditPicture) {
            val options = arrayOf("Photo library", "Camera")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select option")
            builder.setItems(options) { _, which ->
                if (options[which] == "Camera") {
                    PermissionsUtil.askPermission(
                        this,
                        PermissionsUtil.STORAGE
                    ) { isGranted ->
                        if (isGranted) {
                            ImagePicker.with(this)
                                .compress(1024)
                                .cameraOnly()    //User can only capture image using Camera
                                .start()
                        }
                    }
                } else if (options[which] == "Photo library") {
                    PermissionsUtil.askPermission(
                        this,
                        PermissionsUtil.STORAGE
                    ) { isGranted ->
                        if (isGranted) {
                            ImagePicker.with(this)
                                .compress(1024)
                                .galleryOnly()    //User can only select image from Gallery
                                .start()
                        }
                    }
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            builder.show()
        } else if (view == binding.btnSave) {
//            callUpdateProfileAPI()
            if(isNetworkAvailable()) {
                if(binding.etUsername.text.toString().trim()== user?.userName?:"") {
                    callUpdateProfileAPI()
                }
                else{
                    isUserNameExist()
                }
            }
            else{
                hideShowLoading(false)
                val dialog = NetworkPopUp()
                dialog.show(supportFragmentManager, "NetworkPopUp")
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //You can get File object from intent
            val file: File? = ImagePicker.getFile(data)
            file_profile_Pic = file!!
            binding.imgProfilePic.setImageBitmap(
                BitmapFactory.decodeFile(
                    file_profile_Pic?.absolutePath
                )
            )
        } else if (resultCode == com.github.dhaval2404.imagepicker.ImagePicker.RESULT_ERROR) {
            //Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            // Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
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

    private fun isUserNameExist() {
        hideShowLoading(true)
        db.collection("Users").whereEqualTo("userName", binding.etUsername.text.toString())
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result.isEmpty) {
                        if(isNetworkAvailable()) {
                            callUpdateProfileAPI()
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
                    callUpdateProfileAPI()
                }
            }
            .addOnFailureListener {
                callUpdateProfileAPI()
            }
    }

}