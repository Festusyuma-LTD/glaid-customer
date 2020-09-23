package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import festusyuma.com.glaid.model.Address
import festusyuma.com.glaid.model.User
import festusyuma.com.glaid.request.UserRequests
import java.lang.Exception

class EditProfileActivity : AppCompatActivity() {

    private lateinit var userRequests: UserRequests
    private lateinit var userDetails: User
    private var homeAddress: Address? = null
    private var businessAddress: Address? = null
    private val imageUploadCode = 2

    private lateinit var profileImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val dataPref = getSharedPreferences(getString(R.string.cached_data), Context.MODE_PRIVATE)
        userRequests = UserRequests(this)
        MediaManager.init(this);

        if (dataPref.contains(getString(R.string.sh_user_details))) {

            val user = dataPref.getString(getString(R.string.sh_user_details), null)
            if (user != null) {
                userDetails = gson.fromJson(user, User::class.java)
                val homeAddressJson = dataPref.getString(getString(R.string.sh_home_address), null)
                homeAddress = if (homeAddressJson != null) {
                    gson.fromJson(homeAddressJson, Address::class.java)
                }else null

                val businessAddressJson = dataPref.getString(getString(R.string.sh_business_address), null)
                businessAddress = if (businessAddressJson != null) {
                    gson.fromJson(businessAddressJson, Address::class.java)
                }else null

                populateDetails()
            }
        }else startActivity(Intent(applicationContext, MainActivity::class.java))
    }

    fun editBackBtnClick(view: View) = finish()

    private fun populateDetails() {
        val fullNameTV: TextView = findViewById(R.id.fullNameInput)
        val emailTV: TextView = findViewById(R.id.emailInput)
        val telTV: TextView = findViewById(R.id.telInput)
        val homeAddressTV: TextView = findViewById(R.id.homeAddressInput)
        val workAddressTV: TextView = findViewById(R.id.workAddressInput)
        profileImage = findViewById(R.id.profileImage)
        profileImage.setOnClickListener{ updateProfileImage() }

        fullNameTV.text = userDetails.fullName
        emailTV.text = userDetails.email
        telTV.text = userDetails.tel
        homeAddressTV.text = homeAddress?.address
        workAddressTV.text = businessAddress?.address
    }

    fun logout(view: View) {
        val sharedPref = getSharedPreferences(getString(R.string.cached_authentication), Context.MODE_PRIVATE)
        val dataSharedPref = getSharedPreferences(getString(R.string.cached_data), Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            commit()
        }

        with(dataSharedPref.edit()) {
            remove(getString(R.string.sh_authorization))
            clear()
            commit()
        }

        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun updateProfileImage() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, imageUploadCode)
    }

    private fun setProfileImage(url: String) {
        Picasso
            .get()
            .load(url)
            .placeholder(R.mipmap.profileimage)
            .error(R.mipmap.profileimage)
            .fit()
            .into(profileImage, object: Callback {
                override fun onSuccess() { userRequests.setLoading(false) }
                override fun onError(e: Exception?) { userRequests.showError("Error displaying picture") }
            })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            imageUploadCode -> {
                if (data != null) {
                    val imageUri = data.data?: return
                    userRequests.uploadImage(imageUri) {
                        setProfileImage(it)
                    }
                }
            }
            else -> return
        }
    }
}
