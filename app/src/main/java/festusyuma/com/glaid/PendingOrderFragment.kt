package festusyuma.com.glaid

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import festusyuma.com.glaid.helpers.capitalizeWords
import festusyuma.com.glaid.model.live.PendingOrder
import java.text.NumberFormat

/**
 * A simple [Fragment] subclass.
 */
class PendingOrderFragment : Fragment(R.layout.pending_order) {

    private lateinit var livePendingOrder: PendingOrder

    private lateinit var quantity: TextView
    private lateinit var amount: TextView
    private lateinit var gasType: TextView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        livePendingOrder = ViewModelProviders.of(requireActivity()).get(PendingOrder::class.java)
        initElem()
    }

    private fun initElem() {
        val numberFormatter = NumberFormat.getInstance()
        quantity = requireActivity().findViewById(R.id.quantity)
        amount = requireActivity().findViewById(R.id.amount)
        gasType = requireActivity().findViewById(R.id.gasType)

        quantity.text = getString(R.string.predefined_quantity).format(livePendingOrder.quantity.value, livePendingOrder.gasUnit.value)
        amount.text = getString(R.string.formatted_amount).format(numberFormatter.format(livePendingOrder.amount.value))
        gasType.text = livePendingOrder.gasType.value?.capitalizeWords()
    }
}
