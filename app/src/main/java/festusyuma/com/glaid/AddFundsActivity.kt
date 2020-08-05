package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.reflect.TypeToken
import com.wang.avi.AVLoadingIndicatorView
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.model.PaymentCards
import festusyuma.com.glaid.model.Wallet
import festusyuma.com.glaid.requestdto.FundWallet
import kotlinx.android.synthetic.main.activity_add_funds.*
import org.json.JSONObject
import java.lang.Exception

class AddFundsActivity : AppCompatActivity() {

    private lateinit var authPref: SharedPreferences
    private var token: String? = ""

    private lateinit var dataPref: SharedPreferences
    private var operationRunning = false

    private lateinit var loadingCover: ConstraintLayout
    private lateinit var loadingAvi: AVLoadingIndicatorView
    private lateinit var errorMsg: TextView

    private lateinit var amountInput: EditText
    private lateinit var cardInput: TextView
    private lateinit var cardsListCover: ScrollView
    private lateinit var cardsList: LinearLayout
    private lateinit var selectCardButton: TextView
    private lateinit var noCardText: TextView
    private lateinit var clickToClose: TextView

    private val cards: MutableList<PaymentCards> = mutableListOf()
    private var cardId: Long? = null

    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_funds)
        initViews()

        selectCardButton.setOnClickListener { cardsListCover.visibility = View.VISIBLE }
        clickToClose.setOnClickListener { cardsListCover.visibility = View.GONE }

        authPref = getSharedPreferences(getString(R.string.cached_authentication), Context.MODE_PRIVATE)
        if (authPref.contains(getString(R.string.sh_authorization))) {
            token = authPref.getString(getString(R.string.sh_authorization), token)
        }

        dataPref = getSharedPreferences(getString(R.string.cached_data), Context.MODE_PRIVATE)
        if (dataPref.contains(getString(R.string.sh_wallet))) {
            val cardsJSons = dataPref.getString(getString(R.string.sh_payment_cards), null)
            val typeToken = object: TypeToken<MutableList<PaymentCards>>(){}.type

            if (cardsJSons != null) {
                val cardsObj: MutableList<PaymentCards> = gson.fromJson(cardsJSons, typeToken)
                for (c in cardsObj) cards.add(c)

                populateCards()
            }
        }

        addFundsBtn.setOnClickListener {
            if (!operationRunning) {
                queue = Volley.newRequestQueue(this)

                if (cardId != null) {
                    setLoading(true)
                    fundWallet()
                }else showError("Card not selected")
            }
        }
    }

    private fun fundWallet() {
        var dmAmount: Double = 0.0
        val strAmount = amountInput.text.toString()
        var hasError = false

        if (strAmount.isEmpty()) {
            showError("Please enter amount")
            hasError = true
        }else {
            try {
                dmAmount = strAmount.toDouble()

                if (dmAmount <= 0) {
                    showError("amount cannot be less than 0")
                    hasError = true
                }
            }catch (e: Exception) {
                showError("Invalid amount format")
                hasError = true
            }
        }

        if (hasError) {
            setLoading(false)
            return
        }

        val fundReq = FundWallet(dmAmount, cardId)
        val funReqJson = JSONObject(gson.toJson(fundReq))

        val req = object : JsonObjectRequest(
            Method.POST,
            Api.FUND_WALLET,
            funReqJson,
            Response.Listener { response ->
                if (response.getInt("status") == 200) {
                    updateWallet(response.getDouble("data"))
                }else {
                    showError(response.getString("message"))
                    setLoading(false)
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

        req.tag = "fund_wallet"
        queue.add(req)
    }

    private fun updateWallet(amount: Double) {
        if (dataPref.contains(getString(R.string.sh_wallet))) {
            val walletJson = dataPref.getString(getString(R.string.sh_wallet), null)
            if (walletJson != null) {
                val wallet = gson.fromJson(walletJson, Wallet::class.java)
                wallet.wallet = amount

                with (dataPref.edit()) {
                    putString(getString(R.string.sh_wallet), gson.toJson(wallet))
                    apply()
                }

                finish()
            }else logout()
        }else logout()
    }

    private fun initViews() {
        loadingCover = findViewById(R.id.loadingCoverConstraint)
        loadingAvi = loadingCover.findViewById(R.id.avi)
        errorMsg = findViewById(R.id.errorMsg)

        amountInput = findViewById(R.id.amountInput)
        cardInput = findViewById(R.id.cardInput)
        cardsListCover = findViewById(R.id.cardsListCover)
        cardsList = findViewById(R.id.cardsList)
        selectCardButton = findViewById(R.id.selectCardButton)
        noCardText = findViewById(R.id.noCardText)
        clickToClose = findViewById(R.id.clickToClose)
    }

    private fun populateCards() {
        if (cards.size > 0) {
            noCardText.visibility = View.GONE
            clickToClose.visibility = View.GONE

            for (card in cards) {
                val preView: View = LayoutInflater.from(this).inflate(R.layout.card_list_item, ConstraintLayout(this))
                val cardNoTV: TextView = preView.findViewById(R.id.cardNo)
                val expDateTV: TextView = preView.findViewById(R.id.expDate)

                cardNoTV.text = getString(R.string.card_no).format(card.cardNo)
                expDateTV.text = getString(R.string.exp_date).format(card.expMonth, card.expYear)
                preView.setOnClickListener {
                    cardId = card.id
                    cardsListCover.visibility = View.GONE
                    cardInput.text = getString(R.string.card_no_input).format(card.cardNo)
                    Log.v("ApiLog", cardId.toString())
                }

                cardsList.addView(preView)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            loadingCover.visibility = View.VISIBLE
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
        with(authPref.edit()) {
            clear()
            commit()
        }

        with(dataPref.edit()) {
            clear()
            commit()
        }

        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}
