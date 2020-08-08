package festusyuma.com.glaid

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
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
import festusyuma.com.glaid.request.LoadingAndErrorHandler
import festusyuma.com.glaid.requestdto.PasswordResetRequest
import org.json.JSONObject


class ForgotPasswordOtpOptionsActivity : AppCompatActivity() {

    private lateinit var otpChoice : String
    private lateinit var forgotPasswordIntroText: TextView
    private lateinit var getOtpInputLabel: TextView
    private lateinit var getOtpInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_otp)

        forgotPasswordIntroText = findViewById(R.id.forgotPasswordIntroText)
        getOtpInputLabel = findViewById(R.id.getOtpInputLabel)
        getOtpInput = findViewById(R.id.getOtpInput)

        val choice = intent.getStringExtra(EXTRA_FORGOT_PASSWORD_CHOICE)
        if (choice == null) finish() else otpChoice = choice
        formatInput()
    }

    private fun formatInput() {
        val inputLabel = if (otpChoice == "email") "Email" else "Phone number"
        forgotPasswordIntroText.text = getString(R.string.reset_password_intro_text).format(inputLabel)
        getOtpInputLabel.text = inputLabel

        if (otpChoice == "email") {
            getOtpInput.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            getOtpInput.inputType
            getOtpInput.setHint(R.string.email_input_label)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getOtpInput.setAutofillHints(getString(R.string.email_input_label))
            }
        }else {
            getOtpInput.inputType = InputType.TYPE_CLASS_PHONE
            getOtpInput.inputType
            getOtpInput.setHint(R.string.phone_number_input_label)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getOtpInput.setAutofillHints(getString(R.string.phone_number_input_label))
            }
        }
    }

    fun getOtpMethod(view: View) {
        val inp = getOtpInput.text.toString()
        val passwordResetRequest = PasswordResetRequest()
        if (otpChoice == "email") passwordResetRequest.email = inp else passwordResetRequest.tel = inp

        Authentication(this).resetPassword(passwordResetRequest) {
            val intent = Intent(this, ForgotPassOtpFinalScreenActivity::class.java)
            intent.putExtra("resetRequest", passwordResetRequest)
            LoadingAndErrorHandler(this).setLoading(false)
            startActivity(intent)
        }
    }
}
