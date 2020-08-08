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
import festusyuma.com.glaid.request.Authentication
import festusyuma.com.glaid.requestdto.UserRegistrationRequest
import org.json.JSONObject

class OneTimePasswordActivity : AppCompatActivity() {

    private lateinit var userRequest: UserRegistrationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_time_password)
        userRequest = intent.getSerializableExtra("userRequest") as UserRegistrationRequest
    }

    fun completeSignUp(view: View) {
        val otpInput = findViewById<EditText>(R.id.otpInput)
        userRequest.otp = otpInput.text.toString()

        Authentication(this).register(userRequest) {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }
}
