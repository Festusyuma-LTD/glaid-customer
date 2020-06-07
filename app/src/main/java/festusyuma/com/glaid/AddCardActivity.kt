package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Constraints
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.helpers.Dashboard
import kotlinx.android.synthetic.main.activity_add_card.*
import org.json.JSONObject

class AddCardActivity : AppCompatActivity() {

    var token: String? = ""
    private var operationRunning = false
    private lateinit var expDateInput: EditText
    private lateinit var cardNoInput: EditText
    private lateinit var cvvInput: EditText
    private lateinit var addCardBtn: ConstraintLayout

    private lateinit var loadingCover: LinearLayout
    private lateinit var errorMsg: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)

        val sharedPref = getSharedPreferences("auth_token", Context.MODE_PRIVATE)
        if (sharedPref.contains(getString(R.string.auth_key_name))) {
            token = sharedPref.getString(getString(R.string.auth_key_name), token)
        }

        loadingCover = findViewById(R.id.loadingCover)
        errorMsg = findViewById(R.id.errorMsg)

        expDateInput = findViewById(R.id.expDateInput)
        cardNoInput = findViewById(R.id.cardNumberInput)
        cvvInput = findViewById(R.id.cvvInput)
        addCardBtn = findViewById(R.id.addCardBtn)
        addListeners()

        addCardBtn.setOnClickListener {
            if (!operationRunning) {
                setLoading(true)
                val queue = Volley.newRequestQueue(this)
                val loginRequest = validateLogin()
                loginRequest.tag = "add_card"

                queue.add(loginRequest)
            }
        }
    }

    private fun addListeners() {
        cardNoInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s != null) {
                    if (s.length >= 16) expDateInput.requestFocus()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (s.length >= 16) expDateInput.requestFocus()
                }
            }
        })

        expDateInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s != null) {
                    if (s.length >= 5) cvvInput.requestFocus()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (start == 1 && before == 0) expDateInput.append("/")
                if (start == 2 && before == 1) expDateInput.text.delete(1, 2)
                if (s != null) {
                    if (s.length >= 5) cvvInput.requestFocus()
                }
            }
        })

        cvvInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s != null) {
                    if (s.length >= 3) addCardBtn.requestFocus()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (s.length >= 3) addCardBtn.requestFocus()
                }
            }
        })
    }

    private fun validateLogin(): JsonObjectRequest {

        return object : JsonObjectRequest(
            Method.GET,
            Api.VALIDATE_TOKEN,
            null,
            Response.Listener { response ->
                setLoading(false)
            },
            Response.ErrorListener { response->
                if (response.networkResponse == null) showError(getString(R.string.internet_error_msg))
                logout()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $token"
                )
            }
        }
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

    fun logout() {
        val sharedPref = getSharedPreferences("auth_token", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(getString(R.string.auth_key_name))
            commit()
        }

        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}
