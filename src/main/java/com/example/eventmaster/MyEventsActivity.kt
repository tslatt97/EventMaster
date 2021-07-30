package com.example.eventmaster

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_dashboard.*

class MyEventsActivity : AppCompatActivity(), (EventItem) -> Unit {

    private lateinit var auth: FirebaseAuth
    private lateinit var toggle: ActionBarDrawerToggle
    private var db : FirebaseFirestore = FirebaseFirestore.getInstance()
    private var eventsList: List<EventItem> = ArrayList()
    private val eventsAdapter: EventsAdapter = EventsAdapter(eventsList, this)

    // Brukerinformasjon
    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private val userID = user?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_events)

        // Henter Firebase forekomst
        auth = FirebaseAuth.getInstance()
        // Henter events som skal fylle recyclerviewen
        loadEvents()

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = eventsAdapter

        //Oppretter toggle som håndterer klikk og åpning av drawerLayout
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Clicklisteners til venstre drawer-meny
        navView.setNavigationItemSelectedListener{
            when(it.itemId) {
                R.id.NavLogout -> { auth.signOut()
                    val intent = Intent(this,LoginActivity::class.java)
                    startActivity(intent)
                }
                R.id.myAccount -> {
                    val intent = Intent(this, MyAccount::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        //Clicklisteners for bottom-navigation
        bottom_navigation.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.ic_home -> {
                    val intent = Intent(this, DashboardActivity::class.java)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /*
         Tar en snapshot av Firestore collectionen som blir brukt
         sender er query som finner events med lik userID som brukeren
         og sender den tilbake til loadEvents()
     */
    private fun getEventList(): Task<QuerySnapshot> {
        return db.collection("events")
            .whereEqualTo("id", userID)
            .get()
    }

    /*
        KJører getEventList() og henter relevante dokumenter fra Firestore collectionen
     */
    private fun loadEvents() {
        getEventList().addOnCompleteListener {
            if(it.isSuccessful) {
                eventsList = it.result!!.toObjects(EventItem::class.java)
                eventsAdapter.eventsListItem = eventsList
                eventsAdapter.notifyDataSetChanged()
            }else {
                Log.d("Dashboard", "Error: ${it.exception!!.message}")
            }
        }
    }


    // Informasjonen fra Firestore Collectionen blir påkallet
    // Informasjonen blir hentet senere av EventDetailsActivitien
    override fun invoke(eventItem: EventItem) {
        val intent = Intent(this, MyEventDetails::class.java)
        intent.putExtra("Event Title" , eventItem.title )
        intent.putExtra("Event Description" , eventItem.details)
        intent.putExtra("EventType", eventItem.eventType)
        intent.putExtra("Event Address", eventItem.address)
        intent.putExtra("Lat", eventItem.lat)
        intent.putExtra("Long", eventItem.long)
        intent.putExtra("Date", eventItem.date)
        intent.putExtra("Time", eventItem.time)
        intent.putExtra("documentId", eventItem.documentId)
        startActivity(intent)
    }
}