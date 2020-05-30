package festusyuma.com.glaid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.requestdto.UserRegistrationRequest
import org.json.JSONObject

class OneTimePasswordActivity : AppCompatActivity() {

    private lateinit var userRequest: UserRegistrationRequest
    private var signUpRunning: Boolean = false
    private lateinit var loadingCover: LinearLayout
    private lateinit var errorCover: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_time_password)

        userRequest = intent.getSerializableExtra("userRequest") as UserRegistrationRequest
        loadingCover = findViewById(R.id.loadingCover)
        errorCover = findViewById(R.id.errorMsg)
    }

    fun completeSignUp(view: View) {
        if (!signUpRunning) {
            setLoading(true)

            val otpInput = findViewById<EditText>(R.id.otpInput)
            userRequest.otp = otpInput.text.toString()

            val queue = Volley.newRequestQueue(this)
            val userJsonObject = JSONObject(gson.toJson(userRequest))
            Log.v("ApiLog", "Worked")

            val request = JsonObjectRequest(
                Request.Method.POST,
                Api.REGISTER,
                userJsonObject,
                Response.Listener {
                    response ->
                    if (response.getInt("status") == 200) {
                        val intent = Intent(this, LogInActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
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
            signUpRunning = true
        }else {
            loadingCover.visibility = View.INVISIBLE
            signUpRunning = false
        }
    }

    private fun showError(errorMsg: String) {
        errorCover.text = errorMsg
        errorCover.visibility = View.VISIBLE
    }

    fun hideError(view: View) {
        errorCover.visibility = View.INVISIBLE
    }
}
