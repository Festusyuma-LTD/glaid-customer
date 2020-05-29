package festusyuma.com.glaid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import festusyuma.com.glaid.requestdto.UserRegistrationRequest

class OneTimePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_time_password)

        val userRequest = intent.getSerializableExtra("userRequest") as UserRegistrationRequest
        Log.v("ApiLog", userRequest.toString())


    }

    fun completeRegistration() {

    }
}
