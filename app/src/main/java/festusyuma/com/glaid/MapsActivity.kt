package festusyuma.com.glaid

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.drawer_header.*
import android.provider.Settings;
import android.view.Window
import android.view.WindowManager
import festusyuma.com.glaid.model.User
import java.util.*
import kotlin.properties.Delegates

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    var longitude by Delegates.notNull<Double>()
    var latitude by Delegates.notNull<Double>()
    override fun onCreate(savedInstanceState: Bundle?) {
//        val w: Window = window
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            w.setFlags(
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//
//            )
//        }
//        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
//        w.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val sharedPref = getSharedPreferences("cached_data", Context.MODE_PRIVATE)
        val userJson = sharedPref.getString("userDetails", "null")

        if (userJson != null) {
            val fullNameTV: TextView = drawer_header.findViewById(R.id.fullName)
            val emailTV: TextView = drawer_header.findViewById(R.id.email)
            val user = gson.fromJson(userJson, User::class.java)

            fullNameTV.text = user.fullName
            emailTV.text = user.email
        }

        // tint removal for drawer items
        nav_view.itemIconTintList = null;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // fragment switching
        val rootFragment = RootFragment()
//        val detailsFragment = DetailsFragment()
        // fragment transaction to set root fragment on create
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
            .replace(R.id.framelayoutFragment, rootFragment)
            .commit()

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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.uber_map_style
                )
            )
            if (!success) {
                Log.e("FragmentActivity.TAG", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("FragmentActivity.TAG", "Can't find style. Error: ", e)
        }
        getLastLocation()
    }

    fun setUserLocationOnMap(latitude: Double, longitude: Double) {
        var userLocation = LatLng(latitude, longitude)
        val circleOptions = CircleOptions()
            .center(userLocation)
            .radius(500.0)
            .strokeWidth(0.0f)
            .strokeColor(Color.argb(50, 78, 0, 124))
            .fillColor(Color.argb(50, 78, 0, 124))
            .clickable(true); // In meters
        val mapIcon = AppCompatResources.getDrawable(this, R.drawable.customlocation)!!.toBitmap()
        mMap.addMarker(
            MarkerOptions().position(userLocation).title("Marker in Sydney")
                .icon(BitmapDescriptorFactory.fromBitmap(mapIcon))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15.0f))
        mMap.addCircle(circleOptions)

    }

    // This will check if the user has turned on location from the setting
    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // check if user has given permission
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    // request permission from user if they've no granted the permission
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    // This method is called when a user Allow or Deny our requested permissions. So it will help us to move forward if the permissions are granted
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Granted. Start getting the location information
            }
        } else {
            // request the permission again
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        latitude = location.latitude
                        longitude = location.longitude
//                        mMap.isMyLocationEnabled = true
//                        mMap.uiSettings.isMyLocationButtonEnabled = true
                        setUserLocationOnMap(latitude, longitude)
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    // record the location information in runtime to prevent location from being null
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    // callback for previous locations
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            latitude = mLastLocation.latitude
            longitude = mLastLocation.longitude
        }
    }

    // edit profile intent
    fun editProfileIntent(view: View) {
        val editIntent = Intent(this, EditProfileActivity::class.java)
        startActivity(editIntent)
    }

    fun toggleDrawerClick(view: View) {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            rating.setRating(4.5f)
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
            rating.setRating(4.5f)
        }
    }

    fun goToUserLocation(view: View) {
        var userLocation = LatLng(this.latitude, this.longitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17.0f))
    }

    private fun henryCloseDrawer() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

    }

    fun inviteFriendsClick(view: View) {
        henryCloseDrawer()
        val intent = Intent(this, InviteFriendsActivity::class.java)
        startActivity(intent)
    }

    fun helpClick(view: View) {
        henryCloseDrawer()
        val intent = Intent(this, HelpSupportActivity::class.java)
        startActivity(intent)
    }

    fun orderHistoryClick(view: View) {
        henryCloseDrawer()
        val intent = Intent(this, OrderHistoryActivity::class.java)
        startActivity(intent)

    }

    fun paymentClick(view: View) {}

    fun hideError(view: View) {
        val errorMsg: TextView = findViewById(R.id.errorMsg)
        errorMsg.visibility = View.INVISIBLE
    }
}
