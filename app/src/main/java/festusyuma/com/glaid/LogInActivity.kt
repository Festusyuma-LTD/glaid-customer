package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.wang.avi.AVLoadingIndicatorView
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.helpers.Dashboard
import festusyuma.com.glaid.requestdto.LoginRequest
import org.json.JSONObject

class LogInActivity : AppCompatActivity() {

    private var operationRunning = false

    private lateinit var loadingCover: ConstraintLayout
    private lateinit var loadingAvi: AVLoadingIndicatorView
    private lateinit var errorMsg: TextView

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        loadingCover = findViewById(R.id.loadingCoverConstraint)
        loadingAvi = loadingCover.findViewById(R.id.avi)
        errorMsg = findViewById(R.id.errorMsg)

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
    }

    fun forgotPasswordMethod(view: View) {
        val forgotPasswordIntent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(forgotPasswordIntent)
    }

    fun signInMethod(view: View) {
        if (!operationRunning) {
            setLoading(true)

            val loginRequest = LoginRequest(
                emailInput.text.toString(),
                passwordInput.text.toString()
            )

            val queue = Volley.newRequestQueue(this)
            val loginRequestJson = JSONObject(gson.toJson(loginRequest))

            val request = JsonObjectRequest(
                Request.Method.POST,
                Api.LOGIN,
                loginRequestJson,
                Response.Listener {
                    response ->
                    if (response.getInt("status") == 200) {
                        val sharedPref = getSharedPreferences("auth_token", Context.MODE_PRIVATE)
                        val data = response.getJSONObject("data")
                        val serverToken = data.getString("token")

                        auth.signInWithCustomToken(serverToken)
                            .addOnSuccessListener {res->
                                val user = res.user

                                user?.getIdToken(true)
                                    ?.addOnSuccessListener {tokenRes ->
                                        val token = tokenRes.token
                                        if (token != null) {
                                            with (sharedPref.edit()) {
                                                putString(getString(R.string.auth_key_name), token)
                                                commit()
                                            }

                                            queue.add(dashboard(token))
                                        }else errorOccurred()
                                    }
                                    ?.addOnFailureListener { errorOccurred() }
                            }.addOnFailureListener { errorOccurred() }
                    }else {
                        setLoading(false)
                        showError(response.getString("message"))
                    }
                },
                Response.ErrorListener { response ->
                    if (response.networkResponse != null) {
                        showError(getString(R.string.error_occurred))
                        response.printStackTrace()
                    }else showError(getString(R.string.internet_error_msg))

                    setLoading(false)
                }
            )

            queue.add(request)
        }
    }

    private fun errorOccurred(message: String? = null) {
        setLoading(false)
        showError(message?: "An error occurred")
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            loadingCover.visibility = View.VISIBLE
            operationRunning = true
        }else {
            loadingCover.visibility = View.GONE
            operationRunning = false
        }
    }

    private fun showError(msg: String) {
        errorMsg.text = msg
        errorMsg.visibility = View.VISIBLE
    }

    fun hideError(view: View) {
        errorMsg.visibility = View.INVISIBLE
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
                Log.v("ApiLog", response.networkResponse.statusCode.toString())
                logout()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $token"
                )
            }
        }
    }

    fun logout() {
        val sharedPref = getSharedPreferences("auth_token", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(getString(R.string.auth_key_name))
            commit()
        }

        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}
