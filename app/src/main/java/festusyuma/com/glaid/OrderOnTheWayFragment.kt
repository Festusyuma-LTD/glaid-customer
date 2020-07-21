package festusyuma.com.glaid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProviders
import festusyuma.com.glaid.helpers.addCountryCode
import festusyuma.com.glaid.helpers.capitalizeWords
import festusyuma.com.glaid.model.live.PendingOrder
import java.text.NumberFormat

class OrderOnTheWayFragment : Fragment(R.layout.order_on_the_way) {

    private lateinit var livePendingOrder: PendingOrder

    private lateinit var driverName: TextView
    private lateinit var quantity: TextView
    private lateinit var amount: TextView
    private lateinit var gasType: TextView
    private lateinit var driverPhone: ConstraintLayout
    private lateinit var driverChat: ConstraintLayout

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        livePendingOrder = ViewModelProviders.of(requireActivity()).get(PendingOrder::class.java)
        initElem()
    }

    private fun initElem() {
        val numberFormatter = NumberFormat.getInstance()
        driverName = requireActivity().findViewById(R.id.driverName)
        quantity = requireActivity().findViewById(R.id.quantity)
        amount = requireActivity().findViewById(R.id.amount)
        gasType = requireActivity().findViewById(R.id.gasType)
        driverPhone = requireActivity().findViewById(R.id.driverPhone)
        driverChat = requireActivity().findViewById(R.id.driverChat)

        driverName.text = getString(R.string.order_driver_name).format(livePendingOrder.driverName.value?.capitalizeWords())
        quantity.text = getString(R.string.formatted_quantity).format(livePendingOrder.quantity.value, livePendingOrder.gasUnit.value)
        amount.text = getString(R.string.formatted_amount).format(numberFormatter.format(livePendingOrder.amount.value))
        gasType.text = livePendingOrder.gasType.value?.capitalizeWords()

        driverPhone.setOnClickListener { callCustomer() }
    }

    private fun callCustomer() {
        val tel = livePendingOrder.driver.value?.tel
        if (tel != null) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", tel.addCountryCode(), null))
            startActivity(intent)
        }
    }
}