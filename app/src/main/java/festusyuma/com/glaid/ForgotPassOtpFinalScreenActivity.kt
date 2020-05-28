package festusyuma.com.glaid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_forgot_pass_otp_final.*

class ForgotPassOtpFinalScreenActivity : AppCompatActivity() {
    lateinit var otpChoice : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass_otp_final)

        otpChoice = intent.getStringExtra(EXTRA_FORGOT_PASSWORD_CHOICE)
        var otpText = if (otpChoice == "email") "Email" else "Phone number"
        optTextId.text = "A one-time pin has been sent to your $otpText. Please enter it below"
    }

    fun resetPasswordMethod(view: View){
        var resetPasswordIntent = Intent(this, ResetPasswordActivity::class.java);
        startActivity(resetPasswordIntent)
    }
}
