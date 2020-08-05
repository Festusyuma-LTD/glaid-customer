package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.reflect.TypeToken
import com.wang.avi.AVLoadingIndicatorView
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.helpers.Dashboard
import festusyuma.com.glaid.model.PaymentCards
import festusyuma.com.glaid.model.Wallet
import festusyuma.com.glaid.requestdto.PreferredPayment
import org.json.JSONObject
import java.text.NumberFormat

class PaymentActivity : AppCompatActivity() {

    private lateinit var dataPref: SharedPreferences

    private lateinit var walletAmountTV: TextView
    private val radios: MutableList<RadioButton> = mutableListOf()
    private lateinit var preferredPayment: String
    private lateinit var walletRadio: View

    private lateinit var loadingCover: ConstraintLayout
    private lateinit var loadingAvi: AVLoadingIndicatorView
    private lateinit var errorMsg: TextView
    private var operationRunning = false

    var token: String? = "" // Auth token
    private lateinit var queue: RequestQueue //volley

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        walletAmountTV = findViewById(R.id.walletAmount)

        loadingCover = findViewById(R.id.loadingCoverConstraint)
        loadingAvi = loadingCover.findViewById(R.id.avi)
        errorMsg = findViewById(R.id.errorMsg)

        queue = Volley.newRequestQueue(this)

