package com.example.uberclone

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.parse.Parse
import com.parse.ParseObject
import com.parse.ParseUser
import com.parse.SignUpCallback

class SignUpActivity : AppCompatActivity() {

    fun signUp(view: View) {
//        val user = ParseUser()
//        user.username = "test2"
//        user.setPassword("1234Password")
//        user.put("riderOrDriver", "rider")
//        user.signUpInBackground { e ->
//            if (e == null){
//                Log.d("DEBUG", "user saved test2")
//                Log.i("USERTYPE", ParseUser.getCurrentUser().get("riderOrDriver").toString())
//            }
//        }
        val username: String = findViewById<TextView>(R.id.txtUsername).text.toString()
        val password: String = findViewById<TextView>(R.id.txtUserPassword).text.toString()
        var userType: String = if (!findViewById<Switch>(R.id.switcher).isChecked){
            "rider"
        } else {
            "driver"
        }
        val user = ParseUser()
        user.username = username
        user.setPassword(password)
        user.put("riderOrDriver", userType)
        user.signUpInBackground { e ->
            if ( e == null){
                alertDisplayer("Successful Sign Up!","Welcome $username!")
            } else {
                ParseUser.logOut()
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun alertDisplayer(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                dialog.cancel()
                val nextIntent = if (ParseUser.getCurrentUser().get("riderOrDriver").toString() == "rider"){
                    Intent(this@SignUpActivity, RiderActivity::class.java)
                }else {
                    Intent(this@SignUpActivity, DriverLocationActivity::class.java)
                }
                startActivity(nextIntent)
            })
        val ok: AlertDialog = builder.create()
        ok.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        title = "Sign Up to UbClone"
    }
}