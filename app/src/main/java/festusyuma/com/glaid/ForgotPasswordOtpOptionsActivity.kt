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
import festusyuma.com.glaid.requestdto.PasswordResetRequest
import org.json.JSONObject


class ForgotPasswordOtpOptionsActivity : AppCompatActivity() {

    lateinit var otpChoice : String
    private var operationRunning = false

    private lateinit var loadingCover: ConstraintLayout
    private lateinit var loadingAvi: AVLoadingIndicatorView
    private lateinit var errorMsg: TextView

    private lateinit var forgotPasswordIntroText: TextView
    private lateinit var getOtpInputLabel: TextView
    private lateinit var getOtpInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_otp)

        loadingCover = findViewById(R.id.loadingCoverConstraint)
        loadingAvi = loadingCover.findViewById(R.id.avi)
        errorMsg = findViewById(R.id.errorMsg)

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
        if (!operationRunning) {
            setLoading(true)

            val inp = getOtpInput.text.toString()
            val passwordResetRequest = PasswordResetRequest()
            if (otpChoice == "email") passwordResetRequest.email = inp else passwordResetRequest.tel = inp

            val queue = Volley.newRequestQueue(this)
            val resetJsonObject = JSONObject(gson.toJson(passwordResetRequest))

            val request = JsonObjectRequest(
                Request.Method.POST,
                Api.RESET_PASSWORD,
                resetJsonObject,
                Response.Listener {
                        response ->
                    if (response.getInt("status") == 200) {
                        val signUpIntent = Intent(this, ForgotPassOtpFinalScreenActivity::class.java)
                        signUpIntent.putExtra("resetRequest", passwordResetRequest)

                        startActivity(signUpIntent)
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

    private fun showError(msg: String) {
        errorMsg.text = msg
        errorMsg.visibility = View.VISIBLE
    }

    fun hideError(view: View) {
        errorMsg.visibility = View.INVISIBLE
    }
}
