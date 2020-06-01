package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.requestdto.LoginRequest
import kotlinx.android.synthetic.main.activity_forgot_pass_otp_final.*
import kotlinx.android.synthetic.main.activity_forgot_pass_otp_final.errorMsg
import kotlinx.android.synthetic.main.activity_forgot_pass_otp_final.loadingCover
import kotlinx.android.synthetic.main.activity_log_in.*
import org.json.JSONObject

class LogInActivity : AppCompatActivity() {

    private var operationRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
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
                        val homePageIntent = Intent(this, MapsActivity::class.java)
                        val sharedPref = getSharedPreferences("auth_token", Context.MODE_PRIVATE)
                        with (sharedPref.edit()) {
                            val data = response.getJSONObject("data")
                            val token = data.getString("token")

                            putString(getString(R.string.auth_key_name), token)
                            commit()
                        }

                        startActivity(homePageIntent)
                        finishAffinity()
                    }else showError(response.getString("message"))

                    setLoading(false)
                },
                Response.ErrorListener {
                    response ->
                    response.printStackTrace()
                    showError("An error occurred")
                    setLoading(false)
                }
            )

            queue.add(request)
        }
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            loadingCover.visibility = View.VISIBLE
            operationRunning = true
        }else {
            loadingCover.visibility = View.INVISIBLE
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
}
