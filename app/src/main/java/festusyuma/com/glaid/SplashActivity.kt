package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cloudinary.android.MediaManager
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.helpers.Dashboard
import festusyuma.com.glaid.request.DashboardRequest
import java.lang.Exception


class SplashActivity : AppCompatActivity() {

    private val splashDelayTimeZone: Long = 1000
    private lateinit var authPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            MediaManager.get()
        }catch (e: Exception) {
            MediaManager.init(this)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val authPrefName = getString(R.string.cached_authentication)
        val tokenKeyName = getString(R.string.sh_authorization)

        authPref = getSharedPreferences(authPrefName, Context.MODE_PRIVATE)
        if (authPref.contains(tokenKeyName)) {
            val auth = authPref.getString(tokenKeyName, null)
            if (auth != null) {
                DashboardRequest(this).getUserDetails {
                    startActivity(Intent(this, MapsActivity::class.java))
                    finishAffinity()
                }
            }else startCarouselActivity()
        }else startCarouselActivity()
    }

    private fun startCarouselActivity() {
        Handler().postDelayed({
            startActivity(Intent(this, CarouselActivity::class.java))
            finishAffinity()
        }, splashDelayTimeZone)
    }
}
