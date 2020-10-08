package festusyuma.com.glaid.request

import android.app.Activity
import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import festusyuma.com.glaid.*
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.helpers.Dashboard
import festusyuma.com.glaid.model.Order
import festusyuma.com.glaid.model.Wallet
import festusyuma.com.glaid.requestdto.OrderRequest
import festusyuma.com.glaid.requestdto.RatingRequest
import org.json.JSONObject
import java.lang.reflect.Method

class OrderRequests(private val c: Activity): BaseRequest(c) {

    fun createOrder(orderRequest: OrderRequest, callback: (response: JSONObject) -> Unit) {
        if (!operationRunning) {
            setLoading(true)

            val orderRequestJson = JSONObject(gson.toJson(orderRequest))
            getAuthentication { authorization ->
                val req = object : JsonObjectRequest(
                    Method.POST,
                    Api.CREATE_ORDER,
                    orderRequestJson,
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

    fun getOrderDetails(orderId: Long, callback: (order: Order) -> Unit) {
        getAuthentication { authorization ->
            val req = object : JsonObjectRequest(
                Method.GET,
                Api.orderDetails(orderId),
                null,
                Response.Listener { response ->
                    if (response.getInt("status") == 200) {
                        callback(Dashboard().convertOrderJSonToOrder(response.getJSONObject("data")))
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

    fun fetchAll(callback: (response: JSONObject) -> Unit) {
        getAuthentication { authorization ->
            val req = object : JsonObjectRequest(
                Method.GET,
                Api.GET_ORDERS,
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
            req.tag = "rate_driver"
            queue.add(req)
        }
    }

    fun cancelOrder(orderId: Long, callback: () -> Unit) {
        jsonObjectRequestWithAuthorization(Request.Method.GET, Api.cancelOrder(orderId), null)
        { data ->
            val newWalletAmount = data.getDouble("wallet")
            val dataPref = c.getSharedPreferences(c.getString(R.string.cached_data), Context.MODE_PRIVATE)
            val walletJson = dataPref.getString(c.getString(R.string.sh_wallet), null)

            if (walletJson != null) {
                val wallet = gson.fromJson(walletJson, Wallet::class.java)
                wallet.wallet = newWalletAmount

                with(dataPref.edit()) {
                    putString(c.getString(R.string.sh_wallet), gson.toJson(wallet))
                    commit()
                }
            }

            setLoading(false)
        }
    }
}