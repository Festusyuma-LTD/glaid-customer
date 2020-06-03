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
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.model.CustomResponse


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
            val sharedPref = getSharedPreferences("auth_token", Context.MODE_PRIVATE)
            if (sharedPref.contains(getString(R.string.auth_key_name))) {

                val auth = sharedPref.getString(getString(R.string.auth_key_name), "")
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
                val res = gson.fromJson(response.toString(), CustomResponse::class.java)
                Log.v("ApiLog", "Response lass: $res")

                startActivity(Intent(this, MapsActivity::class.java))
                finishAffinity()
            },
            Response.ErrorListener {
                response ->

                val sharedPref = getSharedPreferences("auth_token", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    remove(getString(R.string.auth_key_name))
                    commit()
                }

                startActivity(Intent(this, MainActivity::class.java))
                Log.v("ApiLog", response.networkResponse.statusCode.toString())
                finishAffinity()
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
}
