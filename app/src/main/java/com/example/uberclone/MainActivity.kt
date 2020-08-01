package com.example.uberclone

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import com.parse.LogInCallback
import com.parse.ParseAnalytics
import com.parse.ParseAnonymousUtils
import com.parse.ParseUser

class MainActivity : AppCompatActivity() {

    private fun alertDisplayer(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
                val nextIntent = if (ParseUser.getCurrentUser().get("riderOrDriver").toString() == "rider"){
                    Intent(this@MainActivity, RiderActivity::class.java)
                }else {
                    Intent(this@MainActivity, ViewRequestsActivity::class.java)
                }
                startActivity(nextIntent)
            })
        val ok: AlertDialog = builder.create()
        ok.show()
    }

    fun logIn(view: View) {
        val username = findViewById<EditText>(R.id.txtUsernameMain).text.toString()
        val password = findViewById<EditText>(R.id.txtPasswordMain).text.toString()
        ParseUser.logInInBackground(username, password, LogInCallback{user, e ->
            if (user != null) {
                alertDisplayer("Successful Login","Welcome back ${ParseUser.getCurrentUser().username}!")
            } else {
                ParseUser.logOut()
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    fun signUp(view: View) {
        startActivity(Intent(this@MainActivity, SignUpActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        if (ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().get("riderOrDriver") != null) {
                Log.i("Info", "Redirecting as " + ParseUser.getCurrentUser().get("riderOrDriver"))
                alertDisplayer("Successful Login","Welcome back ${ParseUser.getCurrentUser().username}!")
            }
        }

        ParseAnalytics.trackAppOpenedInBackground(intent)
    }
}