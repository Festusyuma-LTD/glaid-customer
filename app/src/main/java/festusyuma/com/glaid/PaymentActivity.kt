package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import festusyuma.com.glaid.model.Wallet
import java.text.NumberFormat
import java.util.*

class PaymentActivity : AppCompatActivity() {

    private lateinit var walletAmountTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        walletAmountTV = findViewById(R.id.walletAmount)

        val sharedPref = getSharedPreferences("cached_data", Context.MODE_PRIVATE)
        if (sharedPref.contains(getString(R.string.sh_wallet))) {
            val numberFormatter = NumberFormat.getInstance()
            val walletJson = sharedPref.getString(getString(R.string.sh_wallet), null)

            if (walletJson != null) {
                val wallet = gson.fromJson(walletJson, Wallet::class.java)
                walletAmountTV.text = getString(R.string.predefined_price).format(numberFormatter.format(wallet.wallet))
            }else logout()
        }else logout()
    }

    fun addCardClick(view: View) {
        val addCardIntent = Intent(this, AddCardActivity::class.java)
        startActivity(addCardIntent)
    }

    fun addFundsBtnOnClick(view: View) {
        val addCardIntent = Intent(this, AddFundsActivity::class.java)
        startActivity(addCardIntent)
    }

    private fun logout() {
        val sharedPref = getSharedPreferences("auth_token", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(getString(R.string.auth_key_name))
            commit()
        }

        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}
