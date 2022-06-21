package com.archives

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.archives.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

/**
 * Main Activity that contains the majority of the functionality for Archives
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
android.location.LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var stories: HashMap<String,MarkerInfo>
    private lateinit var classicStories: ArrayList<MarkerInfo>
    private lateinit var middleAgeStories: ArrayList<MarkerInfo>
    private lateinit var preHistoryStories: ArrayList<MarkerInfo>
    private lateinit var preModernStories: ArrayList<MarkerInfo>
    private lateinit var modernStories: ArrayList<MarkerInfo>
    private lateinit var location : LocationManager
    private lateinit var closeStories : ArrayList<MarkerInfo>


    private var array : Array<String>? = null

    private var preHistory : Boolean = false
    private var classical : Boolean = false
    private var modern : Boolean = false
    private var premodern : Boolean = false
    private var middle : Boolean = false
    private var isLoaded : Boolean = false
    private var closeStoriesSelected : Boolean = false

    private var geoLongitude : Double = 0.0
    private var geoLatitude : Double = 0.0
    private var radius : Int = 0

    /**
     * OnCreate method checks for location permission, calls the database and loads in the stories and
     * places them in their correct Array Lists.
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        array = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, array!!, 100)
        }

        val gb = GlobalScope.async {
               stories = DBHandler.getStories()
                getLocation()
           }
          while(!gb.isCompleted) {
              SystemClock.sleep(3)
          }

        preHistoryStories = ArrayList()
        classicStories = ArrayList()
        middleAgeStories = ArrayList()
        preModernStories = ArrayList()
        modernStories = ArrayList()


        for (marker: Map.Entry<String,MarkerInfo> in stories) {
            if (marker.value.category.equals("CLASSIC"))
                classicStories.add(marker.value)
            else if(marker.value.category.equals("PREHISTORY"))
                preHistoryStories.add(marker.value)
            else if(marker.value.category.equals("MIDDLE"))
                middleAgeStories.add(marker.value)
            else if(marker.value.category.equals("PREMODERN"))
                preModernStories.add(marker.value)
            else if(marker.value.category.equals("MODERN"))
                modernStories.add(marker.value)
            else continue
        }

    }

    /**
     * Handles user event when user clicks on the Info Window
     * Uses the marker it clicks on as the comparison to find the
     * respective URL within the hash map.
     */
    override fun onInfoWindowClick(marker : Marker) {
        val fragment = supportFragmentManager
        val url = StringBuilder()
        for( markerInfo : Map.Entry<String,MarkerInfo> in stories) {
            if (marker.title?.equals(markerInfo.value.title) == true) {
                url.append(markerInfo.value.externalResource)
                break
            }
        }
        fragment.beginTransaction().replace(R.id.map,WebFragment(url.toString()))
            .setReorderingAllowed(true).addToBackStack("name").commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu_selection, menu)
        return true
    }

    /**
     * onMapReady renders the map for the user and specifies which stories will be shown.
     * If the infoWindowAdapter is already set, skip that portion and continue.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        if(!isLoaded) {
            mMap = googleMap
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            val customInfoWindow  = CustomInfoWindow(this)
            mMap.setInfoWindowAdapter(customInfoWindow)
            isLoaded = true
        }
        if (preHistory) {
            mMap.clear()
            createMarkers(preHistoryStories)
        }else if (classical) {
            mMap.clear()
            createMarkers(classicStories)
        }else if (middle) {
            mMap.clear()
            createMarkers(middleAgeStories)
        } else if (premodern) {
            mMap.clear()
            createMarkers(preModernStories)
        } else if (modern) {
            mMap.clear()
            createMarkers(modernStories)
        } else if (closeStoriesSelected) {
            mMap.clear()
            createMarkers(closeStories)
        }
        mMap.setOnInfoWindowClickListener(this)
    }

    /**
     * Used for selecting items in menu bar
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.burger_menu -> showOptionsDialog()
            R.id.geoMenu -> {showGeoLocationDialog()
            getLocation()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Dialog that prompts the user to choose a radius. If the location of a story is
     * within the desired radius, show it on the map.
     */
    private fun showGeoLocationDialog() {
        closeStories = ArrayList()
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stories_near_me)
        val button: Button = dialog.findViewById(R.id.geo_button)
        val radioGroup: RadioGroup = dialog.findViewById(R.id.milage_buttons)
        button.setOnClickListener {
            when(radioGroup.checkedRadioButtonId) {
                R.id.twenty_five -> radius = 25
                R.id.fifty_miles -> radius = 50
                R.id.hundred_miles -> radius = 100
            }
            for (marker: Map.Entry<String,MarkerInfo> in stories) {
                val locationA = Location("locationA")
                val locationB = Location("locationB")
                locationA.latitude = marker.value.longitude!!
                locationA.longitude = marker.value.latitude!!
                locationB.latitude = geoLatitude
                locationB.longitude = geoLongitude
                locationA.distanceTo(locationB)
                val value = locationB.distanceTo(locationA) / 1609.34
                if (value <= radius)
                        closeStories.add(marker.value)
            }
            preHistory = false
            classical = false
            middle = false
            premodern = false
            modern = false
            closeStoriesSelected = true
            onMapReady(mMap)
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * Show the time periods to choose which stories will be displayed on the map.
     */
    private fun showOptionsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.option_pop_up_window)
        val radioGroup: RadioGroup = dialog.findViewById(R.id.radio_group)
        val accept: Button = dialog.findViewById(R.id.accept)
        accept.setOnClickListener {
            when(radioGroup.checkedRadioButtonId) {
                R.id.prehistory -> {
                    preHistory = true
                    classical = false
                    middle = false
                    premodern = false
                    modern = false
                    closeStoriesSelected = false
                }
                R.id.classical -> {
                    preHistory = false
                    classical = true
                    middle = false
                    premodern = false
                    modern = false
                    closeStoriesSelected = false
                }
                R.id.middle -> {
                    preHistory = false
                    classical = false
                    middle = true
                    premodern = false
                    modern = false
                    closeStoriesSelected = false
                }
                R.id.premodern -> {
                    preHistory = false
                    classical = false
                    middle = false
                    premodern = true
                    modern = false
                    closeStoriesSelected = false
                }
                R.id.modern -> {
                    preHistory = false
                    classical = false
                    middle = false
                    premodern = false
                    modern = true
                    closeStoriesSelected = false
                }
            }
            onMapReady(mMap)
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * Dynamically adds the markers into the map by being called in the onMapReady method
     * and is passed a desired ArrayList to display.
     */
    private fun createMarkers(arrayList: ArrayList<MarkerInfo>) {
        for(marker : MarkerInfo in arrayList) {
            val icon = marker.icon?.let { assignIcon(it) }
            val mapMarker = LatLng(marker.longitude!!,marker.latitude!!)
            mMap.addMarker(MarkerOptions().position(mapMarker).title(marker.title).icon
                (icon?.let { bitmapDescriptorFromVector(it) })
                .snippet(stories[marker.title]?.abstract))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mapMarker))
        }
    }

    /**
     * Dynamically assign an icon
     */
    private fun assignIcon(string : String): Int {
        var icon = 0
        when(string) {
            "PERSON" -> icon = R.drawable.person
            "BATTLE" -> icon = R.drawable.battle
            "EVENT" -> icon = R.drawable.ic_baseline_star_24
            "INVENTION" -> icon = R.drawable.light
            "MYSTERY" -> icon = R.drawable.mystery
            "PLACE" -> icon = R.drawable.place
        }
        return icon
    }

    /**
     * Translate a vector into a BitmapDescriptor.
     */
    private fun bitmapDescriptorFromVector(vectorResId:Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        vectorDrawable.draw(Canvas(bitmap))
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * Update location if it has changed.
     */
    override fun onLocationChanged(currentLocation : Location) {
        geoLatitude = currentLocation.latitude
        geoLongitude = currentLocation.longitude
    }

    /**
     * Obtain device location.
     * SIDE NOTE: Dr. Coles, Im not sure why my IDE is telling me to add this MissingPermissions Lint.
     * I made sure to check for permissions as well as put the permissions in the manifest file.
     */
    @SuppressLint("MissingPermission")
    fun getLocation() {
        try {
            location = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
            location.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,this)
            val currentLocation : Location = location.getLastKnownLocation(LocationManager.GPS_PROVIDER)!!
            geoLatitude = currentLocation.latitude
            geoLongitude = currentLocation.longitude
        }catch (e : Exception) {
            e.printStackTrace()
        }
    }
}