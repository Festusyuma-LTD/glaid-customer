package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.wang.avi.AVLoadingIndicatorView
import festusyuma.com.glaid.helpers.capitalizeWords
import festusyuma.com.glaid.model.GasType
import festusyuma.com.glaid.model.live.LiveOrder
import kotlinx.android.synthetic.main.fragment_payment.*
import java.text.NumberFormat


/**
 * A simple [Fragment] subclass.
 */
class PaymentFragment : Fragment(R.layout.fragment_payment) {

    private lateinit var loadingCover: ConstraintLayout
    private lateinit var loadingAvi: AVLoadingIndicatorView
    private lateinit var errorMsg: TextView
    private var operationRunning = false
    private lateinit var queue: RequestQueue

    private lateinit var authSharedPref: SharedPreferences
    private lateinit var dataPref: SharedPreferences

    private lateinit var liveOrder: LiveOrder
    private lateinit var authToken: String

    private lateinit var paymentMethod: TextView
    private lateinit var changePaymentMethod: TextView
    private lateinit var quantity: TextView
    private lateinit var gasType: TextView
    private lateinit var totalAmount: TextView
    private lateinit var orderNowBtn: ConstraintLayout

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        liveOrder = ViewModelProviders.of(requireActivity()).get(LiveOrder::class.java)
        queue = Volley.newRequestQueue(requireContext())
        authSharedPref = requireActivity().getSharedPreferences("auth_token", Context.MODE_PRIVATE)
        authToken = authSharedPref.getString(getString(R.string.auth_key_name), "")?: ""
        dataPref = requireActivity().getSharedPreferences("cached_data", Context.MODE_PRIVATE)

        initElements()

        chooseAnotherPayment.setOnClickListener {
            goToPaymentActivity()
        }
    }

    private fun initElements() {
        paymentMethod = requireActivity().findViewById(R.id.paymentMethod)
        val prefPayment = dataPref.getString(getString(R.string.sh_preferred_payment), "wallet")

        quantity = requireActivity().findViewById(R.id.quantity)
        quantity.text = getString(R.string.predefined_quantity).format(liveOrder.quantity.value, liveOrder.gasType.value?.unit)

        gasType = requireActivity().findViewById(R.id.gasType)
        val gasTypeString = liveOrder.gasType.value?.type
        if (gasTypeString != null) {
            gasType.text = gasTypeString.capitalizeWords()
        }else requireActivity().supportFragmentManager.popBackStackImmediate()

        totalAmount = requireActivity().findViewById(R.id.totalAmount)
        val q = liveOrder.quantity.value
        val p = liveOrder.gasType.value?.price

        if (q != null && p != null) {
            val numberFormatter = NumberFormat.getInstance()
            totalAmount.text = getString(R.string.formatted_amount).format(numberFormatter.format((q * p)))
        }else requireActivity().supportFragmentManager.popBackStackImmediate()
    }

    fun goToPaymentActivity() {
        val intent = Intent(activity, PaymentActivity::class.java)
        startActivity(intent)
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

    private fun logout() {
        val sharedPref = requireActivity().getSharedPreferences("auth_token", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(getString(R.string.auth_key_name))
            commit()
        }

        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finishAffinity()
    }

}
