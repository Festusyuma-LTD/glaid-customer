package festusyuma.com.glaid.request

import android.app.Activity
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import festusyuma.com.glaid.CHECK_YOUR_INTERNET
import festusyuma.com.glaid.ERROR_OCCURRED_MSG
import festusyuma.com.glaid.defaultRetryPolicy
import festusyuma.com.glaid.helpers.Api
import org.json.JSONObject

class GasRequests(c: Activity): Authentication(c) {

    fun getGasType(gasType: String, callback: (data: JSONObject) -> Unit) {
        val url = if (gasType == "diesel") Api.GET_DIESEL_LIST else Api.GET_GAS_LIST

        getAuthentication { authorization ->
            val req = object : JsonObjectRequest(
                Method.GET,
                url,
                null,
                Response.Listener { response ->
                    if (response.getInt("status") == 200) {
                        callback(response.getJSONObject("data"))
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
            req.tag = "gas_request"
            queue.add(req)
        }
    }
}