package festusyuma.com.glaid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.wang.avi.AVLoadingIndicatorView
import kotlinx.android.synthetic.main.activity_add_funds.*

class AddFundsActivity : AppCompatActivity() {

    private var operationRunning = false

    private lateinit var loadingCover: ConstraintLayout
    private lateinit var loadingAvi: AVLoadingIndicatorView
    private lateinit var errorMsg: TextView

    private lateinit var amountInput: EditText
    private lateinit var cardInput: TextView
    private lateinit var cardsList: ScrollView
    private var cardId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_funds)

        loadingCover = findViewById(R.id.loadingCoverConstraint)
        loadingAvi = loadingCover.findViewById(R.id.avi)
        errorMsg = findViewById(R.id.errorMsg)

        amountInput = findViewById(R.id.amountInput)
        cardInput = findViewById(R.id.cardInput)
        cardsList = findViewById(R.id.cardsList)

        addFundsBtn.setOnClickListener {
            if (cardId != null) {
                setLoading(true)
            }else showError("Card not selected")
        }

    }
    fun paymentIntentMethod(view: View) {
        val paymentIntent = Intent(this, PaymentActivity::class.java)
        startActivity(paymentIntent)
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
