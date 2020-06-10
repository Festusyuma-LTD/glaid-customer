package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
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
import com.wang.avi.AVLoadingIndicatorView
import festusyuma.com.glaid.model.PaymentCards
import kotlinx.android.synthetic.main.activity_add_funds.*

class AddFundsActivity : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_funds)
        initViews()

        selectCardButton.setOnClickListener { cardsListCover.visibility = View.VISIBLE }
        clickToClose.setOnClickListener { cardsListCover.visibility = View.GONE }

        val sharedPref = getSharedPreferences("cached_data", Context.MODE_PRIVATE)
        if (sharedPref.contains(getString(R.string.sh_wallet))) {
            val cardsJSons = sharedPref.getStringSet(getString(R.string.sh_payment_cards), mutableSetOf())
            if (cardsJSons != null) {
                if (cardsJSons.size > 0) {
                    for (c in cardsJSons) {
                        cards.add(gson.fromJson(c, PaymentCards::class.java))
                    }
                    Log.v("ApiLog", cards.size.toString())
                }
            }
        }

        addFundsBtn.setOnClickListener {
            if (cardId != null) {
                setLoading(true)
            }else showError("Card not selected")
        }

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
                val preView: View = LayoutInflater.from(this).inflate(R.layout.predefined_quantity, ConstraintLayout(this))
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
}
