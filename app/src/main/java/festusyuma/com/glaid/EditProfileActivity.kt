package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import festusyuma.com.glaid.model.User

class EditProfileActivity : AppCompatActivity() {

    lateinit var userDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val sharedPref = getSharedPreferences("cached_data", Context.MODE_PRIVATE)
        if (sharedPref.contains(getString(R.string.sh_user_details))) {

            val user = sharedPref.getString(getString(R.string.sh_user_details), null)
            if (user != null) {
                userDetails = gson.fromJson(user, User::class.java)
                populateDetails()
                Log.v("ApiLog", "Response lass: $user")
            }
        }else startActivity(Intent(applicationContext, MainActivity::class.java))
    }

    fun editBackBtnClick(view: View) = finish()

    private fun populateDetails() {
        val fullNameTV: TextView = findViewById(R.id.fullNameInput)
        val emailTV: TextView = findViewById(R.id.emailInput)
        val telTV: TextView = findViewById(R.id.telInput)


        fullNameTV.text = userDetails.fullName
        emailTV.text = userDetails.email
        telTV.text = userDetails.tel
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
