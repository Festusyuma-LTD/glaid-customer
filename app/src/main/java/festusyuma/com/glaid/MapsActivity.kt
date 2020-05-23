package festusyuma.com.glaid

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        nav_view.itemIconTintList = null;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // fragment switching
        val rootFragment = RootFragment()
//        val detailsFragment = DetailsFragment()
        // fragment transaction to set root fragment on create
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.framelayoutFragment, rootFragment)
            commit()
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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupPermissions()
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        // Instantiates a new CircleOptions object and defines the center and radius
        val circleOptions = CircleOptions()
            .center(sydney)
            .radius(500.0)
            .strokeWidth(0.0f)
            .strokeColor(R.color.AppPrimaryColor)
            .fillColor(R.color.AppPrimaryColor)
            .clickable(true); // In meters
        val mapIcon = AppCompatResources.getDrawable(this, R.drawable.customlocation)!!.toBitmap()
        mMap.addMarker(
            MarkerOptions().position(sydney).title("Marker in Sydney")
                .icon(BitmapDescriptorFactory.fromBitmap(mapIcon))
        )
//        mMap.addMarker(
//            MarkerOptions().position(sydney).title("Marker in Sydney").icon(BitmapDescriptorFactory.fromBitmap(mapIcon))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15.0f))
        // Instantiates a new CircleOptions object and defines the center and radius

        mMap.addCircle(circleOptions)
    }
    fun toggleDrawerClick(view: View) {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }
    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("ACCESS_FINE_LOCATION", "Permission to use location denied")
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Permission to access the Device Location is required for this Map to work.")
                    .setTitle("Permission required")

                builder.setPositiveButton(
                    "OK"
                ) { dialog, id ->
                    Log.i("ACCESS_FINE_LOCATION", "Clicked")
                    makeRequest()
                }
                val dialog = builder.create()
                dialog.show()
            } else {
                makeRequest()
            }
        }
    }
    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
    }

}
