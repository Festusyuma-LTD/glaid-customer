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
import festusyuma.com.glaid.request.Authentication
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
        val loginRequest = LoginRequest(
            emailInput.text.toString(),
            passwordInput.text.toString()
        )

        Authentication(this).login(loginRequest) {
            startActivity(Intent(this, MapsActivity::class.java))
            finishAffinity()
        }
    }

    fun logout() {
        Authentication(this).logout()
    }
}
