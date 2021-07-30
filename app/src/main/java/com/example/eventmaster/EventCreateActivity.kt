package com.example.eventmaster
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.eventmaster.databinding.EventCreateBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.event_create.*
import kotlin.collections.HashMap
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import com.example.eventmaster.places.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.event_create.editAddress
import java.util.*
import kotlin.collections.ArrayList


class EventCreateActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    //må ha med @RequiresApi pga current api er 19 og man trenger 24+ for dato og klokkeslett
    // @RequiresApi(Build.VERSION_CODES.N) // Fjern kommentar om programmet ber om API 24+
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

    private val placesApi = PlaceAPI.Builder()
        .apiKey("AIzaSyB-IMDC8kwocn4xUp-u5_wP13HygL7KShg")
        .build(this@EventCreateActivity)

    //Lager tomme verdier
    private var street = ""
    private var city = ""
    private var valueLat = 0.0
    private var valueLong = 0.0
    private var documentIdValue = "" // Tom verdi som blir tildelt en auto-generert string slik at alle dokumenter har en unik ID
    private var idLength = 20        // Alle dokument IDer skal ha en lengde på 20 bokstaver/tall

    // @RequiresApi(Build.VERSION_CODES.N) // Fjern kommentar om programmet ber om API 24+
    override fun onCreate(savedInstanceState: Bundle? ) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<EventCreateBinding>(this,R.layout.event_create)
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!

        getRandomString(idLength)
        documentIdValue = getRandomString(idLength) // Kjører getRandomString() Funksjonen som produserer random string på 20 tall/bokstaver

        val now : Calendar = Calendar.getInstance()
        btnDate.text = dateFormat.format(now.time)
        btnTime.text = timeFormat.format(now.time)

        editAddress.setAdapter(PlacesAutoCompleteAdapter(this, placesApi))
        editAddress.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val place = parent.getItemAtPosition(position) as Place
                editAddress.setText(place.description)
                getPlaceDetails(place.id)
            }

        //Adapter og kategorier for 'Event-type' - Hva slags kategori dreier eventet seg om
        val eventtype = arrayOf("Sport","Konsert","Arrangement", "Friluftsliv","Fest", "Lek og morro", "Annet")
        val adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,eventtype)
        editEvent.threshold=0
        editEvent.setAdapter(adapter)
        editEvent.inputType = InputType.TYPE_NULL
        editEvent.setOnFocusChangeListener { _, b -> if(b) editEvent.showDropDown() }
        editEvent.keyListener = null

        //Clicklistener for create-knappen - opprettelse av arrangement
        btnCreate.setOnClickListener {
            val editTitle = editTitle.text.toString()
            val editEvent = editEvent.text.toString()
            val editAddress = editAddress.text.toString()
            val editDetails = editDetails.text.toString()
            val id = auth.currentUser!!.uid
            val editDate = dateFormat.format(now.time)
            val editTime = timeFormat.format(now.time)
            val lat = valueLat
            val long = valueLong

            saveFireStore(editTitle, editEvent, editAddress, editDetails, lat, long, id, editDate, editTime)
        }

        btnCancel.setOnClickListener{
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        // clicklistener for å sette dato
        // Kilde: https://developer.android.com/reference/android/app/DatePickerDialog
        btnDate.setOnClickListener {
            val editDate = DatePickerDialog(this, {
                    _, year, month, dayOfMonth ->
                //Setter dagens dato når man åpner create-event
                now.set(Calendar.YEAR, year)
                now.set(Calendar.MONTH, month)
                now.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                btnDate.text = dateFormat.format(now.time)
            },
                now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
            )
            editDate.show()
        }

        //clicklistener for å sette tidspunkt
        // Kilde: https://developer.android.com/reference/android/app/TimePickerDialog
        btnTime.setOnClickListener {
            val editTime = TimePickerDialog(this, {
                    _, hourOfDay, minute ->
                //Setter klokkeslett til akkurat nå når man åpner create-event
                now.set(Calendar.HOUR_OF_DAY, hourOfDay)
                now.set(Calendar.MINUTE, minute)
                btnTime.text = timeFormat.format(now.time)
            },
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false
            )
            editTime.show()
        }

        //Clicklisteners for bottom-navigation
        bottom_navigationC.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.ic_home -> {
                    val intent = Intent(this,DashboardActivity::class.java)
                    startActivity(intent)
                }
                R.id.ic_map -> {
                    val intent = Intent(this,MapsActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }



    // Lager random string som blir brukt til dokumentasjon identifikasjon
    // Denne IDen blir brukt i "MyEventDetails" activitien, for å slette bruker events
    private fun getRandomString(length: Int) : String {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return List(length) { charset.random() }
            .joinToString("")
    }

    //Funksjon for å lagre events i Firestore
    // @RequiresApi(Build.VERSION_CODES.N) // Fjern kommentar om programmet ber om API 24+
    // Kilde: https://firebase.google.com/docs/firestore/manage-data/add-data
    fun saveFireStore(
        title: String,
        eventType: String,
        address: String,
        details: String,
        lat: Double,
        long: Double,
        id: String,
        date: String,
        time: String
    ){
        if(title.isEmpty() || eventType.isEmpty() || address.isEmpty() || details.isEmpty())  {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show()
        } else {
            val db = FirebaseFirestore.getInstance()
            val events: MutableMap<String, Any> = HashMap()
            events["title"] = title
            events["eventType"] = eventType
            events["address"] = address
            events["details"] = details
            events["lat"] = lat
            events["long"] = long
            events["id"] = id
            events["documentId"] = documentIdValue
            events["date"] = date
            events["time"] = time

            db.collection("events")
                .add(events)

            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }
    }




    //Her hentes detaljer rundt addressen man velger
    private fun getPlaceDetails(placeId: String) {
        placesApi.fetchPlaceDetails(placeId, object :
            OnPlacesDetailsListener {
            override fun onError(errorMessage: String) {
                Toast.makeText(this@EventCreateActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onPlaceDetailsFetched(placeDetails: PlaceDetails) {
                setupUI(placeDetails)
            }
        })
    }

    //Ubrukelig, kun for å vise lat/long i "create event" vinduet
     private fun setupUI(placeDetails: PlaceDetails) {
        val address = placeDetails.address
        parseAddress(address)
        runOnUiThread {
            valueLat = placeDetails.lat
            valueLong = placeDetails.lng
        }
    }

    private fun parseAddress(address: ArrayList<Address>) {
        (0 until address.size).forEach { i ->
            when {
                address[i].type.contains("street_number") -> street += address[i].shortName + " "
                address[i].type.contains("route") -> street += address[i].shortName
                address[i].type.contains("locality") -> city += address[i].shortName

            }
        }
    }
}