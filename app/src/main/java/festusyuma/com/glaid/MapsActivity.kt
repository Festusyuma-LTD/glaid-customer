package festusyuma.com.glaid

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
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
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import festusyuma.com.glaid.helpers.Dashboard
import festusyuma.com.glaid.model.FSLocation
import festusyuma.com.glaid.model.Order
import festusyuma.com.glaid.model.Truck
import festusyuma.com.glaid.model.User
import festusyuma.com.glaid.model.fs.FSPendingOrder
import festusyuma.com.glaid.model.live.PendingOrder
import festusyuma.com.glaid.request.OrderRequests

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var firstLaunch = true
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
    private lateinit var driverMarker: Marker

    private lateinit var userLocationBtn: ImageView

    private lateinit var authPref: SharedPreferences
    private lateinit var dataPref: SharedPreferences
    private lateinit var livePendingOrder: PendingOrder
    private lateinit var listener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        nav_view.itemIconTintList = null;
        dataPref = getSharedPreferences(getString(R.string.cached_data), Context.MODE_PRIVATE)
        val userJson = dataPref.getString("userDetails", "null")

        if (userJson != null) {
            val fullNameTV: TextView = drawer_header.findViewById(R.id.fullName)
            val emailTV: TextView = drawer_header.findViewById(R.id.email)
            val user = gson.fromJson(userJson, User::class.java)

            fullNameTV.text = user.fullName
            emailTV.text = user.email
        }

        if (isServiceOk()) initMap()
        startFragment()
    }

    override fun onResume() {
        super.onResume()

        if (firstLaunch) {
            firstLaunch = false
            return
        }


        startOrderStatusListener()
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

    private fun startFragment() {
        if (dataPref.contains(getString(R.string.sh_pending_order))) {
            val orderJson = dataPref.getString(getString(R.string.sh_pending_order), null)
            if (orderJson != null) {
                val order = gson.fromJson(orderJson, Order::class.java)
                initiateLivePendingOrder(order)
                startOrderStatusListener()

                when(order.statusId) {
                    1L -> startPendingOrderFragment()
                    2L -> startDriverAssignedFragment()
                    3L -> startOnTheWayFragment()
                }
            }else startRootFragment()
        }else startRootFragment()
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
        }
    }

    private fun initiateLivePendingOrder(order: Order) {
        livePendingOrder = ViewModelProviders.of(this).get(PendingOrder::class.java)
        livePendingOrder.id.value = order.id
        livePendingOrder.amount.value = order.amount
        livePendingOrder.gasType.value = order.gasType
        livePendingOrder.gasUnit.value = order.gasUnit
        livePendingOrder.quantity.value = order.quantity
        livePendingOrder.statusId.value = order.statusId
        livePendingOrder.driver.value = order.driver
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

    private fun startDriverAssignedFragment() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
            .replace(R.id.frameLayoutFragment, DriverAssignedFragment())
            .commit()
    }

    private fun startOnTheWayFragment() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
            .replace(R.id.frameLayoutFragment, OrderOnTheWayFragment())
            .commit()
    }

    private fun initUserLocationBtn() {
        userLocationBtn = findViewById(R.id.userLocationBtn)
        userLocationBtn.setOnClickListener {
            getUserLocation { markUserLocation(it) }
            getUserLocation("festusyuma@gmail.com") {
                Log.v(FIRE_STORE_LOG_TAG, "$it")
            }
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
                }
            }
        }
    }

    // Callback when map ready
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (locationPermissionsGranted) {
            getUserLocation {markUserLocation(it)}

            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = false

            if (this::livePendingOrder.isInitialized) markDriverLocation()
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
    }

    private fun getUserLocation(listener: (lc: Location) -> Unit): Task<Location>? {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            if (locationPermissionsGranted) {
                if (isLocationEnabled()) {
                    return fusedLocationClient.lastLocation
                        .addOnSuccessListener {lc ->
                            if (lc != null) {
                                listener(lc)
                            }else Toast.makeText(this, "Unable to get location, Please check GPS", Toast.LENGTH_SHORT).show()
                        }
                }
             }else Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
        }catch (e: SecurityException) {
            Log.v("ApiLog", "${e.message}")
        }

        return null
    }

    private fun getUserLocation(uid: String, listener: (lc: FSLocation) -> Unit) {
        val locationRef =
            db.collection(getString(R.string.fs_user_locations))
                .document(uid)

        locationRef.get()
            .addOnSuccessListener {
                val lc = it.toObject(FSLocation::class.java)
                if (lc != null) listener(lc)
            }
    }

    private fun markUserLocation(lc: Location) {
        val userLocation = LatLng(lc.latitude, lc.longitude)

        val mapIcon = AppCompatResources.getDrawable(this, R.drawable.customlocation)!!.toBitmap()
        if (!this::userMarker.isInitialized) {
            userMarker = mMap.addMarker(
                MarkerOptions()
                    .position(userLocation).title("User")
                    .icon(BitmapDescriptorFactory.fromBitmap(mapIcon))
                    .rotation(lc.bearing)
            )
        }else {
            userMarker.position = userLocation
            userMarker.rotation = lc.bearing
        }

        moveCamera(userLocation)
    }

    private fun markDriverLocation() {
        val driverId = livePendingOrder.driver.value?.id
        if (driverId != null) {
            getUserLocation(driverId.toString()) {lc ->

                lc.geoPoint?: return@getUserLocation
                lc.bearing?: return@getUserLocation
                val driverLocation = LatLng(lc.geoPoint.latitude, lc.geoPoint.longitude)

                updateMarkerPosition(driverLocation, lc)
                moveCamera(driverLocation, 17f)
            }
        }
    }

    private fun updateMarkerPosition(driverLocation: LatLng, lc: FSLocation) {
        if (!this::driverMarker.isInitialized) {
            val mapIcon = AppCompatResources.getDrawable(this, R.drawable.truck_marker)!!.toBitmap()
            driverMarker = mMap.addMarker(
                MarkerOptions()
                    .position(driverLocation).title("Driver")
                    .icon(BitmapDescriptorFactory.fromBitmap(mapIcon))
            )
        }else {
            driverMarker.position = driverLocation
            driverMarker.rotation = lc.bearing!!
        }
    }

    private fun startOrderStatusListener() {
        Log.v(FIRE_STORE_LOG_TAG, "Listener started")
        val orderId = livePendingOrder.id.value?: return

        val locationRef =
            db.collection(getString(R.string.fs_pending_orders))
                .document(orderId.toString())

        listener = locationRef.addSnapshotListener(this, MetadataChanges.INCLUDE) { snapshot, e ->
            if (e != null) {
                Log.v(FIRE_STORE_LOG_TAG, "$e")
                return@addSnapshotListener
            }

            Log.v(FIRE_STORE_LOG_TAG, "Got here")
            if (snapshot != null) {
                Log.v(FIRE_STORE_LOG_TAG, "Got here too")
                if (!snapshot.metadata.isFromCache) {
                    Log.v(FIRE_STORE_LOG_TAG, "Got here defs")
                    val order = snapshot.toObject(FSPendingOrder::class.java)
                    if (order != null) {
                        Log.v(FIRE_STORE_LOG_TAG, "Got here inside")
                        if (order.status != livePendingOrder.statusId.value) {
                            when(order.status) {
                                OrderStatusCode.DRIVER_ASSIGNED -> driverAssignedData(order)
                                OrderStatusCode.ON_THE_WAY -> startTrackingDriver()
                                OrderStatusCode.DELIVERED -> orderCompleted()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun driverAssignedData(order: FSPendingOrder) {
        livePendingOrder.statusId.value = order.status

        val fsDriver = order.driver?: return
        val driver = User(
            fsDriver.email,
            fsDriver.fullName,
            fsDriver.tel,
            order.driverId
        )

        order.truck?: return
        val truck = Truck(
            order.truck.make,
            order.truck.model,
            order.truck.year,
            order.truck.color
        )

        livePendingOrder.driver.value = driver
        livePendingOrder.truck.value = truck
        startDriverAssignedFragment()
    }

    private fun startTrackingDriver() {
        startOnTheWayFragment()
    }

    private fun orderCompleted() {
        livePendingOrder.id.value = null
        livePendingOrder.amount.value = null
        livePendingOrder.gasType.value = null
        livePendingOrder.gasUnit.value = null
        livePendingOrder.quantity.value = null
        livePendingOrder.statusId.value = null
        livePendingOrder.driver.value = null
        livePendingOrder.truck.value = null

        startRootFragment()
        listener.remove()
    }

    private fun moveCamera(location: LatLng, zoom: Float = 15.0f) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
    }

    // This will check if the user has turned on location from the setting
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
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
            driverRating.rating = 4.5f
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
            driverRating.rating = 4.5f
        }
    }

    private fun closeDrawer() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

    }

    fun inviteFriendsClick(view: View) {
        closeDrawer()
        val intent = Intent(this, InviteFriendsActivity::class.java)
        startActivity(intent)
    }

    fun helpClick(view: View) {
        closeDrawer()
        val intent = Intent(this, HelpSupportActivity::class.java)
        startActivity(intent)
    }

    fun orderHistoryClick(view: View) {
        closeDrawer()
        val intent = Intent(this, OrderHistoryActivity::class.java)
        startActivity(intent)

    }

    fun paymentClick(view: View) {
        val paymentIntent = Intent(this, PaymentActivity::class.java)
        startActivity(paymentIntent)
    }

    fun hideError(view: View) {
        val errorMsg: TextView = findViewById(R.id.errorMsg)
        errorMsg.visibility = View.INVISIBLE
    }
}
