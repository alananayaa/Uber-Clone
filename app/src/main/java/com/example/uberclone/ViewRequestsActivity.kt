package com.example.uberclone

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.parse.*
import java.util.*
import kotlin.math.roundToInt

class ViewRequestsActivity: AppCompatActivity() {
    lateinit var requestListView:ListView
    lateinit var arrayAdapter:ArrayAdapter<String>
    lateinit var locationManager:LocationManager
    lateinit var locationListener:LocationListener
    var requests = ArrayList<String>()
    var requestLatitudes = ArrayList<Double>()
    var requestLongitudes = ArrayList<Double>()
    var usernames = ArrayList<String>()

    fun updateListView(location:Location) {
        if (location != null)
        {
            val query: ParseQuery<ParseObject> = ParseQuery.getQuery("Request")
            val geoPointLocation = ParseGeoPoint(location.latitude, location.longitude)
            query.whereNear("location", geoPointLocation)
            query.whereDoesNotExist("driverUsername")
            query.limit = 10
            query.findInBackground(FindCallback { objects, e ->
                if (e == null) {
                    requests.clear()
                    requestLongitudes.clear()
                    requestLatitudes.clear()
                    if (objects.size > 0)
                    {
                        for (`object` in objects)
                        {
                            val requestLocation = `object`.get("location") as ParseGeoPoint
                            if (requestLocation != null)
                            {
                                val distanceInKM = geoPointLocation.distanceInKilometersTo(requestLocation)
                                val distanceOneDP = (distanceInKM * 10).roundToInt().toDouble() / 10
                                requests.add("$distanceOneDP miles")
                                requestLatitudes.add(requestLocation.latitude)
                                requestLongitudes.add(requestLocation.longitude)
                                usernames.add(`object`.getString("username").toString())
                            }
                        }
                    }
                    else
                    {
                        requests.add("No active requests nearby")
                    }
                    arrayAdapter.notifyDataSetChanged()
                }
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F, locationListener)
                    val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    updateListView(lastKnownLocation)
                }
            }
        }

    }

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_requests)
        title = "Nearby Requests"
        requestListView = findViewById<ListView>(R.id.requestListView)
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, requests)
        requests.clear()
        requests.add("Getting nearby requests...")
        requestListView.adapter = arrayAdapter
        requestListView.setOnItemClickListener { _, _, position, _ ->
            if (ContextCompat.checkSelfPermission(this@ViewRequestsActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val lastKnownLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (requestLatitudes.size > position && requestLongitudes.size > position && usernames.size > position && lastKnownLocation != null) {
                    val intent =
                        Intent(applicationContext, DriverLocationActivity::class.java)
                    intent.putExtra("requestLatitude", requestLatitudes[position])
                    intent.putExtra("requestLongitude", requestLongitudes[position])
                    intent.putExtra("driverLatitude", lastKnownLocation.latitude)
                    intent.putExtra("driverLongitude", lastKnownLocation.longitude)
                    intent.putExtra("username", usernames[position])
                    startActivity(intent)
                }
            }
        }

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object: LocationListener {
            override fun onLocationChanged(location:Location) {
                updateListView(location)
                ParseUser.getCurrentUser().put("location", ParseGeoPoint(location.latitude, location.longitude))
                ParseUser.getCurrentUser().saveInBackground()
            }
            override fun onStatusChanged(s:String, i:Int, bundle:Bundle) {
            }
            override fun onProviderEnabled(s:String) {
            }
            override fun onProviderDisabled(s:String) {
            }
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
        else
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F, locationListener)
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null)
            {
                updateListView(lastKnownLocation)
            }
        }
    }
}