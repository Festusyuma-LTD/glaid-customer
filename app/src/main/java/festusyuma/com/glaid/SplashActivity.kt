package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashActivity : AppCompatActivity() {
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    private val runnable = Runnable {
        if (!isFinishing) {
            val sharedPref = getSharedPreferences("auth_token", Context.MODE_PRIVATE)
            if (sharedPref.contains(getString(R.string.auth_key_name))) {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            }else startActivity(Intent(applicationContext, CarouselActivity::class.java))

            finishAffinity()
        }
    }
    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 2000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }
}
