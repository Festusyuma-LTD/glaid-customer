package festusyuma.com.glaid

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
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
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import festusyuma.com.glaid.model.Order
import festusyuma.com.glaid.model.User
import festusyuma.com.glaid.model.live.LiveOrder
import festusyuma.com.glaid.model.live.PendingOrder
import kotlin.properties.Delegates

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val errorDialogRequest = 9001

    private val requestCode = 42
    private val permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private var locationPermissionsGranted = false

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userMarker: Marker

    var longitude by Delegates.notNull<Double>()
    var latitude by Delegates.notNull<Double>()

    private lateinit var userLocationBtn: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        nav_view.itemIconTintList = null;
        val dataPref = getSharedPreferences("cached_data", Context.MODE_PRIVATE)
        val userJson = dataPref.getString("userDetails", "null")

        if (userJson != null) {
            val fullNameTV: TextView = drawer_header.findViewById(R.id.fullName)
            val emailTV: TextView = drawer_header.findViewById(R.id.email)
            val user = gson.fromJson(userJson, User::class.java)

            fullNameTV.text = user.fullName
            emailTV.text = user.email
        }

        if (isServiceOk()) {
            initMap()
        }

        if (dataPref.contains(getString(R.string.sh_pending_order))) {
            val orderJson = dataPref.getString(getString(R.string.sh_pending_order), null)
            if (orderJson != null) {
                val order = gson.fromJson(orderJson, Order::class.java)
                initiateLivePendingOrder(order)

                when(order.statusId) {
                    1L -> startPendingOrderFragment()
                    2L -> startPendingOrderFragment()
                    3L -> startPendingOrderFragment()
                }
            }else startRootFragment()
        }else startRootFragment()
    }

    private fun initiateLivePendingOrder(order: Order) {
        val livePendingOrder = ViewModelProviders.of(this).get(PendingOrder::class.java)
        livePendingOrder.amount.value = order.amount
        livePendingOrder.gasType.value = order.gasType
        livePendingOrder.gasUnit.value = order.gasUnit
        livePendingOrder.quantity.value = order.quantity
        livePendingOrder.statusId.value = order.statusId
        livePendingOrder.truck.value = order.truck
    }

    private fun startRootFragment() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
            .replace(R.id.frameLayoutFragment, RootFragment())
            .commit()
    }

    private fun startPendingOrderFragment() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
            .replace(R.id.frameLayoutFragment, PendingOrderFragment())
            .commit()
    }

    private fun initUserLocationBtn() {
        userLocationBtn = findViewById(R.id.userLocationBtn)
        userLocationBtn.setOnClickListener { goToUserLocation() }
    }

    private fun isServiceOk(): Boolean {
        // check google service
        val apiAvailabilityInstance = GoogleApiAvailability.getInstance()
        val availability = apiAvailabilityInstance.isGooglePlayServicesAvailable(this)

        if (availability == ConnectionResult.SUCCESS) {
            return true
        }else {
            if (apiAvailabilityInstance.isUserResolvableError(availability)) {
                val dialog = apiAvailabilityInstance.getErrorDialog(this, availability, errorDialogRequest)
                dialog.show()
            }else {
                Toast.makeText(this, "You device can't make map request", Toast.LENGTH_SHORT).show()
            }
        }

        return false
    }

    private fun initMap() {
        getLocationPermission()
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getLocationPermission() {
        val deniedPermissions = mutableListOf<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission)
            }
        }

        if (deniedPermissions.size > 0) {
            ActivityCompat.requestPermissions(this, deniedPermissions.toTypedArray(), requestCode)
        }else {
            locationPermissionsGranted = true
            initUserLocationBtn()
            goToUserLocation()
        }
    }

    // This method is called when a user Allow or Deny our requested permissions. So it will help us to move forward if the permissions are granted
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == this.requestCode) {
            if (grantResults.isNotEmpty()) {
                var granted = true

                for (grants in grantResults) {
                    if (grants == PackageManager.PERMISSION_DENIED) {
                        granted = false
                    }
                }

                locationPermissionsGranted = granted
                if (locationPermissionsGranted) {
                    initUserLocationBtn()
                    goToUserLocation()
                }
            }
        }
    }

    // Callback when map ready
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (locationPermissionsGranted) {
            mMap.isMyLocationEnabled = true
        }

        try {
            // Customise the styling
            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.uber_map_style
                )
            )
        } catch (e: Resources.NotFoundException) {
            Log.e("FragmentActivity.TAG", "Error parsing style. Error: ", e)
        }


        //getLastLocation()
    }

    private fun goToUserLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            if (locationPermissionsGranted) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener {lc ->  
                        if (lc != null) {
                            val userLocation = LatLng(lc.latitude, lc.longitude)

                            val mapIcon = AppCompatResources.getDrawable(this, R.drawable.customlocation)!!.toBitmap()
                            if (!this::userMarker.isInitialized) {
                                userMarker = mMap.addMarker(
                                    MarkerOptions()
                                        .position(userLocation).title("Marker in Sydney")
                                        .icon(BitmapDescriptorFactory.fromBitmap(mapIcon))
                                )
                            }else userMarker.position = userLocation

                            moveCamera(userLocation)
                        }else Toast.makeText(this, "Unable to get location, Please check GPS", Toast.LENGTH_SHORT).show()
                    }
            }else Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
        }catch (e: SecurityException) {
            Log.v("ApiLog", "${e.message}")
        }
    }

    private fun markUserLocation(location: LatLng) {
        moveCamera(location)
    }

    private fun moveCamera(location: LatLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f))
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

    // request permission from user if they've no granted the permission
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            requestCode
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (locationPermissionsGranted) {
            if (isLocationEnabled()) {

                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient!!.requestLocationUpdates(
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
            driverRating.setRating(4.5f)
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
            driverRating.setRating(4.5f)
        }
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

    fun paymentClick(view: View) {
        var paymentIntent = Intent(this, PaymentActivity::class.java)
        startActivity(paymentIntent)
    }

    fun hideError(view: View) {
        val errorMsg: TextView = findViewById(R.id.errorMsg)
        errorMsg.visibility = View.INVISIBLE
    }
}
