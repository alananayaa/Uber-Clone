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
import android.os.Handler
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.parse.*
import java.util.ArrayList
import kotlin.math.roundToInt

class RiderActivity: FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    lateinit var callUberButton: Button
    lateinit var infoTextView: TextView

    private val handler: Handler = Handler()

    var requestActive: Boolean = false
    var driverActive: Boolean = true

    private fun checkForUpdates() {
        val query: ParseQuery<ParseObject> = ParseQuery.getQuery("Request")
        query.whereEqualTo("username", ParseUser.getCurrentUser().username)
        query.whereExists("driverUsername")

        query.findInBackground { objects, e ->
            if (e == null && objects.size > 0) {
                val driverLocation: ParseGeoPoint? = objects[0].getParseGeoPoint("location")
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    val lastKnownLocation: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastKnownLocation != null) {
                        val userLocation: ParseGeoPoint = ParseGeoPoint(lastKnownLocation.latitude, lastKnownLocation.longitude)
                        val distanceToKM: Double = driverLocation!!.distanceInKilometersTo(userLocation)

                        if (distanceToKM < 0.1) {
                            infoTextView.text = "Your driver is here!"

                            val query: ParseQuery<ParseObject> = ParseQuery.getQuery("Request")
                            query.whereEqualTo("username", ParseUser.getCurrentUser().username)

                            query.findInBackground { objects, e ->
                                if (e == null) {
                                    objects.forEach{
                                        it.deleteInBackground()
                                    }
                                }
                            }

                            handler.postDelayed({
                                infoTextView.text = ""
                                callUberButton.visibility = View.VISIBLE
                                callUberButton.text = "Call An Uber"
                                requestActive = false
                                driverActive = false
                            }, 5000)
                        } else {
                            val distanceOneDP: Double = distanceToKM.roundToInt().toDouble()
                            infoTextView.text = "Your driver is $distanceOneDP kilometers away!"

                            val driverLocationLatLng: LatLng = LatLng(driverLocation.latitude, driverLocation.longitude)
                            val requestLocationLatLng: LatLng = LatLng(userLocation.latitude, userLocation.longitude)
                            val markers = ArrayList<Marker>()
                            mMap.clear()

                            markers.add(mMap.addMarker(MarkerOptions().position(driverLocationLatLng).title("Driver Location")))
                            markers.add(mMap.addMarker(MarkerOptions().position(requestLocationLatLng).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))))

                            val builder = LatLngBounds.Builder()
                            for (marker in markers) {
                                builder.include(marker.position)
                            }
                            val bounds = builder.build()
                            val padding = 60 // offset from edges of the map in pixels
                            val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                            mMap.animateCamera(cu)

                            callUberButton.visibility = View.INVISIBLE

                            handler.postDelayed({
                                checkForUpdates()
                            }, 2000)

                        }
                    }
                }
            }
        }
    }

    fun logout(view: View) {
        ParseUser.logOut()
        val intent: Intent = Intent(this@RiderActivity, MainActivity::class.java)
        startActivity(intent)
    }

    fun callUber(view: View) {
        Log.i("Info", "Call Uber")

        if (requestActive) {
            val query: ParseQuery<ParseObject> = ParseQuery("Request")
            query.whereEqualTo("username", ParseUser.getCurrentUser().username)
            query.findInBackground { objects, e ->
                if ( e == null) {
                    if (objects.size > 0) {
                        for (`object` in objects) {
                            `object`.deleteInBackground()
                        }
                        requestActive = false
                        callUberButton.text = "Call An Uber"
                    }
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10F,
                    this.locationListener
                )
                val lastKnownLocation: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if (lastKnownLocation != null) {
                    val request: ParseObject = ParseObject("Request")
                    request.put("username", ParseUser.getCurrentUser().username)
                    val parseGeoPoint: ParseGeoPoint = ParseGeoPoint(lastKnownLocation.latitude, lastKnownLocation.longitude)
                    request.put("location", parseGeoPoint)

                    request.saveInBackground { e ->
                        if (e == null) {
                            callUberButton.text = "Cancel Uber"
                            requestActive = true
                            checkForUpdates()
                        }
                    }
                } else {
                    Toast.makeText(this, "Could not find location. Please try again later.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10F, this.locationListener)
                    val lastKnownLocation: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    updateMap(lastKnownLocation!!)

                }
            }
        }
    }

    fun updateMap(location: Location) {
        if (!driverActive) {
            val userLocation = LatLng(location.latitude, location.longitude)
            mMap.clear()
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15F))
            mMap.addMarker(MarkerOptions().position(userLocation).title("Your Location"))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rider)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        callUberButton = findViewById<Button>(R.id.callUberButton)
        infoTextView = findViewById<TextView>(R.id.infoTextView)

        val query: ParseQuery<ParseObject> = ParseQuery("Request")
        query.whereEqualTo("username", ParseUser.getCurrentUser().username)

        query.findInBackground { objects, e ->
            if (e == null) {
                if (objects.size > 0){
                    requestActive = true
                    callUberButton.text = "Cancel Uber"

                    checkForUpdates()
                }
            }
        }

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

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            mMap = googleMap
        }


        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                updateMap(location)
            }
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10F, this.locationListener)
            val lastKnownLocation: Location? =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null){
                updateMap(lastKnownLocation)
            }
        }

    }

}