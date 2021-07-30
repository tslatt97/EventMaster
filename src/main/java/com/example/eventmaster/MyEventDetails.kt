package com.example.eventmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_event_details.bottom_navigationD
import kotlinx.android.synthetic.main.activity_event_details.event_address
import kotlinx.android.synthetic.main.activity_event_details.event_details
import kotlinx.android.synthetic.main.activity_event_details.event_title
import kotlinx.android.synthetic.main.activity_event_details.event_type
import kotlinx.android.synthetic.main.activity_my_event_details.*


class MyEventDetails : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var auth: FirebaseAuth

    private var documentIdValue = "" // Tom verdi for documentId som blir fylt senere
    private val rootRef: FirebaseFirestore = FirebaseFirestore.getInstance()                // Referanse til Firestore
    private val eventsRef: CollectionReference = rootRef.collection("events") // Referanse til Firestore collectionen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_event_details)


        //Får tak i mapFragment slik at man kan opprette pins, lat/long, zoom osv
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        auth = FirebaseAuth.getInstance()

        // Henter informasjon som ble invoked i Dashboard-Classens invoke funksjon
        event_title.text = intent.getStringExtra("Event Title")
        event_details.text = intent.getStringExtra("Event Description")
        event_type.text = intent.getStringExtra("EventType")
        event_address.text = intent.getStringExtra("Event Address")
        myDateText.text = intent.getStringExtra("Date")
        myTimeText.text = intent.getStringExtra("Time")
        documentIdValue = intent.getStringExtra("documentId").toString() // dokument IDen som ble auto-generert i "EventCreateActivity" blir nå brukt for å identifisere dokument til sletting


        //Clicklisteners for bottom-navigation
        bottom_navigationD.setOnNavigationItemSelectedListener{
            when(it.itemId){
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

        // Slett knapp som kjører slett funksjonen
        btnDelete.setOnClickListener{
                deleteEvent()
            }
        }








    /* Funksjon for å slette events brukeren har laget selv.
       Funksjonen starter med å kjøre queryen som blir laget inni funksjonen
       Queryen som kjører i funksjonen sjekker om documentIden som ble hentet i "MyEventsActivity" er lik den IDen som er i dokumentet,
       om det er likt vil funksjonen kjøre videre og slette referansen til dokumentet, som da vil slette hele dokumentet.

       KILDER:
       https://firebase.google.com/docs/firestore/manage-data/delete-data
       https://stackoverflow.com/questions/65003996/firebase-kotlin-find-document-id-delete-document-with-id
     */
    private fun deleteEvent() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Event")
        val view = layoutInflater.inflate(R.layout.dialogue_delete_event, null)
        builder.setView(view)

        // Kjører når brukeren trykker på "Yes" knappen
        builder.setPositiveButton("Yes") { _, _ ->
            val docIdQuery: Query = eventsRef.whereEqualTo("documentId", documentIdValue)
            val intent = Intent(this, MyEventsActivity::class.java)
            val slettToast = Toast.makeText(this, "Event successfully deleted", Toast.LENGTH_SHORT)

            docIdQuery.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        document.reference.delete().addOnSuccessListener {
                            startActivity(intent)
                            slettToast.show()
                        }.addOnFailureListener { }
                    }
                } else {
                    println("Error getting documents")
                }
            }
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.show()
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