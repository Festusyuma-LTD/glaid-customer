package festusyuma.com.glaid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
    }

    fun signUpMethod(view: View) {
        var signUpIntent = Intent(this, OneTimePasswordActivity::class.java)
        startActivity(signUpIntent)
    }
}
