package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.helpers.Dashboard


class SplashActivity : AppCompatActivity() {

    private val handler = Handler()
    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        queue = Volley.newRequestQueue(this)
    }

    private val runnable = Runnable {

        if (!isFinishing) {
            val sharedPref = getSharedPreferences(getString(R.string.cached_authentication), Context.MODE_PRIVATE)
            if (sharedPref.contains(getString(R.string.sh_authorization))) {

                val auth = sharedPref.getString(getString(R.string.sh_authorization), "")
                if (auth != null) {
                    queue.add(dashboard(auth))
                }
            }else startActivity(Intent(applicationContext, CarouselActivity::class.java))
        }
    }

    private fun dashboard(token: String): JsonObjectRequest {

        return object : JsonObjectRequest(
            Method.GET,
            Api.DASHBOARD,
            null,
            Response.Listener {
                response ->
                Dashboard.store(this, response.getJSONObject("data"))

                startActivity(Intent(this, MapsActivity::class.java))
                finishAffinity()
            },
            Response.ErrorListener { response->
                if (response.networkResponse == null) {
                    startActivity(Intent(this, MapsActivity::class.java))
                    finishAffinity()
                }else {
                    Log.v("ApiLog", response.networkResponse.statusCode.toString())
                    logout()
                }
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $token"
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 1000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    fun logout() {
        val sharedPref = getSharedPreferences(getString(R.string.cached_authentication), Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(getString(R.string.sh_authorization))
            commit()
        }

        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}
