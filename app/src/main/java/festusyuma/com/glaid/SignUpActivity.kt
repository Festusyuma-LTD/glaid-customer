package festusyuma.com.glaid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.wang.avi.AVLoadingIndicatorView
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.request.Authentication
import festusyuma.com.glaid.requestdto.UserRegistrationRequest
import org.json.JSONObject
import java.util.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
    }

    fun signUp(view: View) {
        val userRequest = getUserRequest()
        if (!hasError(userRequest)) {
            Authentication(this).register(userRequest) {
                val intent = Intent(this, OneTimePasswordActivity::class.java)
                intent.putExtra("userRequest", userRequest)
                startActivity(intent)
            }
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
}
