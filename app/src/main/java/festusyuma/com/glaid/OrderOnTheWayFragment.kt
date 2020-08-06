package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProviders
import festusyuma.com.glaid.helpers.addCountryCode
import festusyuma.com.glaid.helpers.capitalizeWords
import festusyuma.com.glaid.helpers.getFirst
import festusyuma.com.glaid.model.Chat
import festusyuma.com.glaid.model.User
import festusyuma.com.glaid.model.live.PendingOrder
import java.text.NumberFormat

class OrderOnTheWayFragment : Fragment(R.layout.order_on_the_way) {

    private lateinit var dataPref: SharedPreferences
    private lateinit var livePendingOrder: PendingOrder
    private lateinit var user: User

    private lateinit var driverName: TextView
    private lateinit var quantity: TextView
    private lateinit var amount: TextView
    private lateinit var gasType: TextView
    private lateinit var driverPhone: ConstraintLayout
    private lateinit var driverChat: ConstraintLayout

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dataPref = requireActivity().getSharedPreferences(getString(R.string.cached_data), Context.MODE_PRIVATE)
        val userJson = dataPref.getString(getString(R.string.sh_user_details), "null")
        if (userJson != null) {
            user = gson.fromJson(userJson, User::class.java)
        }else requireActivity().supportFragmentManager.popBackStackImmediate()

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

        driverName.text = getString(R.string.order_driver_name).format(livePendingOrder.driver.value?.fullName?.capitalizeWords())
        quantity.text = getString(R.string.formatted_quantity).format(livePendingOrder.quantity.value, livePendingOrder.gasUnit.value)
        amount.text = getString(R.string.formatted_amount).format(numberFormatter.format(livePendingOrder.amount.value))
        gasType.text = livePendingOrder.gasType.value?.capitalizeWords()

        driverPhone.setOnClickListener { callCustomer() }
        driverChat.setOnClickListener { chat() }
    }

    private fun callCustomer() {
        val tel = livePendingOrder.driver.value?.tel
        if (tel != null) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", tel.addCountryCode(), null))
            startActivity(intent)
        }
    }

    private fun chat() {
        val chatId = livePendingOrder.id.value?: return
        val sender = user.email?: return
        val senderName = user.fullName?.capitalizeWords()?: return
        val recipient = livePendingOrder.driver.value?.email?: return
        val recipientName = livePendingOrder.driver.value?.fullName?.getFirst()?.capitalizeWords() ?: return
        val chat = Chat(
            chatId.toString(),
            sender,
            senderName,
            recipient,
            recipientName,
            true
        )

        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra(CHAT, gson.toJson(chat))
        startActivity(intent)
    }
}