package com.example.uberclone
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import java.util.ArrayList

class DriverLocationActivity: FragmentActivity(), OnMapReadyCallback {
    lateinit var mMap: GoogleMap

    fun acceptRequest(view: View) {
        val query: ParseQuery<ParseObject> = ParseQuery.getQuery("Request")
        query.whereEqualTo("username", intent.getStringExtra("username"))
        query.findInBackground { objects, e ->
            if (e == null) {
                if (objects.size > 0)
                {
                    for (`object` in objects)
                    {
                        `object`.put("driverUsername", ParseUser.getCurrentUser().username)
                        `object`.saveInBackground { e ->
                            if (e == null)
                            {
                                val directionsIntent = Intent(
                                    ACTION_VIEW,
                                    Uri.parse("http://maps.google.com/maps?saddr=" + intent.getDoubleExtra("driverLatitude", 0.0) + "," + intent.getDoubleExtra("driverLongitude", 0.0) + "&daddr=" + intent.getDoubleExtra("requestLatitude", 0.0) + "," + intent.getDoubleExtra("requestLongitude",
                                        0.0
                                    )))
                                startActivity(directionsIntent)
                            }
                        }
                    }
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_location)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val intent = intent
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        val mapLayout = findViewById<RelativeLayout>(R.id.mapRelativeLayout)
        mapLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val driverLocation = LatLng(intent.getDoubleExtra("driverLatitude", 0.0), intent.getDoubleExtra("driverLongitude", 0.0))
            val requestLocation = LatLng(intent.getDoubleExtra("requestLatitude", 0.0), intent.getDoubleExtra("requestLongitude", 0.0))
            val markers = ArrayList<Marker>()
            markers.add(mMap.addMarker(MarkerOptions().position(driverLocation).title("Your Location")))
            markers.add(mMap.addMarker(MarkerOptions().position(requestLocation).title("Request Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))))
            val builder = LatLngBounds.Builder()
            for (marker in markers) {
                builder.include(marker.position)
            }
            val bounds = builder.build()
            val padding = 60 // offset from edges of the map in pixels
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            mMap.animateCamera(cu)
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
    override fun onMapReady(googleMap:GoogleMap) {
        mMap = googleMap ?: return
    }
}