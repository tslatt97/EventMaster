package com.example.eventmaster

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.eventmaster.databinding.ActivityEventDetailsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_event_details.*


class EventDetailsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityEventDetailsBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationReqest: LocationRequest
    //Unik permission-id
    private var PERMISSION_ID = 1000
    //Variabler for å sammeligne brukerens posisjon med eventet sin addresse
    private val endPoint = Location(LocationManager.GPS_PROVIDER)
    private val startPoint = Location(LocationManager.NETWORK_PROVIDER)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_details)

        //Får tak i mapFragment slik at man kan opprette pins, lat/long, zoom osv
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // Henter informasjon som ble invoked i Dashboard-Classens invoke funksjon
        event_title.text = intent.getStringExtra("Event Title")
        event_details.text = intent.getStringExtra("Event Description")
        event_type.text = intent.getStringExtra("EventType")
        event_address.text = intent.getStringExtra("Event Address")
        dateText.text = intent.getStringExtra("Date")
        timeText.text = intent.getStringExtra("Time")

        startPoint.latitude = intent.getDoubleExtra("Lat", 0.0)
        startPoint.longitude = intent.getDoubleExtra("Long", 0.0)


        //Instansierer fusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()




        //Clicklisteners for bottom-navigation
        bottom_navigationD.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.ic_home -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                }
                R.id.ic_map -> {
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                }
                R.id.ic_group -> {
                    val intent = Intent(this, MyEventsActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }

    //Finner avstanden mellom Punkt A og B, brukerposisjon og eventposisjon
    private fun findDistance(start:Location, end:Location) {
        val distance: Float = start.distanceTo(end)
        //Gjør om resultatet fra meter til kilometer
        val ts: Double = (distance * 0.001)
        //Setter en grense på antall desimaler til 2, for å unngå veldig lange tall
        fun Double.round(decimals: Int = 0): Double = "%.${decimals}f".format(this).toDouble()
       compare.text = ts.round(0).toString() + " km"
        return
    }

    //Funksjon som sjekker bruker-tillatelser
    private fun checkPermissions(): Boolean {
       if(
           ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
           || ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
               ) {
           return true
       }
        return false
    }


    //Funksjon som ber om tillatelser
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_ID
        )
    }

    //Funksjon som ser om Location-service er aktiver
    private fun isLocationEnabled():Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    //Funksjon som henter ut siste posisjon
    private fun getLastLocation() {
        //Sjekker tillatelser
        if(checkPermissions()){
            //Sjekker om locationService er aktiver
            if(isLocationEnabled()){
                //Henter location
                fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    var location = task.result
                    if(location == null){
                        //Hvis location er tom hentes den nye bruker locationen
                        getNewLocation()
                    }else {
                        endPoint.latitude = location.latitude
                        endPoint.longitude = location.longitude
                        findDistance(startPoint,endPoint)
                    }
                }
            }else {
                Toast.makeText(this,"Please Enable your Location Service", Toast.LENGTH_LONG).show()
            }
        }else{
            requestPermissions()
        }
    }

    private fun getNewLocation() {
        locationReqest = LocationRequest()
        locationReqest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationReqest.interval = 0
        locationReqest.fastestInterval = 0
        locationReqest.numUpdates = 2
        fusedLocationClient!!.requestLocationUpdates(
            locationReqest,locationCallback, Looper.myLooper()
        )
       println("AAAAAAAAAAAAAAAAAAAAAAAA")
    }

    private val locationCallback = object: LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            val lastLocation = p0.lastLocation
            endPoint.latitude = lastLocation.latitude
            endPoint.longitude = lastLocation.longitude
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == PERMISSION_ID) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Debug", "You have the permission")
            }

        }
    }
    companion object {
        private const val TAG = "LocationProvider"
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //Henter latitude/longitude fra Intent som ble invoked i Dashboard-classen
        //Disse to blir brukt til MapFragment, for å vise hvor arrangementet tar sted
        val lat = intent.getDoubleExtra("Lat", 0.0)
        val long = intent.getDoubleExtra("Long", 0.0)

        val posisjon = LatLng(lat, long)
        //Legger til marker og zoomer inn
        mMap.addMarker(MarkerOptions().position(posisjon).title("Marker"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posisjon, 15f))

    }
}
