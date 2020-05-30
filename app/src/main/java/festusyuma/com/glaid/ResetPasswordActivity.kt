package festusyuma.com.glaid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.requestdto.PasswordResetRequest
import kotlinx.android.synthetic.main.activity_forgot_pass_otp_final.*
import kotlinx.android.synthetic.main.activity_forgot_pass_otp_final.errorMsg
import kotlinx.android.synthetic.main.activity_forgot_pass_otp_final.loadingCover
import kotlinx.android.synthetic.main.activity_reset_password.*
import org.json.JSONObject

class ResetPasswordActivity : AppCompatActivity() {

    private var operationRunning = false
    private lateinit var passwordResetRequest: PasswordResetRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        passwordResetRequest = intent.getSerializableExtra("resetRequest") as PasswordResetRequest
    }

    fun resetPassword(view: View) {
        if (!operationRunning) {
            setLoading(true)
            passwordResetRequest.newPassword = passwordInput.text.toString()

            if (!hasError()) {
                val queue = Volley.newRequestQueue(this)
                val resetJsonObject = JSONObject(gson.toJson(passwordResetRequest))

                val request = JsonObjectRequest(
                    Request.Method.POST,
                    Api.RESET_PASSWORD,
                    resetJsonObject,
                    Response.Listener {
                        response ->
                        if (response.getInt("status") == 200) {
                            val intent = Intent(this, LogInActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        }else showError(response.getString("message"))

                        setLoading(false)
                    },
                    Response.ErrorListener {
                        response ->
                        response.printStackTrace()
                        showError("An error occurred")
                        setLoading(false)
                    }
                )

                queue.add(request)
            }else setLoading(false)
        }
    }

    private fun hasError(): Boolean {
        var error = false

        when {
            passwordResetRequest.newPassword == null -> {
                passwordInputError.setText(R.string.field_empty)
                error = true
            }
            passwordResetRequest.newPassword == "" -> {
                passwordInputError.setText(R.string.field_empty)
                error = true
            }
            !passwordResetRequest.newPassword?.matches(Regex("^(?=.*[0-9])(?=.*[a-z]).{6,}"))!! -> {
                passwordInputError.setText(R.string.password_invalid)
                error = true
            }
            else -> passwordInputError.text = ""
        }

        if (passwordResetRequest.newPassword != verifyPasswordInput.text.toString()) {
            verifyPasswordInputError.setText(R.string.password_does_not_match)
            error = true
        }else verifyPasswordInputError.text = ""

        return error
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            loadingCover.visibility = View.VISIBLE
            operationRunning = true
        }else {
            loadingCover.visibility = View.INVISIBLE
            operationRunning = false
        }
    }

    private fun showError(msg: String) {
        errorMsg.text = msg
        errorMsg.visibility = View.VISIBLE
    }

    fun hideError(view: View) {
        errorMsg.visibility = View.INVISIBLE
    }
}
