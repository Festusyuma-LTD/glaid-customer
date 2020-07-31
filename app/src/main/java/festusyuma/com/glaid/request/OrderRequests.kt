package festusyuma.com.glaid.request

import android.app.Activity
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import festusyuma.com.glaid.CHECK_YOUR_INTERNET
import festusyuma.com.glaid.ERROR_OCCURRED_MSG
import festusyuma.com.glaid.defaultRetryPolicy
import festusyuma.com.glaid.gson
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.requestdto.RatingRequest
import org.json.JSONObject
import retrofit2.http.POST

class OrderRequests(private val c: Activity): Authentication(c) {

    private val queue = Volley.newRequestQueue(c)

    fun getOrderDetails(orderId: Long, callback: (response: JSONObject) -> Unit) {
        getAuthentication { authorization ->
            val req = object : JsonObjectRequest(
                Method.GET,
                Api.orderDetails(orderId),
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
            req.tag = "update_order_status"
            queue.add(req)
        }
    }

    fun rateDriver(ratingRequest: RatingRequest, callback: () -> Unit) {
        if (!operationRunning) {
            setLoading(true)

            val ratingRequestJsonObj = JSONObject(gson.toJson(ratingRequest))
            getAuthentication { authorization ->
                val req = object : JsonObjectRequest(
                    Method.POST,
                    Api.RATE_DRIVER,
                    ratingRequestJsonObj,
                    Response.Listener { response ->
                        if (response.getInt("status") == 200) {
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
                req.tag = "rate_driver"
                queue.add(req)
            }
        }
    }
}