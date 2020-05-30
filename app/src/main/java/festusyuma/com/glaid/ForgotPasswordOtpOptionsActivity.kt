package festusyuma.com.glaid

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_forgot_password_otp.*


class ForgotPasswordOtpOptionsActivity : AppCompatActivity() {

    lateinit var otpChoice : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_otp)

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
        if (otpChoice != "") {
            val getOtpIntent = Intent(this, ForgotPassOtpFinalScreenActivity::class.java)
            getOtpIntent.putExtra(EXTRA_FORGOT_PASSWORD_CHOICE, otpChoice)
            startActivity(getOtpIntent)
        }
    }
}
