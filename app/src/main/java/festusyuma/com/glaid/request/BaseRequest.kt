package festusyuma.com.glaid.request

import android.app.Activity
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import festusyuma.com.glaid.R
import festusyuma.com.glaid.defaultRetryPolicy
import org.json.JSONObject

open class BaseRequest(private val c: Activity): Authentication(c) {

    protected fun jsonObjectRequest(method: Int, url: String, data: JSONObject? = null, callback: (response: JSONObject) -> Unit) {
        val req = JsonObjectRequest(
            method,
            url,
            data,
            { response ->
                if (response.getInt("status") == 200) {
                    callback(response.getJSONObject("data"))
                }else showError(response.getString("message"))
            },
            { response->
                if (response.networkResponse != null) {
                    showError(c.getString(R.string.error_occurred))
                    response.printStackTrace()
                }else showError(c.getString(R.string.internet_error_msg))
            }
        )

        req.retryPolicy = defaultRetryPolicy
        req.tag = "order_request"
        queue.add(req)
    }

    protected fun jsonObjectRequestWithAuthorization(method: Int, url: String, data: JSONObject? = null, callback: (response: JSONObject) -> Unit) {
        getAuthentication { authorization ->
            val req = object : JsonObjectRequest(
                method,
                url,
                data,
                Response.Listener { response ->
                    if (response.getInt("status") == 200) {
                        callback(response.getJSONObject("data"))
                    }else showError(response.getString("message"))
                },
                Response.ErrorListener { response->
                    if (response.networkResponse != null) {
                        showError(c.getString(R.string.error_occurred))
                        response.printStackTrace()
                    }else showError(c.getString(R.string.internet_error_msg))
                }
            ){
                override fun getHeaders(): MutableMap<String, String> {
                    return authorization
                }
            }

            req.retryPolicy = defaultRetryPolicy
            req.tag = "order_request"
            queue.add(req)
        }
    }
}