package festusyuma.com.glaid

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_funds.*

class AddFundsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_funds)
        addFundsBtn.setOnClickListener {
            startAnim()
        }
//        startAnim()
    }
    fun paymentIntentMethod(view: View) {
        var paymentIntent = Intent(this, PaymentActivity::class.java)
        startActivity(paymentIntent)
    }

    private fun startAnim() {
        loadingCoverConstraint.visibility = View.VISIBLE
        avi.show()
    }
}