        val authPref = getSharedPreferences(getString(R.string.cached_authentication), Context.MODE_PRIVATE)
        if (authPref.contains(getString(R.string.sh_authorization))) {
            token = authPref.getString(getString(R.string.sh_authorization), token)
        }
    }

    override fun onResume() {
        super.onResume()

        dataPref = getSharedPreferences(getString(R.string.cached_data), Context.MODE_PRIVATE)
        if (dataPref.contains(getString(R.string.sh_preferred_payment))) {
            val prefPayment = dataPref.getString(getString(R.string.sh_preferred_payment), "wallet")
            preferredPayment = prefPayment?: "wallet"
        }

        if (dataPref.contains(getString(R.string.sh_wallet))) {
            val numberFormatter = NumberFormat.getInstance()
            val walletJson = dataPref.getString(getString(R.string.sh_wallet), null)

            if (walletJson != null) {
                val wallet = gson.fromJson(walletJson, Wallet::class.java)
                walletAmountTV.text = getString(R.string.formatted_amount).format(numberFormatter.format(wallet.wallet))
            }else logout()
        }else logout()

        if (dataPref.contains(getString(R.string.sh_payment_cards))) {
            val cardsJSons = dataPref.getString(getString(R.string.sh_payment_cards), null)
            val typeToken = object: TypeToken<MutableList<PaymentCards>>(){}.type

            if (cardsJSons != null) {
                Log.v("ApiLog", "cards $cardsJSons")
                val cardsObj: MutableList<PaymentCards> = gson.fromJson(cardsJSons, typeToken)
                populateCards(cardsObj)
            }
        }
    }

    private fun populateCards(cards: MutableList<PaymentCards>) {
        val cardRadioGroup: LinearLayout = findViewById(R.id.preferredPaymentList)
        cardRadioGroup.removeAllViews()
        radios.clear()

        for (card in cards) {
            val cardRadioItem: View = LayoutInflater.from(this).inflate(R.layout.card_radio_item, ConstraintLayout(this))
            val cardRadioBtn: RadioButton = cardRadioItem.findViewById(R.id.cardRadioBtn)
            val deleteBtn: TextView = cardRadioItem.findViewById(R.id.deleteBtn)

            if (preferredPayment == card.id.toString()) cardRadioBtn.isChecked = true
            cardRadioBtn.text = getString(R.string.card_no).format(card.cardNo)


            cardRadioBtn.setOnClickListener {
                updatePreferredPayment(cardRadioBtn, card.id.toString())
            }

            deleteBtn.setOnClickListener { deleteCard(card.id, cardRadioItem) }

            radios.add(cardRadioBtn)
            cardRadioGroup.addView(cardRadioItem)
        }

        walletRadio = getDefaultRadio(PaymentType.WALLET, PaymentType.WALLET_TEXT)
        cardRadioGroup.addView(walletRadio)
        cardRadioGroup.addView(getDefaultRadio(PaymentType.CASH, PaymentType.CASH_TEXT))
    }

    private fun getDefaultRadio(value: String, title: String): View {
        val radioItem: View = LayoutInflater.from(this).inflate(R.layout.card_radio_item, ConstraintLayout(this))
        val deleteBtn: TextView = radioItem.findViewById(R.id.deleteBtn)
        val radioBtn: RadioButton = radioItem.findViewById(R.id.cardRadioBtn)

        radioBtn.text = title
        deleteBtn.visibility = View.GONE

        if (preferredPayment == value) radioBtn.isChecked = true

        radioBtn.setOnClickListener {
            updatePreferredPayment(radioBtn, value)
        }

        radios.add(radioBtn)
        return radioItem
    }

    private fun deleteCard(id: Long, cardRadioItem: View) {
        if (!operationRunning) {
            setLoading(true)
            val cardRadioGroup: LinearLayout = findViewById(R.id.preferredPaymentList)
            Log.v("ApiLog", "delete $id")

            val req = object : JsonObjectRequest(
                Method.GET,
                Api.removeCardUrl(id),
                null,
                Response.Listener { response ->
                    if (response.getInt("status") == 200) {
                        if (id.toString() == preferredPayment) {
                            selectWallet()
                        }

                        cardRadioGroup.removeView(cardRadioItem)
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

            req.tag = "remove_card"
            queue.add(req)
        }
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

                    with (dataPref.edit()) {
                        putString(getString(R.string.sh_payment_cards), paymentCards)
                        commit()
                    }
                }

                setLoading(false)
            },
            Response.ErrorListener { setLoading(false) }
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

    private fun selectWallet() {
        with(dataPref.edit()) {
            putString(getString(R.string.sh_preferred_payment), "wallet")
            commit()
        }

        for (rad in radios) {
            rad.isChecked = false
        }

        val walletRadioButton: RadioButton = walletRadio.findViewById(R.id.cardRadioBtn)
        walletRadioButton.isChecked = true
        preferredPayment = "wallet"
    }

    private fun updatePreferredPayment(radioBtn: RadioButton, value: String) {
        if (!operationRunning) {
            setLoading(true)
            val prefPayment = when (value) {
                PaymentType.WALLET -> PreferredPayment(PaymentType.WALLET)
                PaymentType.CASH -> PreferredPayment(PaymentType.CASH)
                else -> PreferredPayment(
                    "card",
                    value.toLong()
                )
            }

            val req = object : JsonObjectRequest(
                Method.POST,
                Api.SET_PREFERRED,
                JSONObject(gson.toJson(prefPayment)),
                Response.Listener { response ->
                    if (response.getInt("status") == 200) {
                        radioSelect(radioBtn)
                        preferredPayment = value

                        with(dataPref.edit()) {
                            putString(getString(R.string.sh_preferred_payment), value)
                            commit()
                        }

                    }else {
                        radioBtn.isChecked = false
                        showError(response.getString("message"))
                    }

                    setLoading(false)
                },
                Response.ErrorListener { response->
                    radioBtn.isChecked = false
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

            req.tag = "remove_card"
            queue.add(req)
        }
    }

    private fun radioSelect(selectedRadio: RadioButton) {
        for (rad in radios) {
            rad.isChecked = false
        }

        selectedRadio.isChecked = true
    }

    fun addCardClick(view: View) {
        val addCardIntent = Intent(this, AddCardActivity::class.java)
        startActivity(addCardIntent)
    }

    fun addFundsBtnOnClick(view: View) {
        val addCardIntent = Intent(this, AddFundsActivity::class.java)
        startActivity(addCardIntent)
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

    private fun logout() {
        val sharedPref = getSharedPreferences(getString(R.string.cached_authentication), Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(getString(R.string.sh_authorization))
            commit()
        }

        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}
