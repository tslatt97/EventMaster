package com.example.eventmaster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_my_account.*

class MyAccount : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_account)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        userEmail.text = auth.currentUser?.email.toString()



        //Click listener for å slette bruker
        btnDeleteAccount.setOnClickListener {
            authUser()
        }

        //Clicklisteners for bottom-navigation
        bottom_navigationC.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.ic_home -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                }
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

        //Clicklisteners til venstre drawer-meny
        navView2.setNavigationItemSelectedListener{
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
    }

    /*Funksjon for å autentisere bruker. FireBase registrerer om du nylig har logget
    * inn eller ikke, og vil kreve re-autentisering dersom det er nødvendig*/
    private fun authUser() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Please enter credentials")
        val view = layoutInflater.inflate(R.layout.dialogue_reauthenticate, null)
        /*Oppretter 2 variabler som får tak input fra brukeren.
        *Disse blir senere brukt til å autentisere brukeren*/
        val email = view.findViewById<EditText>(R.id.authEmail)
        val password = view.findViewById<EditText>(R.id.authPassword)
        builder.setView(view)

        builder.setPositiveButton("Enter") { _, _ ->
            val credential =
                EmailAuthProvider.getCredential(email.text.toString(), password.text.toString())
            //Re-autentisering
            user.reauthenticate(credential)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("AUTH", "User re-authenticated.")
                        //Dersom reautentisering lykkes, skal brukeren slettes
                        deleteUser()
                    } else {
                        println("Feil 1")
                        Toast.makeText(this, "Error: " + it.exception, Toast.LENGTH_LONG).show()
                    }
                }
        }
        builder.setNegativeButton("Cancel") { _, _ -> }
        builder.show()
    }

    //Funksjon for å slette bruker
    private fun deleteUser() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Do you want to delete this user?")
        val view = layoutInflater.inflate(R.layout.dialouge_delete_account, null)
        builder.setView(view)
        builder.setPositiveButton("Yes") { _, _ ->
            //Får tak i nåværende innlogget bruker og forsøker å slette
            auth.currentUser?.delete()
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("Task.Message", "Successful, slettet")
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.e("Task.Message", "Failed... " + task.exception)
                    }
                }
        }
        builder.setNegativeButton("Cancel") { _, _ -> }
        builder.show()

    }


}
