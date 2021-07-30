package com.example.eventmaster

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.Error

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        tvRegister.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Glemt passord link
        forgottenPassword.setOnClickListener{

            // Builder som inneholder felt som forventer email input.
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Forgot Password")
            val view = layoutInflater.inflate(R.layout.dialogue_forgotten_password, null)
            val username = view.findViewById<EditText>(R.id.usernameID)
            builder.setView(view)
            builder.setPositiveButton("Reset") { _, _ ->
                forgotPassword(username)
            }
            builder.setNegativeButton("Close") { _, _ -> }
            builder.show()
        }

        btnLogin.setOnClickListener{
            try {


            if(editTextEmail.text.toString().isNotEmpty() &&  editTextPassword.text.trim().toString().isNotEmpty()){
                signInUser(editTextEmail.text.trim().toString(), editTextPassword.text.trim().toString())

            }else  {
                Toast.makeText(this,"Input required", Toast.LENGTH_SHORT).show()

            }} catch(e: Error) {
                println(e.toString())
            }
        }
    }


    /* Funksjon for å resete glemt passord
       https://firebase.google.com/docs/auth/web/manage-users#send_a_password_reset_email
       Firebase sin egen dokumentasjon, denne siden av dokumentasjonen inneholder mye informasjon om hvordan man kan redigere/hente bruker informasjon
     */
    private fun forgotPassword(username : EditText){
        // Gir error om email feltet er tomt
        if(username.text.toString().isEmpty()){
            username.error = "Please enter your Email"
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(username.text.toString()).matches()){
            return
        }
        // Firebase funksjon som henter Email og sender mail til emailen som inneholder informasjon om hvordan man får nytt passord
        auth.sendPasswordResetEmail(username.text.toString()).addOnSuccessListener {
            // Kort toast på bunnen av skjermen som viser at funskjonen ble gjennomført
            Toast.makeText(this, "Email Sent", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInUser(email:String, password:String) {
        progressBar.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) {task ->
                if(task.isSuccessful){
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                }else {
                    Toast.makeText(this,"Error .. " +task.exception, Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                }
            }
    }
}