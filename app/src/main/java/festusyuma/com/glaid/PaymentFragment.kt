package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.reflect.TypeToken
import com.wang.avi.AVLoadingIndicatorView
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.helpers.Dashboard
import festusyuma.com.glaid.helpers.capitalizeWords
import festusyuma.com.glaid.model.Order
import festusyuma.com.glaid.requestdto.OrderRequest
import festusyuma.com.glaid.model.PaymentCards
import festusyuma.com.glaid.model.live.LiveOrder
import festusyuma.com.glaid.request.LoadingAndErrorHandler
import festusyuma.com.glaid.request.OrderRequests
import festusyuma.com.glaid.requestdto.PreferredPayment
import org.json.JSONObject
import java.text.NumberFormat


/**
 * A simple [Fragment] subclass.
 */
class PaymentFragment : Fragment(R.layout.fragment_payment) {

    private lateinit var authSharedPref: SharedPreferences
    private lateinit var dataPref: SharedPreferences

    private lateinit var liveOrder: LiveOrder
    private lateinit var token: String

    private lateinit var paymentMethod: TextView
    private lateinit var changePaymentMethod: TextView
    private lateinit var quantity: TextView
    private lateinit var gasType: TextView
    private lateinit var totalAmount: TextView
    private lateinit var orderNowBtn: ConstraintLayout

    private lateinit var cardsListCover: ScrollView
    private lateinit var cardsList: LinearLayout
    private lateinit var clickToClose: TextView
    private lateinit var cashPayment: TextView
    private lateinit var walletPayment: TextView
    private val cards: MutableList<PaymentCards> = mutableListOf()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        liveOrder = ViewModelProviders.of(requireActivity()).get(LiveOrder::class.java)
        authSharedPref = requireActivity().getSharedPreferences(getString(R.string.cached_authentication), Context.MODE_PRIVATE)
        token = authSharedPref.getString(getString(R.string.sh_authorization), "")?: ""
        dataPref = requireActivity().getSharedPreferences(getString(R.string.cached_data), Context.MODE_PRIVATE)

