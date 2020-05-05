package festusyuma.com.glaid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPasswordActivity : AppCompatActivity() {
    var otpChoice: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        radioGroupForgotPassword.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId === R.id.emailId){
                otpChoice = "email"
            } else if (checkedId === R.id.phoneId){
                otpChoice = "phone"
            }
        }
    }
    fun getOtpTypeMethod(view: View){
        if (otpChoice !== "") {
            var getOtpIntent = Intent(this, ForgotPasswordOtpActivity::class.java)
            getOtpIntent.putExtra(EXTRA_FORGOT_PASSWORD_CHOICE, otpChoice)
            startActivity(getOtpIntent)
        } else {
            Toast.makeText(applicationContext, "please select one of the options", Toast.LENGTH_LONG).show()
        }
    }
}
