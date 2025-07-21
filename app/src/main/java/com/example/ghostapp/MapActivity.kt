package com.example.ghostapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ghostapp.fragments.HeaderDefaultFragment
import com.example.ghostapp.services.MapServices
import com.example.ghostapp.services.ReportServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import kotlin.concurrent.thread
import androidx.core.graphics.createBitmap

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val tag = "MapActivity"

    lateinit var mapActivityUserProfile: ImageView
    lateinit var mapActivityAddReport: ImageView
    lateinit var mapActivityMapButton: ImageView
    lateinit var mapActivityReportButton: ImageView
    lateinit var mapActivityBestiaryButton: ImageView

    var reportServices = ReportServices()
    var mapServices = MapServices()

    private var userLatLng: LatLng = LatLng(46.603354, 1.888334)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_activity_map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val headerDefaultFragment = supportFragmentManager.findFragmentById(R.id.header_container) as? HeaderDefaultFragment
        headerDefaultFragment?.setHeaderTitle(getString(R.string.map_activity_title))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mapActivityAddReport = findViewById(R.id.map_activity_add_report)
        mapActivityAddReport.setOnClickListener {
            val intent = Intent(this, AddReportActivity::class.java)
            startActivity(intent)
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapServices.getUserLocationWithoutMap(this, this) { latLng ->
            if (latLng != null) {
                userLatLng = latLng
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
            }else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(48.8566, 2.3522), 10f))
            }
        }
        try {
            if (mapServices.hasLocationPermission(this)) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permissions de localisation non accordées", Toast.LENGTH_SHORT).show()
        }

        val userToken = getSharedPreferences("save", MODE_PRIVATE).getString("token", null)
        thread {
            reportServices.getReportAddress(
                JSONObject(userToken.toString()).getString("session"),
                onSuccess = { responseBody: String? ->
                    runOnUiThread {
                        addMarkersForAddresses(JSONArray(responseBody.toString()))
                    }
                },
                onError = { errorMessage: String ->
                    runOnUiThread {
                        Log.e(tag, "Erreur lors de la récupération des adresses: $errorMessage")
                        Toast.makeText(this, "Erreur lors de la récupération des adresses", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    private fun addMarkersForAddresses(addressList: JSONArray) {
        val geocoder = Geocoder(this, Locale.getDefault())

        for (i in 0 until addressList.length()) {
            try {
                val jsonObj = addressList.getJSONObject(i)
                val address = jsonObj.getString("address")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocationName(address, 1) { addresses ->
                        runOnUiThread {
                            processGeocodingResult(addresses, address, jsonObj)
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(tag, "Géocodage impossible pour l'adresse", e)
            }
        }
    }

    private fun processGeocodingResult(addresses: List<Address>?, addressStr: String, jsonObj: JSONObject) {
        if (addresses.isNullOrEmpty()) {
            Log.e(tag, "Aucun résultat trouvé pour: $addressStr")
            return
        }

        val address = addresses[0]
        val latLng = LatLng(address.latitude, address.longitude)

        val markerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("${jsonObj.getString("type")}")
                .icon(markerIcon)
        )
    }

}