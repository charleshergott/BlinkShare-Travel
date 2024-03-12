package app.blinkshare.android

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import app.blinkshare.android.databinding.ActivityMainBinding
import app.blinkshare.android.model.Product
import app.blinkshare.android.utills.AppUtils
import app.blinkshare.android.utills.isNetworkAvailable
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityMainBinding
    private val blockList = HashMap<String, String>()
    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap
    var isRefresh: Boolean = false

    //    private var COORDINATE_OFFSET: Double = 0.0000445
    private var COORDINATE_OFFSET: Double = 0.00002
    private val markerCoordinates: ArrayList<LatLng> = ArrayList()

    private var currLocation: Location? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var REQUEST_CHECK_SETTINGS: Int = 101
    private lateinit var locationCallback: LocationCallback
    private val list: ArrayList<Product> = ArrayList()
    private var mCustomTabsSession: CustomTabsSession? = null

    private var topLeft: LatLng? = null
    private var topRight: LatLng? = null
    private var bottomLeft: LatLng? = null
    private var bottomRight: LatLng? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //try {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val isFromLogin = intent.getBooleanExtra("isFromLogin", false)
        if (this.getOnBoarding() && isFromLogin) {
            showOnBoarding()
        }
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        updateToken()
        //addUser()
        mapFragment = (supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this@MainActivity)
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this@MainActivity)
        initListeners()
        showLocationAccessPopup()
        if (isNetworkAvailable()) {
            getBlockedList()
        } else {
            hideShowLoading(false)
            val dialog = NetworkPopUp()
            dialog.show(supportFragmentManager, "NetworkPopUp")
        }
        if (intent != null) {
            if (intent.hasExtra("product_id")) {
                val intent_ = Intent(applicationContext, ViewProductActivity::class.java)
                intent_.putExtra("product_id", intent.getStringExtra("product_id"))
                intent_.putExtra("isFromProfile", false)
                startActivity(intent_)
            }
        }
