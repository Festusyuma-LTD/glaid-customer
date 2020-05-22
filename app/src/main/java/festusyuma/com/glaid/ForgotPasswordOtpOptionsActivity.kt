package festusyuma.com.glaid

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_forgot_password_otp.*


class ForgotPasswordOtpOptionsActivity : AppCompatActivity() {
    lateinit var otpChoice : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_otp)

        otpChoice = intent.getStringExtra(EXTRA_FORGOT_PASSWORD_CHOICE)
        var otpText = if (otpChoice == "email") "Email" else "Phone number"
//        println(":::::: $otpText :::::::")
        forgotPasswordText.text = "Please enter the $otpText associated with your account below"
        inputLabeId.text = otpText
        this.toggleInputFields()
        this.setConstrantOnButton()
    }

    private fun toggleInputFields(){
        if (otpChoice == "email") {
            emailInputId.visibility = View.VISIBLE
            numberInputId.visibility = View.GONE
        } else if (otpChoice == "phone") {
            emailInputId.visibility = View.GONE
            numberInputId.visibility = View.VISIBLE
        }
    }

    fun setConstrantOnButton(){
        val params = dividerLine.layoutParams as ConstraintLayout.LayoutParams
        if (otpChoice == "email") {
            params.topToBottom = emailInputId.id
            forgotOtpButton.text = "Send Email"
        } else if (otpChoice == "phone") {
            params.topToBottom = numberInputId.id
            forgotOtpButton.text = "Send Otp"
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
