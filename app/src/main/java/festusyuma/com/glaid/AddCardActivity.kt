package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.wang.avi.AVLoadingIndicatorView
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.helpers.Dashboard
import org.json.JSONObject

class AddCardActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var dataSharedPref: SharedPreferences
    var token: String? = "" // Auth token
    private var operationRunning = false

    // views
    private lateinit var expDateInput: EditText
    private lateinit var cardNoInput: EditText
    private lateinit var cvvInput: EditText
    private lateinit var addCardBtn: ConstraintLayout

    private lateinit var loadingCover: ConstraintLayout
    private lateinit var loadingAvi: AVLoadingIndicatorView
    private lateinit var errorMsg: TextView

    //paystack
    private lateinit var card: Card

    //Volley
    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)

        PaystackSdk.initialize(this)
        sharedPref = getSharedPreferences("auth_token", Context.MODE_PRIVATE)
        dataSharedPref = getSharedPreferences("cached_data", Context.MODE_PRIVATE)
        if (sharedPref.contains(getString(R.string.auth_key_name))) {
            token = sharedPref.getString(getString(R.string.auth_key_name), token)
        }

        loadingCover = findViewById(R.id.loadingCoverConstraint)
        loadingAvi = loadingCover.findViewById(R.id.avi)
        errorMsg = findViewById(R.id.errorMsg)

        expDateInput = findViewById(R.id.expDateInput)
        cardNoInput = findViewById(R.id.cardNumberInput)
        cvvInput = findViewById(R.id.cvvInput)
        addCardBtn = findViewById(R.id.addCardBtn)
        addListeners()

        addCardBtn.setOnClickListener {
            if (!operationRunning) {
                setLoading(true)
                queue = Volley.newRequestQueue(this)
                //todo validate card is entered
                validateLogin()
            }
        }
    }

    private fun addListeners() {
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

    private fun validateLogin() {

        val req = object : JsonObjectRequest(
            Method.GET,
            Api.VALIDATE_TOKEN,
            null,
            Response.Listener { response ->
                if (response.getInt("status") == 200) {
                    val cardNo = cardNoInput.text.toString()
                    val expMonth = expDateInput.text.substring(0, 2).toInt()
                    val expYear = expDateInput.text.substring(3, 5).toInt()
                    val cvv = cvvInput.text.toString()

                    card = Card(
                        cardNo,
                        expMonth,
                        expYear,
                        cvv
                    )

                    saveCardInit()
                }
            },
            Response.ErrorListener { response->
                if (response.networkResponse == null) showError(getString(R.string.internet_error_msg)) else {
                    if (response.networkResponse.statusCode == 403) {
                        logout()
                    }else showError(getString(R.string.api_error_msg))
                }

                setLoading(false)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $token"
                )
            }
        }

        req.tag = "add_card"
        queue.add(req)
    }

    private fun saveCardInit() {

        val req = object : JsonObjectRequest(
            Method.GET,
            Api.ADD_CARD_INIT,
            null,
            Response.Listener { response ->
                if (response.getInt("status") == 200) {
                    val accessCode = response.getString("data")
                    paystackChargeCard(accessCode)
                }else showError(response.getString("message"))
            },
            Response.ErrorListener { response->
                if (response.networkResponse == null) showError(getString(R.string.internet_error_msg)) else {
                    if (response.networkResponse.statusCode == 403) {
                        logout()
                    }else showError(getString(R.string.api_error_msg))
                }

                setLoading(false)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $token"
                )
            }
        }

        req.tag = "add_card"
        queue.add(req)
    }

    private fun paystackChargeCard(accessCode: String) {
        val charge = Charge()
        charge.card = card
        charge.accessCode = accessCode

        PaystackSdk.chargeCard(this, charge, object : Paystack.TransactionCallback {
            override fun onSuccess(transaction: Transaction?) {
                if (transaction != null) {
                    saveCard(transaction.reference)
                    Log.v("ApiLog", "success")
                }
            }

            override fun beforeValidate(transaction: Transaction?) {
                if (transaction != null) Log.v("ApiLog", "validate")
            }

            override fun onError(error: Throwable?, transaction: Transaction?) {
                showError(getString(R.string.api_error_msg))
                setLoading(false)
                if (transaction != null) Log.v("ApiLog", transaction.toString())
            }
        })
    }

    private fun saveCard(reference: String) {
        val data = mapOf<String, String>(
            "cardNo" to card.number,
            "reference" to reference
        )
        val reqData = JSONObject(gson.toJson(data))

        val req = object : JsonObjectRequest(
            Method.POST,
            Api.ADD_CARD,
            reqData,
            Response.Listener { response ->
                if (response.getInt("status") == 200) {
                    updateCardsList()
                }else showError(response.getString("message"))

                setLoading(false)
            },
            Response.ErrorListener { response->
                if (response.networkResponse == null) showError(getString(R.string.internet_error_msg)) else {
                    if (response.networkResponse.statusCode == 403) {
                        logout()
                    }else showError(getString(R.string.api_error_msg))
                }

                setLoading(false)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $token"
                )
            }
        }

        req.tag = "add_card"
        queue.add(req)
    }

    private fun updateCardsList() {
        val req = object : JsonObjectRequest(
            Method.GET,
            Api.GET_CARDS_LIST,
            null,
            Response.Listener { response ->
                if (response.getInt("status") == 200) {
                    val dashboard = Dashboard()
                    val paymentCards = gson.toJson(dashboard.getPaymentCards(response.getJSONArray("data")))

                    with (dataSharedPref.edit()) {
                        putString(getString(R.string.sh_payment_cards), paymentCards)
                        commit()
                    }
                    finish()
                }else showError(response.getString("message"))

                setLoading(false)
            },
            Response.ErrorListener { response->
                if (response.networkResponse == null) showError(getString(R.string.internet_error_msg)) else {
                    if (response.networkResponse.statusCode == 403) {
                        logout()
                    }else showError(getString(R.string.api_error_msg))
                }

                setLoading(false)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $token"
                )
            }
        }

        req.tag = "add_card"
        queue.add(req)
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            loadingCover.visibility = View.VISIBLE
            loadingAvi.show()
            operationRunning = true
        }else {
            loadingCover.visibility = View.GONE
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
