package app.blinkshare.android

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.blinkshare.android.adapters.ProductAdapter
import app.blinkshare.android.databinding.ActivityViewProductBinding
import app.blinkshare.android.model.Product
import app.blinkshare.android.model.User
import app.blinkshare.android.notification.*
import app.blinkshare.android.utills.isNetworkAvailable
import app.blinkshare.android.utills.toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class ViewProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewProductBinding
    private var list = ArrayList<Product>()
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private var lat = ""
    private var lang = ""
    private var isFromProfile = false
    private var productId = ""
    private var apiService: APIService? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        apiService = Client.getClient("https://fcm.googleapis.com")?.create(APIService::class.java)
        lat = intent.getStringExtra("latitude") ?: ""
        lang = intent.getStringExtra("longitude") ?: ""
        productId = intent.getStringExtra("product_id") ?: ""
        isFromProfile = intent.getBooleanExtra("isFromProfile", false)
        initAdapter()
        if (isNetworkAvailable()) {
            getProducts()
        } else {
            hideShowLoading(false)
            val dialog = NetworkPopUp()
            dialog.show(supportFragmentManager, "NetworkPopUp")
        }
        initListeners()
    }

    private fun initListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun initAdapter() {
        binding.rv.adapter = ProductAdapter(
            list,
            auth.currentUser?.uid ?: "",
            object : ProductAdapter.OnItemClickListeners {
                @SuppressLint("NotifyDataSetChanged")
                override fun onLikeClick(position: Int) {
                    if (list[position].is_like) {
                        return
                    }
                    if (!isNetworkAvailable()) {
                        hideShowLoading(false)
                        val dialog = NetworkPopUp()
                        dialog.show(supportFragmentManager, "NetworkPopUp")
                        return
                    }
                    val map = hashMapOf(
                        auth.currentUser?.uid to 1
                    )
                    db.collection("LikeDislike").document(list[position].id)
                        .set(map)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val cal = Calendar.getInstance()
                                val date = Date(list[position].createdDateTime)
                                cal.time = date
                                //cal.add(Calendar.DAY_OF_MONTH, 1)
                                if (list[position].is_dis_like) {
                                    cal.add(Calendar.HOUR_OF_DAY, 2)
                                } else {
                                    cal.add(Calendar.HOUR_OF_DAY, 1)
                                }
                                val time = cal.timeInMillis
                                db.collection("Products").document(list[position].id)
                                    .update("createdDateTime", time).addOnCompleteListener {
                                        list[position].createdDateTime = time
                                        list[position].is_like = true
                                        list[position].is_dis_like = false
                                        binding.rv.adapter?.notifyDataSetChanged()
                                    }
                            }
                        }
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onDisLikeClick(position: Int) {
                    if (list[position].is_dis_like) {
                        return
                    }
                    if (!isNetworkAvailable()) {
                        hideShowLoading(false)
                        val dialog = NetworkPopUp()
                        dialog.show(supportFragmentManager, "NetworkPopUp")
                        return
                    }
                    val map = hashMapOf(
                        auth.currentUser?.uid to 0
                    )
                    db.collection("LikeDislike").document(list[position].id)
                        .set(map)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val cal = Calendar.getInstance()
                                val date = Date(list[position].createdDateTime)
                                cal.time = date
                                //cal.add(Calendar.DAY_OF_MONTH, -1)
                                if (list[position].is_like) {
                                    cal.add(Calendar.HOUR_OF_DAY, -2)
                                } else {
                                    cal.add(Calendar.HOUR_OF_DAY, -1)
                                }

                                val time = cal.timeInMillis
                                db.collection("Products").document(list[position].id)
                                    .update("createdDateTime", time).addOnCompleteListener {
                                        list[position].createdDateTime = time
                                        list[position].is_dis_like = true
                                        list[position].is_like = false
                                        binding.rv.adapter?.notifyDataSetChanged()
                                    }
                            }
                        }
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onDeleteClick(position: Int) {
                    if (!isNetworkAvailable()) {
                        hideShowLoading(false)
                        val dialog = NetworkPopUp()
                        dialog.show(supportFragmentManager, "NetworkPopUp")
                        return
                    }
                    hideShowLoading(true)
                    db.collection("Products").document(list[position].id).delete()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                hideShowLoading(false)
                                list.removeAt(position)
                                binding.rv.adapter?.notifyDataSetChanged()
                                if (list.isNullOrEmpty()) {
                                    val sIntent =
                                        Intent(applicationContext, MainActivity::class.java)
                                    sIntent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(sIntent)
                                    finish()
                                }
                            }
                        }.addOnFailureListener {
                            hideShowLoading(false)
                        }
                }

                override fun onReportClick(position: Int) {
                    if (list.size > position) {
                        val dialog =
                            BlockProductFragment(object : BlockProductFragment.OnItemClickListener {
                                override fun onReportClick(text: String) {
                                    hideShowLoading(true)
                                    val map = hashMapOf(
                                        "id" to list[position].id,
                                        "why" to text,
                                        "date" to Calendar.getInstance().timeInMillis
                                    )
                                    db.collection("ReportedProducts")
                                        .document(auth.currentUser?.uid.toString())
                                        .collection("products")
                                        .document(list[position].id)
                                        .set(map)
                                        .addOnCompleteListener {
                                            hideShowLoading(false)
                                            if (it.isSuccessful) {
                                                this@ViewProductActivity.toast("Picture reported successfully.")
                                                finish()
                                            } else {
                                                this@ViewProductActivity.toast(it.exception?.message.toString())
                                            }
                                        }
                                        .addOnFailureListener {
                                            hideShowLoading(false)
                                            this@ViewProductActivity.toast(it.message.toString())
                                        }
                                }

                                override fun onCancelClick() {
                                }

                            })
                        dialog.show(supportFragmentManager, "BlockProductFragment")
                    }
                }

                override fun onEditClick(position: Int) {
                    val sIntent = Intent(applicationContext, AddProductActivity::class.java)
                    sIntent.putExtra("product", list[position])
                    startActivity(sIntent)
                }

                override fun onSubscribeClick(position: Int) {
                    hideShowLoading(true)

                    val map = hashMapOf(
                        "${list[position].id}" to if(list[position].isSubscribed){"0"}else{"1"},
                    )

                    db.collection("Subscriptions").document(auth.currentUser?.uid!!)
                        .set(map)
                        .addOnCompleteListener {
                            if(list[position].isSubscribed){
                                unSubscribe(list[position].id)
                            }
                            else {
                                subscribe(list[position].id, list[position].authToken)
                            }
                            list[position].isSubscribed = !list[position].isSubscribed
                        }
                        .addOnFailureListener {
                            hideShowLoading(false)
                            Toast.makeText(applicationContext,"Subscribe failed\n"+it.message.toString(),Toast.LENGTH_LONG).show()
                        }

                }

            })
    }

    private fun getProducts() {
        hideShowLoading(true)
        val ref = if (isFromProfile) {
            db.collection("Products").whereEqualTo("authToken", auth.currentUser?.uid ?: "")
        } else {
            if(!productId.isNullOrEmpty()){
                db.collection("Products").whereEqualTo("id", productId)
            }else {
                db.collection("Products").whereEqualTo("latitude", lat).whereEqualTo("longitude", lang)
            }
//            db.collection("Products").whereEqualTo("latitude", lat).whereEqualTo("longitude", lang)
        }
        ref.get().addOnCompleteListener {
            if (it.isSuccessful) {
                hideShowLoading(false)
                var isProfileLoaded = false
                for (snapShot in it.result) {
                    val product = snapShot.toObject(Product::class.java)
                    if (!isProfileLoaded) {
                        try {
                            getProfile(product.authToken)
                        } catch (ex: Exception) {

                        }
                    }
                    isProfileLoaded = true
                    product.image = "Products/${product.authToken}/${snapShot.id}.jpg"
                    list.add(product)
                    try {
                        if (auth.currentUser?.uid ?: "" != "") {
                            db.collection("Subscriptions").document(auth.currentUser?.uid!!)
                                .get().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        if (task.result.exists() && task.result.contains(
                                                product.id
                                            )
                                        ) {
                                            if (task.result.get(product.id)
                                                    .toString() == "1"
                                            ) {
                                                product.isSubscribed = true
                                                binding.rv.adapter?.notifyDataSetChanged()
                                            } else {
                                                product.isSubscribed = false
                                                binding.rv.adapter?.notifyDataSetChanged()
                                            }

                                        }else{
                                            product.isSubscribed = false
                                            binding.rv.adapter?.notifyDataSetChanged()
                                        }

                                    }
                                }
                            db.collection("LikeDislike").document(product.id)
                                .get().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        if (task.result.exists() && task.result.contains(
                                                auth.currentUser?.uid ?: ""
                                            )
                                        ) {
                                            if (task.result.get(auth.currentUser?.uid ?: "")
                                                    .toString() == "1"
                                            ) {
                                                product.is_like = true
                                                product.is_dis_like = false
                                                binding.rv.adapter?.notifyDataSetChanged()
                                            } else {
                                                product.is_like = false
                                                product.is_dis_like = true
                                                binding.rv.adapter?.notifyDataSetChanged()
                                            }
                                        }

                                    }
                                }
                        }
                    } catch (ex: Exception) {

                    }
                }
                list.sortByDescending { item -> item.createdDateTime }
                binding.rv.adapter?.notifyDataSetChanged()
                binding.rlProgressLoading.visibility = View.GONE
            }
        }.addOnFailureListener {
            hideShowLoading(false)
            binding.rlProgressLoading.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getProfile(uid: String) {
        storageRef.child("Avatars/$uid.jpg").downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                Glide.with(applicationContext).load(downloadUri).into(binding.ivProfile)
            }
        }
        db.collection("Users").document(uid).get().addOnCompleteListener {
            try {
                val user = it.result.toObject(User::class.java)
                //binding.tvName.text = "${user?.firstName?:""} ${user?.lastName?:""}"
                binding.tvName.text = user?.userName ?: ""
            } catch (ex: Exception) {

            }
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

    private fun subscribe(topic: String, uid: String) {

        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                hideShowLoading(false)
                binding.rv.adapter?.notifyDataSetChanged()
                var msg = "Subscribed"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Toast.makeText(this@ViewProductActivity, msg, Toast.LENGTH_SHORT).show()
                db.collection("Tokens").document(uid).get()
                    .addOnCompleteListener {
                        if(it.isSuccessful && it.result.exists()){
                            try {
                                val token = it.result.get("token").toString()
                                val body = "Some one subscribed your aircraft"
                                val data = Data(topic, R.drawable.logo_notification_small, body, "")

                                val sender = Sender(data, token)

                                apiService!!.sendNotification(sender)
                                    ?.enqueue(object : Callback<MyResponse?> {
                                        override fun onResponse(
                                            call: Call<MyResponse?>,
                                            response: Response<MyResponse?>
                                        ) {
                                            if (response.code() == 200) {
                                                if (response.body()!!.success !== 1) {
                                                    //error
                                                    Log.e("sendNotification->", response.message().toString())

                                                } else {
                                                    Log.e("sendNotification->", response.message().toString())

                                                }
                                            }
                                        }

                                        override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                                            Log.e("sendNotification->", t.message.toString())
                                        }

                                    })
                            }catch (ex: Exception){

                            }

                        }
                    }
                    .addOnFailureListener {

                    }
            }
            .addOnFailureListener {
                hideShowLoading(false)
                Toast.makeText(this@ViewProductActivity, it.message.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun unSubscribe(topic: String) {

        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                hideShowLoading(false)
                binding.rv.adapter?.notifyDataSetChanged()
                var msg = "UnSubscribed"
                if (!task.isSuccessful) {
                    msg = "UnSubscribe failed"
                }
                Toast.makeText(this@ViewProductActivity, msg, Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {
                hideShowLoading(false)
                Toast.makeText(this@ViewProductActivity, it.message.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
    }
}