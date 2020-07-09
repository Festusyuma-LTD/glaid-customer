package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import festusyuma.com.glaid.model.GasType
import kotlinx.android.synthetic.main.fragment_payment.*


/**
 * A simple [Fragment] subclass.
 */
class PaymentFragment : Fragment(R.layout.fragment_payment) {

    private var operationRunning = true
    private lateinit var gasType: String
    private lateinit var authToken: String
    private lateinit var gasTypeObj: GasType

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val authSharedPref = this.activity?.getSharedPreferences("auth_token", Context.MODE_PRIVATE)
        authToken = authSharedPref?.getString(getString(R.string.auth_key_name), "")?: ""
        chooseAnotherPayment.setOnClickListener {

        }

        chooseAnotherPayment.setOnClickListener {
            goToPaymentActivity()
        }
    }
    fun goToPaymentActivity() {
        val intent = Intent(activity, PaymentActivity::class.java)
        startActivity(intent)
    }

}
