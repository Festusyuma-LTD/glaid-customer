package festusyuma.com.glaid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.requestdto.UserRegistrationRequest
import org.json.JSONObject
import java.util.*

class SignUpActivity : AppCompatActivity() {

    private var signUpRunning = false
    private lateinit var loadingCover: LinearLayout
    private lateinit var errorCover: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        loadingCover = findViewById(R.id.loadingCover)
        errorCover = findViewById(R.id.errorMsg)
    }

    fun signUp(view: View) {
        if (!signUpRunning) {
            setLoading(true)
            val userRequest = getUserRequest()

            if (!hasError(userRequest)) {
                val queue = Volley.newRequestQueue(this)
                val userJsonObject = JSONObject(gson.toJson(userRequest))
                Log.v("ApiLog", "Worked")

                val request = JsonObjectRequest(
                    Request.Method.POST,
                    Api.REGISTER,
                    userJsonObject,
                    Response.Listener {
                        response ->
                        if (response.getInt("status") == 200) {
                            val signUpIntent = Intent(this, OneTimePasswordActivity::class.java)
                            signUpIntent.putExtra("userRequest", userRequest)

                            startActivity(signUpIntent)
                        }else showError(response.getString("message"))

                        setLoading(false)
                    },
                    Response.ErrorListener {
                        response ->
                        response.printStackTrace()
                        showError("Please check your internet")
                        setLoading(false)
                    }
                )

                queue.add(request)
            }else setLoading(false)
        }
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            loadingCover.visibility = View.VISIBLE
            signUpRunning = true
        }else {
            loadingCover.visibility = View.INVISIBLE
            signUpRunning = false
        }
    }

    private fun getUserRequest(): UserRegistrationRequest {
        return UserRegistrationRequest(
            findViewById<EditText>(R.id.fullNameInput).text.toString(),
            findViewById<EditText>(R.id.emailInput).text.toString(),
            findViewById<EditText>(R.id.telInput).text.toString(),
            findViewById<EditText>(R.id.passwordInput).text.toString()
        )
    }

    private fun hasError(userRequest: UserRegistrationRequest): Boolean {
        var error = false
        val verifyPassword = findViewById<EditText>(R.id.verifyPasswordInput)
        val fullNameError = findViewById<TextView>(R.id.fullNameInputError)
        val emailError = findViewById<TextView>(R.id.emailInputError)
        val telError = findViewById<TextView>(R.id.telInputError)
        val passwordError = findViewById<TextView>(R.id.passwordInputError)
        val verifyPasswordError = findViewById<TextView>(R.id.verifyPasswordInputError)
        val emailRegex = "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])"

        when {
            userRequest.fullName == "" -> {
                fullNameError.setText(R.string.field_empty)
                error = true
            }
            !userRequest.fullName.matches(Regex("^[a-zA-Z ]*$")) -> {
                fullNameError.setText(R.string.invalid_format)
                error = true
            }
            else -> fullNameError.text = ""
        }

        when {
            userRequest.email == "" -> {
                emailError.setText(R.string.field_empty)
                error = true
            }
            !userRequest.email.toLowerCase(Locale.ROOT).matches(Regex(emailRegex)) -> {
                emailError.setText(R.string.invalid_format)
                error = true
            }
            else -> emailError.text = ""
        }

        when {
            userRequest.tel == "" -> {
                telError.setText(R.string.field_empty)
                error = true
            }
            !userRequest.tel.matches(Regex("^[0-9]*$")) -> {
                telError.setText(R.string.invalid_format)
                error = true
            }else -> telError.text = ""
        }

        when {
            userRequest.password == "" -> {
                passwordError.setText(R.string.field_empty)
                error = true
            }
            !userRequest.password.matches(Regex("^(?=.*[0-9])(?=.*[a-z]).{6,}")) -> {
                passwordError.setText(R.string.password_invalid)
                error = true
            }
            else -> passwordError.text = ""
        }

        if (userRequest.password != verifyPassword.text.toString()) {
            verifyPasswordError.setText(R.string.password_does_not_match)
            error = true
        }else verifyPasswordError.text = ""

        return error
    }

    private fun showError(errorMsg: String) {
        errorCover.text = errorMsg
        errorCover.visibility = View.VISIBLE
    }

    fun hideError(view: View) {
        errorCover.visibility = View.INVISIBLE
    }
}
