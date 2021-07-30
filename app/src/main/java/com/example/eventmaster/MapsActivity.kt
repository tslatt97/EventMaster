package com.example.eventmaster

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    // Location values og variabler
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationReqest: LocationRequest
    //Unik permission-id
    var PERMISSION_ID = 1000
    //Variabler for å sammeligne brukerens posisjon med eventet sin addresse
    private val endPoint = Location(LocationManager.NETWORK_PROVIDER)


    /*
        Denne klassen utnytter samme kode som blir brukt i EventDetailsActivity, men uten avstand forskjell.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        // Starter funksjonene som henter bruker posisjon
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

    }



    //Funksjon som sjekker bruker-tillatelser
    private fun checkPermissions(): Boolean {
        if(
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }


    //Funksjon som ber om tillatelser
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_ID
        )
    }

    //Funksjon som ser om Location-service er aktivert
    private fun isLocationEnabled():Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    //Funksjon som henter ut siste posisjon
    private fun getLastLocation() {
        //Sjekker tillatelser
        println("getLastLocation()")
        if(checkPermissions()){
            //Sjekker om locationService er aktiver
            if(isLocationEnabled()){
                //Henter location
                fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    val location = task.result
                    if(location == null){
                        println("starting getNewLocation()")
                        //Hvis location er tom hentes den nye bruker locationen
                        getNewLocation()
                    }else {
                        println("endPoints set.")
                        endPoint.latitude = location.latitude
                        endPoint.longitude = location.longitude
                        println(endPoint)

                        val mapFragment = supportFragmentManager
                            .findFragmentById(R.id.map) as SupportMapFragment
                        mapFragment.getMapAsync(this)
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
        println("getNewLocation()")
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
            println("locationCallback()")
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
            println("onRequestPermissionResult()")
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Debug", "You have the permission")
            }

        }
    }
    companion object {
        private const val TAG = "LocationProvider"
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Legger til marker i Bø
        val bo = LatLng(endPoint.latitude,  endPoint.longitude)
        mMap.addMarker(MarkerOptions().position(bo).title("Your position"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bo, 15f))
    }
}