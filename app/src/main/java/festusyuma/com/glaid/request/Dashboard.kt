package festusyuma.com.glaid.request

import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import festusyuma.com.glaid.MainActivity
import festusyuma.com.glaid.R
import festusyuma.com.glaid.gson
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.model.CustomResponse

class Dashboard {
    fun get(token: String, context: Context) {
        val queue = Volley.newRequestQueue(context)
        val request = object : JsonObjectRequest(
            Method.GET,
            Api.DASHBOARD,
            null,
            Response.Listener {
                response ->
                val resJson = gson.toJson(response)
                val res = gson.fromJson(resJson, CustomResponse::class.java)
                Log.v("ApiLog", res.toString())
            },
            Response.ErrorListener {
                response ->

                val sharedPref = context.getSharedPreferences("auth_token", Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    //remove(context.getString(R.string.auth_key_name))
                    commit()
                }

                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                Log.v("ApiLog", response.networkResponse.statusCode.toString())
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $token"
                )
            }
        }

        queue.add(request)
    }
}