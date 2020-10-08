package festusyuma.com.glaid

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProviders
import festusyuma.com.glaid.helpers.capitalizeWords
import festusyuma.com.glaid.model.live.PendingOrder
import festusyuma.com.glaid.request.OrderRequests
import kotlinx.android.synthetic.main.pending_order.*
import java.text.NumberFormat

/**
 * A simple [Fragment] subclass.
 */
class PendingOrderFragment : Fragment(R.layout.pending_order) {

    private lateinit var livePendingOrder: PendingOrder

    private lateinit var quantity: TextView
    private lateinit var amount: TextView
    private lateinit var gasType: TextView
    private lateinit var cancelOrderBtn: ConstraintLayout
    private lateinit var orderRequest: OrderRequests

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        livePendingOrder = ViewModelProviders.of(requireActivity()).get(PendingOrder::class.java)
        orderRequest = OrderRequests(requireActivity())
        initElem()
    }

    private fun initElem() {
        val numberFormatter = NumberFormat.getInstance()
        quantity = requireActivity().findViewById(R.id.quantity)
        amount = requireActivity().findViewById(R.id.amount)
        gasType = requireActivity().findViewById(R.id.gasType)
        cancelOrderBtn = requireActivity().findViewById(R.id.cancelOrderBtn)
        cancelOrderBtn.setOnClickListener {
            orderRequest.cancelOrder(livePendingOrder.id.value!!) {}
        }

        quantity.text = getString(R.string.formatted_quantity).format(livePendingOrder.quantity.value, livePendingOrder.gasUnit.value)
        amount.text = getString(R.string.formatted_amount).format(numberFormatter.format(livePendingOrder.amount.value))
        gasType.text = livePendingOrder.gasType.value?.capitalizeWords()
    }
}
