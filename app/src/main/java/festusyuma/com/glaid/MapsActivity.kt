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
import android.widget.ImageView
import android.widget.RatingBar
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.firestore.*
import com.google.gson.reflect.TypeToken
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import com.google.maps.model.DirectionsRoute
import festusyuma.com.glaid.helpers.capitalizeWords
import festusyuma.com.glaid.model.FSLocation
import festusyuma.com.glaid.model.GasType
import festusyuma.com.glaid.model.Order
import festusyuma.com.glaid.model.User
import festusyuma.com.glaid.model.fs.FSPendingOrder
import festusyuma.com.glaid.model.live.PendingOrder
import festusyuma.com.glaid.request.OrderRequests
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
    private lateinit var geoApiContext: GeoApiContext
    private lateinit var polyline: Polyline

    private lateinit var authPref: SharedPreferences
    private lateinit var dataPref: SharedPreferences
    private lateinit var livePendingOrder: PendingOrder

    //Listeners
    private lateinit var listener: ListenerRegistration
    private lateinit var driverLocationListener: ListenerRegistration

    //Api Requests
    private lateinit var orderRequests: OrderRequests

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
            val rating: RatingBar = drawer_header.findViewById(R.id.userRating)
            val user = gson.fromJson(userJson, User::class.java)

            fullNameTV.text = user.fullName
            emailTV.text = user.email
            rating.rating = user.rating.toFloat()
        }

        initGasTypeText()
        initRequests()
        if (isServiceOk()) initMap()
    }

    private fun initGasTypeText() {
        val dieselPriceFeed: TextView = findViewById(R.id.dieselPriceFeed)
        val gasPriceFeed: TextView = findViewById(R.id.gasPriceFeed)
        val typeToken = object: TypeToken<MutableList<GasType>>(){}.type
        val gasTypeJson = dataPref.getString(getString(R.string.sh_gas_type), null)

        if (gasTypeJson != null) {
            val gasTypes: List<GasType> = gson.fromJson(gasTypeJson, typeToken)
            val diesel = gasTypes.find { it.type == "diesel" }
            val gas = gasTypes.find { it.type == "gas" }

            dieselPriceFeed.text = getString(R.string.gas_price_text).format(
                diesel?.type?.capitalizeWords(), diesel?.price?.toFloat(), diesel?.unit
            )

            gasPriceFeed.text = getString(R.string.gas_price_text).format(
                gas?.type?.capitalizeWords(), gas?.price?.toFloat(), gas?.unit
            )
        }
    }

    override fun onResume() {
        super.onResume()

        if (firstLaunch) {
            firstLaunch = false
            return
        }

        if (livePendingOrder.id.value != null) startOrderStatusListener()
    }

    private fun initRequests() {
        orderRequests = OrderRequests(this)
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

        geoApiContext =
            GeoApiContext.Builder()
                .apiKey(getString(R.string.google_api_key))
                .build()
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
                updateMapWithStatusId(order.statusId)
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

        listener = locationRef.addSnapshotListener(this) { snapshot, e ->
            if (e != null) {
                Log.v(FIRE_STORE_LOG_TAG, "$e")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                if (!snapshot.metadata.isFromCache) {
                    val order = snapshot.toObject(FSPendingOrder::class.java)
                    if (order != null) {
                        if (order.status != livePendingOrder.statusId.value && order.status != null) {
                            Log.v(FIRE_STORE_LOG_TAG, "Listener ran")
                            updateMapWithStatusId(order.status)
                        }
                    }
                }
            }
        }
    }

    private fun startDriverLocationListener() {
        val driverId = livePendingOrder.driver.value?.id.toString()
        val locationRef =
            db.collection(getString(R.string.fs_user_locations))
                .document(driverId)

        driverLocationListener = locationRef.addSnapshotListener(this, MetadataChanges.INCLUDE) {
            snapshot, e ->

            if (e != null) {
                Log.v(FIRE_STORE_LOG_TAG, "$e")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val fsLocation = snapshot.toObject(FSLocation::class.java)!!
                updateDriverMarkerPosition(fsLocation)
            }
        }
    }

    private fun updateMapWithStatusId(statusId: Long?) {
        statusId?: return
        isOnTrip = statusId == OrderStatusCode.ON_THE_WAY

        when(statusId) {
            OrderStatusCode.PENDING -> startPendingOrderFragment()
            OrderStatusCode.DRIVER_ASSIGNED -> driverAssignedData()
            OrderStatusCode.ON_THE_WAY -> startTrackingDriver(statusId)
            else -> orderCompleted(statusId)
        }
    }

    private fun driverAssignedData() {
        orderRequests.getOrderDetails(livePendingOrder.id.value!!) {
            livePendingOrder.statusId.value = it.statusId
            livePendingOrder.driver.value = it.driver
            livePendingOrder.truck.value = it.truck

            updateLocalOrderStatus(it.statusId, it)
            startDriverAssignedFragment()
        }
    }

    private fun startTrackingDriver(statusId: Long) {
        livePendingOrder.statusId.value = statusId
        updateLocalOrderStatus(statusId)

        calculateDirections { addPolyLine(it) }
        startDriverLocationListener()
        startDriverAssignedFragment()
    }

    private fun orderCompleted(status: Long) {
        if (this::driverMarker.isInitialized) driverMarker.remove()
        removePolyLine()
        updateLocalOrderStatus(status)

        livePendingOrder.id.value = null
        livePendingOrder.amount.value = null
        livePendingOrder.gasType.value = null
        livePendingOrder.gasUnit.value = null
        livePendingOrder.quantity.value = null
        livePendingOrder.statusId.value = null
        livePendingOrder.driver.value = null
        livePendingOrder.truck.value = null

        startRootFragment()
        if (this::listener.isInitialized) listener.remove()
        if (this::driverLocationListener.isInitialized) driverLocationListener.remove()
    }

    private fun updateLocalOrderStatus(statusId: Long, order: Order? = null) {
        val typeToken = object: TypeToken<MutableList<Order>>(){}.type
        val ordersJson = dataPref.getString(getString(R.string.sh_orders), null)
        val orders = if (ordersJson != null) {
            gson.fromJson(ordersJson, typeToken)
        }else mutableListOf<Order>()

        orders.forEach {
            if (it.id == livePendingOrder.id.value) {
                it.statusId = statusId

                if (order != null) {
                    it.driver = order.driver
                    it.truck = order.truck
                }

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
        markDriverLocation()

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
        }

        startFragment()

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
                .scale(48, 72, false)

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
                updateDriverMarkerPosition(lc)
            }
        }
    }

    private fun updateDriverMarkerPosition(lc: FSLocation) {
        lc.geoPoint!!
        val location = LatLng(lc.geoPoint.latitude, lc.geoPoint.longitude)

        if (!this::driverMarker.isInitialized) {
            val mapIcon = AppCompatResources.getDrawable(this, R.drawable.truck_marker)!!
                .toBitmap()
                .scale(81, 81, false)

            driverMarker = gMap.addMarker(
                MarkerOptions()
                    .position(location).title("Driver")
                    .icon(BitmapDescriptorFactory.fromBitmap(mapIcon))
            )
        }else {
            driverMarker.position = location
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

    @SuppressLint("MissingPermission")
    private fun calculateDirections(callback: (route: DirectionsRoute) -> Unit) {
        val driverId = livePendingOrder.driver.value?.id?: return
        getUserLocation(driverId.toString()) {
            it.geoPoint!!
            val driverLocation = com.google.maps.model.LatLng(
                it.geoPoint.latitude,
                it.geoPoint.longitude
            )

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val userLocation = com.google.maps.model.LatLng(
                    location.latitude,
                    location.longitude
                )

                val directionsRequest = DirectionsApiRequest(geoApiContext)
                directionsRequest.alternatives(true)
                directionsRequest.origin(userLocation)
                directionsRequest.destination(driverLocation).setCallback(
                    object: PendingResult.Callback<DirectionsResult> {
                        override fun onFailure(e: Throwable?) {
                            Log.v(API_LOG_TAG, "Error getting directions ${e?.message}")
                        }

                        override fun onResult(result: DirectionsResult?) {
                            result?: return
                            if (result.routes.isNotEmpty()) callback(result.routes[0])
                        }

                    }
                )
            }
        }
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
                        .width(10f)

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
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
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
        if (this::listener.isInitialized){
            listener.remove()
            Log.v(FIRE_STORE_LOG_TAG, "Listener ended")
        }
    }
}