        initElements()
        initCardListView()
        getCardsList()
        setPreferredPaymentMethod()
    }

    private fun initElements() {
        paymentMethod = requireActivity().findViewById(R.id.paymentMethod)

        quantity = requireActivity().findViewById(R.id.quantity)
        quantity.text = getString(R.string.formatted_quantity).format(liveOrder.quantity.value, liveOrder.gasType.value?.unit)

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

        orderNowBtn = requireActivity().findViewById(R.id.orderNowBtn)
        orderNowBtn.setOnClickListener { placeOrder() }
    }

    private fun initCardListView() {
        cardsListCover = requireActivity().findViewById(R.id.paymentMethodsCover)
        cardsList = requireActivity().findViewById(R.id.paymentMethods)
        changePaymentMethod = requireActivity().findViewById(R.id.chooseAnotherPayment)
        changePaymentMethod.setOnClickListener {
            cardsListCover.visibility = View.VISIBLE
        }

        clickToClose = requireActivity().findViewById(R.id.clickToClose)
        clickToClose.setOnClickListener {
            cardsListCover.visibility = View.GONE
        }

        cashPayment = requireActivity().findViewById(R.id.cashPayment)
        cashPayment.setOnClickListener {updatePredefinedPaymentMethod(PaymentType.CASH)}

        walletPayment = requireActivity().findViewById(R.id.walletPayment)
        walletPayment.setOnClickListener { updatePredefinedPaymentMethod(PaymentType.WALLET) }
    }

    private fun updatePredefinedPaymentMethod(method: String) {
        cardsListCover.visibility = View.GONE
        liveOrder.paymentType.value = method
        liveOrder.paymentCard.value = null
        updatePaymentMethodInput()
    }

    private fun updatePaymentMethodInput() {
        if (liveOrder.paymentType.value == PaymentType.CARD) {
            val card = liveOrder.paymentCard.value
            if (card != null) {
                paymentMethod.text = getString(R.string.card_no_input).format(card.cardNo)
            }else { requireActivity().supportFragmentManager.popBackStackImmediate() }
        }else {
            paymentMethod.text = if (liveOrder.paymentType.value == PaymentType.WALLET) {
                PaymentType.WALLET_TEXT
            }else PaymentType.CASH_TEXT
        }
    }

    private fun getCardsList() {
        if (dataPref.contains(getString(R.string.sh_wallet))) {
            val cardsJSons = dataPref.getString(getString(R.string.sh_payment_cards), null)
            val typeToken = object: TypeToken<MutableList<PaymentCards>>(){}.type

            if (cardsJSons != null) {
                val cardsObj: MutableList<PaymentCards> = gson.fromJson(cardsJSons, typeToken)
                for (c in cardsObj) cards.add(c)

                populateCards()
            }
        }
    }

    private fun populateCards() {
        for (card in cards) {
            val preView: View = LayoutInflater.from(requireContext()).inflate(R.layout.card_list_item, ConstraintLayout(requireContext()))
            val cardNoTV: TextView = preView.findViewById(R.id.cardNo)
            val expDateTV: TextView = preView.findViewById(R.id.expDate)

            cardNoTV.text = getString(R.string.card_no).format(card.cardNo)
            expDateTV.text = getString(R.string.exp_date).format(card.expMonth, card.expYear)
            preView.setOnClickListener {
                cardsListCover.visibility = View.GONE
                liveOrder.paymentType.value = PaymentType.CARD
                liveOrder.paymentCard.value = card
                updatePaymentMethodInput()
            }

            cardsList.addView(preView)
        }
    }

    private fun setPreferredPaymentMethod() {
        val prefPayment = dataPref.getString(getString(R.string.sh_preferred_payment), null)?: PaymentType.WALLET
        if (prefPayment == PaymentType.WALLET || prefPayment == PaymentType.CASH) {
            liveOrder.paymentType.value = prefPayment
            liveOrder.paymentCard.value = null
        }else {
            liveOrder.paymentType.value = PaymentType.CARD
            liveOrder.paymentCard.value = cards.find { it.id == prefPayment.toLong() }
        }

        updatePaymentMethodInput()
    }

    private fun placeOrder() {
        if (liveOrder.paymentType.value != null) {
            if (liveOrder.paymentType.value == "card" && liveOrder.paymentCard.value == null) {
                LoadingAndErrorHandler(requireActivity()).errorOccurred()
                return
            }

            val address = liveOrder.deliveryAddress.value
            if (address != null) {
                val order = OrderRequest(
                    liveOrder.quantity.value,
                    liveOrder.gasType.value?.id ?: 1,
                    address,
                    liveOrder.paymentType.value,
                    liveOrder.paymentCard.value?.id,
                    liveOrder.scheduledDate.value
                )

                orderRequest(order)
            }else requireActivity().supportFragmentManager.popBackStackImmediate()
        }else {
            LoadingAndErrorHandler(requireActivity()).showError("Select payment method")
        }
    }

    private fun orderRequest(orderRequest: OrderRequest) {
        OrderRequests(requireActivity()).createOrder(orderRequest) { data ->
            val typeToken = object: TypeToken<MutableList<Order>>(){}.type
            val order = Dashboard().convertOrderJSonToOrder(data)
            val ordersJson = dataPref.getString(getString(R.string.sh_orders), null)

            val orders = if (ordersJson != null) {
                gson.fromJson(ordersJson, typeToken)
            }else mutableListOf<Order>()

            orders.add(0, order)

            with(dataPref.edit()) {
                putString(getString(R.string.sh_pending_order), gson.toJson(order))
                putString(getString(R.string.sh_orders), gson.toJson(orders))
                commit()
            }

            val intent = Intent(requireContext(), MapsActivity::class.java)
            startActivity(intent)
            requireActivity().finishAffinity()
        }
    }
}
