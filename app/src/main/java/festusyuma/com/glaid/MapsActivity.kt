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
import android.os.Handler
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
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.gson.reflect.TypeToken
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsRoute
import festusyuma.com.glaid.model.FSLocation
import festusyuma.com.glaid.model.Order
import festusyuma.com.glaid.model.Truck
import festusyuma.com.glaid.model.User
import festusyuma.com.glaid.model.fs.FSPendingOrder
import festusyuma.com.glaid.model.live.PendingOrder
import festusyuma.com.glaid.utilities.LatLngInterpolator
import festusyuma.com.glaid.utilities.MarkerAnimation

class MapsActivity :
    AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnCameraMoveStartedListener
{

    private var firstLaunch = true
    private var locationUpdate = false
    private var isOnTrip = false
    private var isCameraSticky = true
    private val errorDialogRequest = 9001

    private val updateInterval =  1000L
    private val fastestInterval = 1000L
    private val defaultZoom = 15.0f
    private val defaultTilt = 0f
    private val defaultBearing = 0f

    private val requestCode = 42
    private val permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private var locationPermissionsGranted = false

    private lateinit var userLocationBtn: ImageView
    private lateinit var gMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var userMarker: Marker
    private lateinit var driverMarker: Marker
    private lateinit var polyline: Polyline

    private lateinit var authPref: SharedPreferences
    private lateinit var dataPref: SharedPreferences
    private lateinit var livePendingOrder: PendingOrder
    private lateinit var listener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        livePendingOrder = ViewModelProviders.of(this).get(PendingOrder::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = locationCallback()

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

        livePendingOrder.id.observe(this, Observer { id ->
            if (id != null) startOrderStatusListener()
        })
    }

    override fun onResume() {
        super.onResume()

        if (firstLaunch) {
            firstLaunch = false
            return
        }

        if (livePendingOrder.id.value != null) startOrderStatusListener()
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
        }
    }

    private fun startFragment() {
        if (dataPref.contains(getString(R.string.sh_pending_order))) {
            val orderJson = dataPref.getString(getString(R.string.sh_pending_order), null)
            if (orderJson != null) {
                val order = gson.fromJson(orderJson, Order::class.java)
                initiateLivePendingOrder(order)
                when(order.statusId) {
                    1L -> startPendingOrderFragment()
                    2L -> startDriverAssignedFragment()
                    3L -> startOnTheWayFragment()
                }

                startOrderStatusListener()
            }else startRootFragment()
        }else startRootFragment()
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

            if (snapshot != null) {
                if (!snapshot.metadata.isFromCache) {
                    val order = snapshot.toObject(FSPendingOrder::class.java)
                    if (order != null) {
                        if (order.status != livePendingOrder.statusId.value) {
                            updateMapWithStatusId(order)
                        }
                    }
                }
            }
        }
    }

    private fun updateMapWithStatusId(order: FSPendingOrder) {
        val statusId = order.status
        isOnTrip = statusId == OrderStatusCode.ON_THE_WAY

        when(statusId) {
            OrderStatusCode.DRIVER_ASSIGNED -> driverAssignedData(order)
            OrderStatusCode.ON_THE_WAY -> startTrackingDriver()
            else -> {
                orderCompleted()
                if (this::driverMarker.isInitialized) driverMarker.remove()
                removePolyLine()
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
        updateLocalOrderStatus(order.status!!, driver, truck)
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

    private fun updateLocalOrderStatus(statusId: Long, driver: User? = null, truck: Truck? = null) {
        val typeToken = object: TypeToken<MutableList<Order>>(){}.type
        val ordersJson = dataPref.getString(getString(R.string.sh_orders), null)
        val orders = if (ordersJson != null) {
            gson.fromJson(ordersJson, typeToken)
        }else mutableListOf<Order>()

        orders.forEach {
            if (it.id == livePendingOrder.id.value) {
                it.statusId = statusId

                if (driver != null) it.driver = driver
                if (truck != null) it.truck = truck

                with(dataPref.edit()) {
                    putString(getString(R.string.sh_orders), gson.toJson(orders))
                    commit()
                }

                return@forEach
            }
        }
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
            /*if (isOnTrip) calculateDirections { addPolyLine(it) }*/
            goToUserLocation()
        }
    }

    private fun getZoom(): Float {
        return if (isOnTrip) 20.0f else defaultZoom
    }

    private fun getTilt(): Float {
        return if (isOnTrip) 45.0f else defaultTilt
    }

    private fun getBearing(): Float {
        return if (isOnTrip) userMarker.rotation else defaultBearing
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
                    if (!locationUpdate) startLocationUpdates()
                }
            }
        }
    }

    override fun onCameraMoveStarted(p0: Int) {
        isCameraSticky = false
    }

    // Callback when map ready
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        gMap.setOnCameraMoveStartedListener(this)

        if (locationPermissionsGranted) {
            gMap.uiSettings.isMyLocationButtonEnabled = false
            gMap.uiSettings.isMyLocationButtonEnabled = false
            if (!locationUpdate) startLocationUpdates()
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

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationUpdate = true
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = updateInterval
        locationRequest.fastestInterval = fastestInterval

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
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

        val mapIcon =
            AppCompatResources
                .getDrawable(this, R.drawable.customlocation)!!
                .toBitmap()
                .scale(64, 96, false)

        if (!this::userMarker.isInitialized) {
            userMarker = gMap.addMarker(
                MarkerOptions()
                    .position(userLocation).title("User")
                    .icon(BitmapDescriptorFactory.fromBitmap(mapIcon))
                    .rotation(lc.bearing)
                    .flat(true)
            )
        }else {
            MarkerAnimation.animateMarkerToGB(userMarker, userLocation, LatLngInterpolator.Spherical())
            userMarker.rotation = lc.bearing
        }

        val cameraPosition = CameraPosition(userLocation, getZoom(), getTilt(), getBearing())
        if (isCameraSticky) moveCamera(cameraPosition)
    }

    private fun goToUserLocation() {
        if (this::userMarker.isInitialized) {
            val cameraPosition = CameraPosition(userMarker.position, getZoom(), getTilt(), getBearing())
            moveCamera(cameraPosition)
            isCameraSticky = true
        }
    }

    private fun markDriverLocation() {
        val driverId = livePendingOrder.driver.value?.id
        if (driverId != null) {
            getUserLocation(driverId.toString()) {lc ->

                lc.geoPoint?: return@getUserLocation
                lc.bearing?: return@getUserLocation
                val driverLocation = LatLng(lc.geoPoint.latitude, lc.geoPoint.longitude)

                updateMarkerPosition(driverLocation, lc)
                /*moveCamera(driverLocation, 17f)*/
            }
        }
    }

    private fun updateMarkerPosition(driverLocation: LatLng, lc: FSLocation) {
        if (!this::driverMarker.isInitialized) {
            val mapIcon = AppCompatResources.getDrawable(this, R.drawable.truck_marker)!!.toBitmap()
            driverMarker = gMap.addMarker(
                MarkerOptions()
                    .position(driverLocation).title("Driver")
                    .icon(BitmapDescriptorFactory.fromBitmap(mapIcon))
            )
        }else {
            driverMarker.position = driverLocation
            driverMarker.rotation = lc.bearing!!
        }
    }

    private fun moveCamera(position: CameraPosition) {
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000, null)
    }

    // This will check if the user has turned on location from the setting
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun addPolyLine(route: DirectionsRoute) {
        if (this::gMap.isInitialized) {
            Handler(Looper.getMainLooper()).post {
                val decodedPathD = PolylineEncoding.decode(route.overviewPolyline.encodedPath)
                val decodedPath = decodedPathD.map { LatLng(it.lat, it.lng) }
                val polylineOptions =
                    PolylineOptions()
                        .addAll(decodedPath)
                        .color(R.color.polyLineColor)
                        .width(20f)

                if (this::polyline.isInitialized) polyline.remove()
                polyline = gMap.addPolyline(polylineOptions)
            }
        }
    }

    private fun removePolyLine() {
        if (this::polyline.isInitialized) polyline.remove()
    }

    private fun locationCallback(): LocationCallback {

        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult != null) {
                    val stickyCameraState = isCameraSticky
                    markUserLocation(locationResult.lastLocation)
                    isCameraSticky = stickyCameraState
                }
            }
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

    override fun onPause() {
        super.onPause()
        if (this::listener.isInitialized) listener.remove()
    }
}
