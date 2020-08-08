package festusyuma.com.glaid.request

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import festusyuma.com.glaid.*
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.helpers.Dashboard
import festusyuma.com.glaid.requestdto.LoginRequest
import festusyuma.com.glaid.requestdto.UserRegistrationRequest
import org.json.JSONObject

open class Authentication(private val c: Activity): LoadingAndErrorHandler(c) {

    val queue: RequestQueue = Volley.newRequestQueue(c)
    private lateinit var authPref: SharedPreferences

    fun login(loginRequest: LoginRequest, callback: () -> Unit) {
        if (!operationRunning) {
            setLoading(true)

            val loginRequestJson = JSONObject(gson.toJson(loginRequest))
            val req = JsonObjectRequest(
                Request.Method.POST,
                Api.LOGIN,
                loginRequestJson,
                Response.Listener { response ->
                    if (response.getInt("status") == 200) {
                        val authKeyName = c.getString(R.string.cached_authentication)
                        val tokenKeyName = c.getString(R.string.sh_authorization)
                        authPref = c.getSharedPreferences(authKeyName, Context.MODE_PRIVATE)

                        val data = response.getJSONObject("data")
                        val serverToken = data.getString("token")
                        val userDetails = data.getJSONObject("user")

                        if (serverToken.isBlank()) {
                            showError("An error occurred")
                            setLoading(false)
                            return@Listener
                        }

                        auth.signInWithCustomToken(serverToken)
                            .addOnSuccessListener {

                                with (authPref.edit()) {
                                    putString(tokenKeyName, serverToken)
                                    commit()
                                }
                                Dashboard.store(c, userDetails)
                                callback()

                            }.addOnFailureListener { errorOccurred() }
                    }else {
                        showError(response.getString("message"))
                    }
                },
                Response.ErrorListener { response ->
                    if (response.networkResponse != null) {
                        showError(c.getString(R.string.error_occurred))
                        response.printStackTrace()
                    }else showError(c.getString(R.string.internet_error_msg))
                }
            )

            req.retryPolicy = defaultRetryPolicy
            req.tag = "authentication"
            queue.add(req)
        }
    }

    fun register(userRequest: UserRegistrationRequest, callback: () -> Unit) {
        if (!operationRunning) {
            setLoading(true)

            val userRequestJson = JSONObject(gson.toJson(userRequest))
            val req = JsonObjectRequest(
                Request.Method.POST,
                Api.REGISTER,
                userRequestJson,
                Response.Listener { response ->
                    if (response.getInt("status") == 200) {
                        callback()
                    }else showError(response.getString("message"))
                },
                Response.ErrorListener { response ->
                    if (response.networkResponse != null) {
                        showError(c.getString(R.string.error_occurred))
                        response.printStackTrace()
                    }else showError(c.getString(R.string.internet_error_msg))
                }
            )

            req.retryPolicy = defaultRetryPolicy
            req.tag = "authentication"
            queue.add(req)
        }
    }

    fun getAuthentication(callback: (authentication: MutableMap<String, String>) -> Unit) {
        val authKeyName = c.getString(R.string.cached_authentication)
        val tokenKeyName = c.getString(R.string.sh_authorization)

        authPref = c.getSharedPreferences(authKeyName, Context.MODE_PRIVATE)
        if (authPref.contains(tokenKeyName)) {
            auth.currentUser?.getIdToken(true)
                ?.addOnSuccessListener {
                    val token = it.token
                    val serverToken = authPref.getString(tokenKeyName, null)?: ""

                    if (token != null) callback(mutableMapOf(
                        "Authorization" to "Bearer $token",
                        "ServerAuthorization" to serverToken
                    ))
                }
                ?.addOnFailureListener{ logout() }
        }else logout()
    }

    fun logout() {
        val dataPref = c.getSharedPreferences(c.getString(R.string.sh_authorization), Context.MODE_PRIVATE)
        val authPref = c.getSharedPreferences(c.getString(R.string.cached_data), Context.MODE_PRIVATE)

        with(authPref.edit()) {
            clear()
            commit()
        }

        with(dataPref.edit()) {
            clear()
            commit()
        }

        c.startActivity(Intent(c, MainActivity::class.java))
        c.finishAffinity()
    }
}