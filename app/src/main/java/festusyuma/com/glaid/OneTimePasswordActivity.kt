package festusyuma.com.glaid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.wang.avi.AVLoadingIndicatorView
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.requestdto.UserRegistrationRequest
import org.json.JSONObject

class OneTimePasswordActivity : AppCompatActivity() {

    private lateinit var userRequest: UserRegistrationRequest
    private var operationRunning: Boolean = false

    private lateinit var loadingCover: ConstraintLayout
    private lateinit var loadingAvi: AVLoadingIndicatorView
    private lateinit var errorMsg: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_time_password)

        userRequest = intent.getSerializableExtra("userRequest") as UserRegistrationRequest
        loadingCover = findViewById(R.id.loadingCoverConstraint)
        loadingAvi = loadingCover.findViewById(R.id.avi)
        errorMsg = findViewById(R.id.errorMsg)
    }

    fun completeSignUp(view: View) {
        if (!operationRunning) {
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
                        startActivity(intent)
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
            loadingAvi.show()
            operationRunning = true
        }else {
            loadingCover.visibility = View.GONE
            loadingAvi.hide()
            operationRunning = false
        }
    }

    private fun showError(errorMsg: String) {
        this.errorMsg.text = errorMsg
        this.errorMsg.visibility = View.VISIBLE
    }

    fun hideError(view: View) {
        errorMsg.visibility = View.INVISIBLE
    }
}
