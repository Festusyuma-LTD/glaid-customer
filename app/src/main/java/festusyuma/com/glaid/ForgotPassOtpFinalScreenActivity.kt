package festusyuma.com.glaid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import festusyuma.com.glaid.request.Authentication
import festusyuma.com.glaid.request.LoadingAndErrorHandler
import festusyuma.com.glaid.requestdto.PasswordResetRequest
import org.json.JSONObject

class ForgotPassOtpFinalScreenActivity : AppCompatActivity() {

    private var operationRunning = false
    private lateinit var passwordResetRequest: PasswordResetRequest

    private lateinit var otpResetIntroText: TextView

    private lateinit var loadingCover: ConstraintLayout
    private lateinit var loadingAvi: AVLoadingIndicatorView
    private lateinit var errorMsg: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass_otp_final)

        loadingCover = findViewById(R.id.loadingCoverConstraint)
        loadingAvi = loadingCover.findViewById(R.id.avi)
        errorMsg = findViewById(R.id.errorMsg)

        otpResetIntroText = findViewById(R.id.otpResetIntroText)

        passwordResetRequest = intent.getSerializableExtra("resetRequest") as PasswordResetRequest

        val inputLabel = if (passwordResetRequest.email != null) "Email" else "Phone number"
        otpResetIntroText.text = getString(R.string.otp_reset_intro_text).format(inputLabel)
    }

    fun resetPasswordMethod(view: View){
        val otpInput: EditText = findViewById(R.id.otpInput)
        passwordResetRequest.otp = otpInput.text.toString()

        Authentication(this).validateOtp(passwordResetRequest) {
            val intent = Intent(this, ResetPasswordActivity::class.java);
            intent.putExtra("resetRequest", passwordResetRequest)
            LoadingAndErrorHandler(this).setLoading(false)
            startActivity(intent)
        }
    }
}
