package festusyuma.com.glaid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.toolbox.Volley
import festusyuma.com.glaid.requestdto.UserRegistrationRequest

class OneTimePasswordActivity : AppCompatActivity() {

    private val userRequest = intent.getSerializableExtra("userRequest") as UserRegistrationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_time_password)
    }

    fun completeRegistration() {
        val queue = Volley.newRequestQueue(this)

    }
}
