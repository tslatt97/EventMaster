package com.example.eventmaster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmaster.databinding.ActivityDashboardBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_dashboard.*


class DashboardActivity : AppCompatActivity(), (EventItem) -> Unit {

    private lateinit var auth: FirebaseAuth
    private lateinit var toggle: ActionBarDrawerToggle
    private var db : FirebaseFirestore = FirebaseFirestore.getInstance()
    private var eventsList: List<EventItem> = ArrayList()
    private val eventsAdapter: EventsAdapter = EventsAdapter(eventsList, this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initialiserer binding
        val binding =  DataBindingUtil.setContentView<ActivityDashboardBinding>(this, R.layout.activity_dashboard)
        auth = FirebaseAuth.getInstance()
        loadEvents()

        //Clicklistener for floating action button
        binding.fab.setOnClickListener {
            val intent = Intent(this, EventCreateActivity::class.java)
            startActivity(intent)
        }

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
                R.id.ic_map -> {
                    val intent = Intent(this,MapsActivity::class.java)
                    startActivity(intent)
                }
                R.id.ic_group -> {
                    val intent = Intent(this,MyEventsActivity::class.java)
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

    //Firestore
    // Tar en snapshot av Firestore collectionen som blir brukt
    // Kilde for enkelte funksjoner nedover i koden https://firebase.google.com/docs/firestore/query-data/get-data
    private fun getEventList(): Task<QuerySnapshot> {
        return db.collection("events")
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
    }

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
        val intent = Intent(this, EventDetailsActivity::class.java)
        intent.putExtra("Event Title" , eventItem.title )
        intent.putExtra("Event Description" , eventItem.details)
        intent.putExtra("EventType", eventItem.eventType)
        intent.putExtra("Event Address", eventItem.address)
        intent.putExtra("Lat", eventItem.lat)
        intent.putExtra("Long", eventItem.long)
        intent.putExtra("Date", eventItem.date)
        intent.putExtra("Time", eventItem.time)
        startActivity(intent)
    }
}


