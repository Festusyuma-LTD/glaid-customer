package festusyuma.com.glaid

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_forgot_password_otp.*


class ForgotPasswordOtpActivity : AppCompatActivity() {
    lateinit var otpChioce : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_otp)

        otpChioce = intent.getStringExtra(EXTRA_FORGOT_PASSWORD_CHOICE)
        var otpText = if (otpChioce == "email") "Email" else "Phone number"
        println(":::::: $otpText :::::::")
        forgotPasswordText.text = "Please enter the $otpText associated with your account below"
        inputLabeId.text = otpText
        this.toggleInputFields()
        this.setConstrantOnButton()
    }

    private fun toggleInputFields(){
        if (otpChioce == "email") {
            emailInputId.visibility = View.VISIBLE
            numberInputId.visibility = View.GONE
        } else if (otpChioce == "phone") {
            emailInputId.visibility = View.GONE
            numberInputId.visibility = View.VISIBLE
        }
    }

    fun setConstrantOnButton(){
        val params = forgotOtpButton.layoutParams as ConstraintLayout.LayoutParams
        if (otpChioce == "email") {
            params.topToBottom = emailInputId.id
        } else if (otpChioce == "phone") {
            params.topToBottom = numberInputId.id
        }
    }
}
