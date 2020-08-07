package festusyuma.com.glaid.request

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import festusyuma.com.glaid.*
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.helpers.Dashboard
import festusyuma.com.glaid.model.CustomResponse
import org.json.JSONObject

class DashboardRequest(private val c: Activity): Authentication(c) {

    fun getUserDetails(callback: () -> Unit) {
        getAuthentication { authorization ->
            val req = object : JsonObjectRequest(
                Method.GET,
                Api.DASHBOARD,
                null,
                Response.Listener { response ->
                    if (response.getInt("status") == 200) {
                        Dashboard.store(c, response.getJSONObject("data"))
                        callback()
                    }else showError(response.getString("message"))

                    setLoading(false)
                },

                Response.ErrorListener { response->
                    if (response.networkResponse != null) {
                        if (response.networkResponse.statusCode == 403) {
                            logout()
                        }else showError(ERROR_OCCURRED_MSG)
                    }else showError(CHECK_YOUR_INTERNET)

                    setLoading(false)
                }
            ){
                override fun getHeaders(): MutableMap<String, String> {
                    return authorization
                }
            }

            req.retryPolicy = defaultRetryPolicy
            req.tag = "dashboard"
            queue.add(req)
        }
    }
}