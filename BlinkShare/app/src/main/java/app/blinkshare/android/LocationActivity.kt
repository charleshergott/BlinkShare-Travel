package app.blinkshare.android

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import app.blinkshare.android.databinding.ActivityLocationBinding
import app.blinkshare.android.model.Product
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.VisibleRegion
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private val OFFSET = 0.0000267
    private lateinit var binding: ActivityLocationBinding
    lateinit var googleMap: GoogleMap
    lateinit var mapFragment: SupportMapFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var REQUEST_CHECK_SETTINGS: Int = 101
    private var currLocation: Location? = null
    private var myLat: Double? = null
    private var myLang: Double? = null

    private var topLeft: LatLng? = null
    private var topRight: LatLng? = null
    private var bottomLeft: LatLng? = null
    private var bottomRight: LatLng? = null

    private val list: ArrayList<Product> = ArrayList()
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var locationCallback: LocationCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        mapFragment = (supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this@LocationActivity)
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this@LocationActivity)
        showLocationAccessPopup()
        binding.btnDone.setOnClickListener {
            if (currLocation != null) {
                updateLatLangUpload(myLat!!, myLang!!, myLat!!, myLang!!, -1, OFFSET)
                val sIntent = Intent()
                sIntent.putExtra("lat", myLat)
                sIntent.putExtra("lang", myLang)
                setResult(RESULT_OK, sIntent)
                finish()
            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        this.googleMap = p0
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setOnMapClickListener { point ->
            myLat = point.latitude
            myLang = point.longitude
            googleMap.clear()
            val markerOption = MarkerOptions()
                .position(
                    point
                )
                .title("")
            googleMap.addMarker(markerOption)
        }
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
            getProduct()
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
                        myLat = currLocation?.latitude
                        myLang = currLocation?.longitude
                        Handler(Looper.getMainLooper()).postDelayed({
                            googleMap.moveCamera(
                                CameraUpdateFactory.newLatLng(LatLng(
                                    currLocation!!.latitude, currLocation!!.longitude
                                ))
//                                CameraUpdateFactory.newLatLngZoom(
//                                    LatLng(
//                                        currLocation!!.latitude, currLocation!!.longitude
//                                    ), 16.0f
//                                )
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

    private fun getProduct() {
        hideShowLoading(true)
        val cal = Calendar.getInstance()
        val time = cal.timeInMillis
        Log.e("time->", time.toString())
        db.collection("Products")
            .whereGreaterThanOrEqualTo("latitude", bottomLeft?.latitude.toString())
            .whereLessThanOrEqualTo("latitude", topLeft?.latitude.toString()).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    googleMap.clear()
                    list.clear()
                    for (snapShot in it.result) {
                        try {
                            val product = snapShot.toObject(Product::class.java)
                            if (product.createdDateTime >= time) {
                                list.add(product)
                            }
                        } catch (e: Exception) {

                        }

                    }
                    hideShowLoading(false)
                }
            }.addOnFailureListener {
                hideShowLoading(false)
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

    private val TOP_LEFT = 1
    private val BOTTOM_LEFT = 2
    private val TOP_RIGHT = 3
    private val BOTTOM_RIGHT = 4
    private val BOTTOM = 5
    private val TOP = 6
    private val LEFT = 7
    private val RIGHT = 8

    private fun updateLatLangUpload(
        lat: Double = 0.0,
        lang: Double = 0.0,
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        type: Int = -1,
        offset: Double = OFFSET
    ) {
        var isOverlap = false
        for (item in list){
            if(latitude >= item.latitude.toDouble() - OFFSET && latitude <= item.latitude.toDouble() + OFFSET && longitude >=  item.longitude.toDouble() - OFFSET && longitude <=  item.longitude.toDouble() + OFFSET ){
                isOverlap = true
                break
            }
        }
        if(!isOverlap){
            myLat = latitude
            myLang = longitude
            return
        }
        when(type){
            -1 ->{ updateLatLangUpload(lat, lang, lat+offset, longitude, LEFT, offset)}
            LEFT -> {updateLatLangUpload(lat, lang, lat-offset, longitude, RIGHT, offset)}
            RIGHT -> {updateLatLangUpload(lat, lang, lat, lang+offset, TOP, offset)}
            TOP -> {updateLatLangUpload(lat, lang, lat, lang-offset, BOTTOM, offset)}
            BOTTOM -> {updateLatLangUpload(lat, lang, lat+offset+offset, longitude, LEFT, offset+OFFSET)}
        }
    }
}