//        } catch (ex: Exception) {
//            Toast.makeText(applicationContext, ex.message.toString(), Toast.LENGTH_LONG).show()
//        }
    }

    private fun updateToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("updateToken Home", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            try {
                val dataToken = hashMapOf(
                    "token" to token,
                )
                db.collection("Tokens").document(auth.currentUser?.uid!!)
                    .set(dataToken)
            }catch (ex: Exception){

            }
        })
    }

    private fun initListeners() {
        binding.imgUserAccount.setOnClickListener {
            if (AppUtils().isUserLoggedIn(applicationContext))
                startActivity(Intent(this@MainActivity, UserAccountActivity::class.java))
            else
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }
        binding.imgCamera.setOnClickListener {
            if (AppUtils().isUserLoggedIn(applicationContext)) {
                if (applicationContext.getIsAdmin()) {
                    val intent_ = Intent(applicationContext, AddProductActivity::class.java)
                    startActivity(intent_)
                } else {
                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))
                }
            } else
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }
        binding.imgRefresh.setOnClickListener {
            //isRefresh = true
            //selectedRadius = 0.5
            if (isNetworkAvailable()) {
                getBlockedList()
            } else {
                hideShowLoading(false)
                val dialog = NetworkPopUp()
                dialog.show(supportFragmentManager, "NetworkPopUp")
            }
        }
        binding.imgInfoTutorial.setOnClickListener {
//            val customTabsIntent = CustomTabsIntent.Builder(mCustomTabsSession)
//                .setToolbarColor(resources.getColor(R.color.colorBg))
//                .setShowTitle(true)
//                .build()
//
//            customTabsIntent.launchUrl(this, Uri.parse("https://youtu.be/mo4jNq18Pg0"))
            showOnBoarding()
        }
    }

    fun showLocationAccessPopup() {

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user
                val dialogBuilder = AlertDialog.Builder(applicationContext)
                dialogBuilder.setMessage("App needs location permission to associate location with your product. Do you want to grant location permission")
                    ?.setCancelable(false)
                    ?.setPositiveButton(
                        "Yes, Grant permission",
                        DialogInterface.OnClickListener { dialog, id ->
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                100
                            )
                        })
                    // negative button text and action
                    ?.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        currLocation = null
                    })
                val alert = dialogBuilder.create()
                alert.setTitle("Location Permission")
                alert.show()

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    100
                )
            }
        } else {
            checkLocationSettings()
        }
    }

    private fun checkLocationSettings() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder =
            locationRequest?.let { LocationSettingsRequest.Builder().addLocationRequest(it) }
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder?.build()!!)

        task.addOnSuccessListener { locationSettingsResponse ->
            getLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        this,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }
        val locationRequest = LocationRequest.create()?.apply {
            interval = 5000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations != null && locationResult.locations.size > 0) {
                    val location: Location = locationResult.locations[0]
                    if (location != null) {
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                        currLocation = location

                        Handler(Looper.getMainLooper()).postDelayed({
                            googleMap.moveCamera(
                                CameraUpdateFactory.newLatLng(
                                    LatLng(
                                        currLocation!!.latitude, currLocation!!.longitude
                                    )
                                )
//                                        CameraUpdateFactory.newLatLngZoom(
//                                        LatLng(
//                                            currLocation!!.latitude, currLocation!!.longitude
//                                        ), 16.0f
//                            )
                            )
                        }, 1000)

                    }
                } else {
                    currLocation = null
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest!!,
            locationCallback,
            Looper.getMainLooper()
        )
            .addOnFailureListener {
                binding.rlProgressLoading.visibility = View.GONE
                Toast.makeText(
                    applicationContext,
                    "Failed to get your location. Please check location services/internet and try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted.
                    checkLocationSettings()
                } else {
                    //viewModel.setCurrentLocation(Location(""))
                    currLocation = null
                    Toast.makeText(
                        applicationContext,
                        "Location permission denied. Please grant permission from Application info settings.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        val intent_ = Intent(applicationContext, ViewProductActivity::class.java)
        intent_.putExtra("latitude", p0.position.latitude.toString())
        intent_.putExtra("longitude", p0.position.longitude.toString())
        intent_.putExtra("isFromProfile", false)
        startActivity(intent_)
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.style_json
                )
            )

        } catch (e: Exception) {

        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }

        googleMap.uiSettings.isZoomControlsEnabled = true
        this.googleMap.setOnMarkerClickListener(this)


        this.googleMap.setOnCameraIdleListener {
            //val midLatLng: LatLng = this.googleMap.cameraPosition.target//map's center position latitude & longitude
            val visibleRegion: VisibleRegion = this.googleMap.getProjection().getVisibleRegion()
            topLeft = visibleRegion.farLeft
            topRight = visibleRegion.farRight
            bottomLeft = visibleRegion.nearLeft
            bottomRight = visibleRegion.nearRight
            Log.e("farLeft->", topLeft?.latitude.toString() + ", " + topLeft?.longitude.toString())
            Log.e(
                "farRight->",
                topRight?.latitude.toString() + ", " + topRight?.longitude.toString()
            )
            Log.e(
                "nearLeft->",
                bottomLeft?.latitude.toString() + ", " + bottomLeft?.longitude.toString()
            )
            Log.e(
                "nearRight->",
                bottomRight?.latitude.toString() + ", " + bottomRight?.longitude.toString()
            )
        }
    }

    private fun setMapData(product: Product) {

        var index: Int = 0

        if (product.latitude.contains(",")) {
            product.latitude = product.latitude.replace(",", ".")
        }
        if (product.longitude.contains(",")) {
            product.longitude = product.longitude.replace(",", ".")
        }
        //updateLatLang(product, product.latitude.toDouble(), product.longitude.toDouble())
        var markerOption = MarkerOptions()
            .position(
                getLatLng(
                    LatLng(
                        product.latitude.toDouble(),
                        product.longitude.toDouble()
                    )
                )
            )
            .title(product.description)
        // marker.icon = BitmapDescriptorFactory.fromResource(R.drawable.home)
//        ContextCompat.getDrawable(applicationContext, R.drawable.airplane)?.toBitmap()?.copy(Bitmap.Config.ARGB_8888,true)


        if (product.is_object) {
            if (product.is_flight) {
                try {
                    markerOption.icon(
                        BitmapDescriptorFactory.fromBitmap(
                            ContextCompat.getDrawable(
                                applicationContext,
                                R.drawable.airplane
                            )?.toBitmap()?.copy(Bitmap.Config.ARGB_8888, true)!!
                        )
                    )
                } catch (ex: Exception) {
                    markerOption.icon(
                        BitmapDescriptorFactory.fromResource(
                            R.drawable.airplane
                        )
                    )
                }
            } else {
                try {
                    markerOption.icon(
                        BitmapDescriptorFactory.fromBitmap(
                            ContextCompat.getDrawable(
                                applicationContext,
                                R.drawable.home
                            )?.toBitmap()?.copy(Bitmap.Config.ARGB_8888, true)!!
                        )
                    )
                } catch (ex: Exception) {
                    markerOption.icon(
                        BitmapDescriptorFactory.fromResource(
                            R.drawable.home
                        )
                    )

                }
//                            BitmapDescriptorFactory.fromResource(
//                        R.drawable.home
//                    )
            }
        } else {
            try {
                markerOption.icon(
                    BitmapDescriptorFactory.fromBitmap(
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.camera_icon_map
                        )?.toBitmap()?.copy(Bitmap.Config.ARGB_8888, true)!!
                    )
                )
            } catch (ex: Exception) {
                markerOption.icon(
                    BitmapDescriptorFactory.fromResource(
                        R.drawable.camera_icon_map
                    )
                )

            }
        }

        val marker = googleMap.addMarker(markerOption)
