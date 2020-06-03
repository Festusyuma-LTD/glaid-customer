package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
    }

    fun editBackBtnClick(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    fun logout(view: View) {
        val sharedPref = getSharedPreferences("auth_token", Context.MODE_PRIVATE)
        val dataSharedPref = getSharedPreferences("cached_data", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            commit()
        }

        with(dataSharedPref.edit()) {
            remove(getString(R.string.auth_key_name))
            clear()
            commit()
        }

        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}
