package festusyuma.com.glaid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class AddFundsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_funds)
    }
    fun paymentIntentMethod(view: View) {
        var paymentIntent = Intent(this, PaymentActivity::class.java)
        startActivity(paymentIntent)
    }
}