//        marker?.tag = index
//        index += 1

        if (isRefresh && currLocation != null) {
            isRefresh = false
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(
                        currLocation!!.latitude, currLocation!!.longitude
                    )
                )
//                        CameraUpdateFactory.newLatLngZoom(
//                    LatLng(
//                        currLocation!!.latitude, currLocation!!.longitude
//                    ), 16.0f
//                )
            )
        }


    }

    private val TOP_LEFT = 1
    private val BOTTOM_LEFT = 2
    private val TOP_RIGHT = 3
    private val BOTTOM_RIGHT = 4
    private val BOTTOM = 5
    private val TOP = 6
    private val LEFT = 7
    private val RIGHT = 8
    private fun updateLatLang(
        product: Product,
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        type: Int = -1
    ) {
        val data =
            list.filter { item -> item.latitude.toDouble() == latitude && item.longitude.toDouble() == longitude }
        if (data.isNullOrEmpty()) {
            if (type != -1) {
                product.latitude = latitude.toString()
                product.longitude = longitude.toString()
                updateLatLang(product)
            }
            return
        } else {
            val lat = product.latitude.toDouble()
            val lang = product.longitude.toDouble()
            when (type) {
                -1 -> {
                    updateLatLang(product, lat - COORDINATE_OFFSET, lang, LEFT)
                }
                LEFT -> {
                    updateLatLang(product, lat + COORDINATE_OFFSET, lang, RIGHT)
                }
                RIGHT -> {
                    updateLatLang(product, lat, lang + COORDINATE_OFFSET, TOP)
                }
                TOP -> {
                    updateLatLang(product, lat, lang - COORDINATE_OFFSET, BOTTOM)
                }
                BOTTOM -> {
                    updateLatLang(
                        product,
                        lat - COORDINATE_OFFSET,
                        lang + COORDINATE_OFFSET,
                        TOP_LEFT
                    )
                }
                TOP_LEFT -> {
                    updateLatLang(
                        product,
                        lat + COORDINATE_OFFSET,
                        lang + COORDINATE_OFFSET,
                        TOP_RIGHT
                    )
                }
                TOP_RIGHT -> {
                    updateLatLang(
                        product,
                        lat - COORDINATE_OFFSET,
                        lang - COORDINATE_OFFSET,
                        BOTTOM_LEFT
                    )
                }
                BOTTOM_LEFT -> {
                    updateLatLang(
                        product,
                        lat + COORDINATE_OFFSET,
                        lang - COORDINATE_OFFSET,
                        BOTTOM_RIGHT
                    )
                }
                BOTTOM_RIGHT -> {
                    COORDINATE_OFFSET = COORDINATE_OFFSET + 0.00002
                    updateLatLang(product, lat - COORDINATE_OFFSET, lang, LEFT)
                }
            }
        }
    }

    private fun getBlockedList() {
        if (auth.currentUser?.uid.isNullOrEmpty()) {
            getProduct()
            return
        }
        db.collection("ReportedProducts").document(auth.currentUser?.uid.toString())
            .collection("products")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (snap in it.result) {
                        blockList.put(snap.id, snap.id)
                    }
                    getProduct()
                } else {
                    getProduct()
                }
            }
            .addOnFailureListener {
                getProduct()
            }
    }

    private fun getProduct() {
        hideShowLoading(true)
        val cal = Calendar.getInstance()
        val time = cal.timeInMillis
        Log.e("time->", time.toString())
        db.collection("Products").whereGreaterThanOrEqualTo("createdDateTime", time).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    googleMap.clear()
                    for (snapShot in it.result) {
                        try {
                            COORDINATE_OFFSET = 0.00002
                            val product = snapShot.toObject(Product::class.java)
                            if (blockList.containsKey(product.id)) {
                                continue
                            }
                            setMapData(product)
                            //list.add(product)
                        } catch (e: Exception) {

                        }

                    }
                    hideShowLoading(false)
                }
            }.addOnFailureListener {
                binding.rlProgressLoading.visibility = View.GONE
            }
    }

    private fun getLatLng(latLng: LatLng): LatLng {
        val updatedLatLng: LatLng
        if (markerCoordinates.contains(latLng)) {
            return latLng
        } else {
            markerCoordinates.add(latLng)
            updatedLatLng = latLng
        }
        return updatedLatLng
    }

    private fun showOnBoarding() {
        val dialog = OnBoardingDialog()
        dialog.show(supportFragmentManager, "OnBoardingDialog")
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

    override fun onResume() {
        super.onResume()
        if (isNetworkAvailable()) {
            getBlockedList()
        } else {
            hideShowLoading(false)
            val dialog = NetworkPopUp()
            dialog.show(supportFragmentManager, "NetworkPopUp")
        }
    }
}