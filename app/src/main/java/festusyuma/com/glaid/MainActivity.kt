package festusyuma.com.glaid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun signUpWithMail(view: View){
        val signUpMailIntent = Intent(this, SignUpActivity::class.java)
        startActivity(signUpMailIntent)
    }

    fun signInMethod(view: View){
        val signInIntent = Intent(this, LogInActivity::class.java)
        startActivity(signInIntent)
    }
}
