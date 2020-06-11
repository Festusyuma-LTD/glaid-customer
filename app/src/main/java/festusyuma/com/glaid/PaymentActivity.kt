package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import festusyuma.com.glaid.model.Wallet
import java.text.NumberFormat

class PaymentActivity : AppCompatActivity() {

    private lateinit var dataPref: SharedPreferences

    private lateinit var walletAmountTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        walletAmountTV = findViewById(R.id.walletAmount)
    }

    override fun onResume() {
        super.onResume()

        dataPref = getSharedPreferences("cached_data", Context.MODE_PRIVATE)
        if (dataPref.contains(getString(R.string.sh_wallet))) {
            val numberFormatter = NumberFormat.getInstance()
            val walletJson = dataPref.getString(getString(R.string.sh_wallet), null)

            if (walletJson != null) {
                val wallet = gson.fromJson(walletJson, Wallet::class.java)
                walletAmountTV.text = getString(R.string.formatted_amount).format(numberFormatter.format(wallet.wallet))
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
