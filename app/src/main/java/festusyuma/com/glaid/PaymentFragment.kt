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
import festusyuma.com.glaid.requestdto.OrderRequest
import festusyuma.com.glaid.model.PaymentCards
import festusyuma.com.glaid.model.live.LiveOrder
import org.json.JSONObject
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
        queue = Volley.newRequestQueue(requireContext())
        authSharedPref = requireActivity().getSharedPreferences(getString(R.string.cached_authentication), Context.MODE_PRIVATE)
        token = authSharedPref.getString(getString(R.string.sh_authorization), "")?: ""
        dataPref = requireActivity().getSharedPreferences(getString(R.string.cached_data), Context.MODE_PRIVATE)

        initLoadingAndError()
        initElements()
        initCardListView()
        getCardsList()
        setPreferredPaymentMethod()
    }

    private fun initLoadingAndError() {
        loadingCover = requireActivity().findViewById(R.id.loadingCoverConstraint)
        loadingAvi = loadingCover.findViewById(R.id.avi)
        errorMsg = requireActivity().findViewById(R.id.errorMsg)
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
        cashPayment.setOnClickListener {updatePredefinedPaymentMethod("cash")}

        walletPayment = requireActivity().findViewById(R.id.walletPayment)
        walletPayment.setOnClickListener { updatePredefinedPaymentMethod("wallet") }
    }

    private fun updatePredefinedPaymentMethod(method: String) {
        cardsListCover.visibility = View.GONE
        liveOrder.paymentType.value = method
        liveOrder.paymentCard.value = null
        updatePaymentMethodInput()
    }

    private fun updatePaymentMethodInput() {
        if (liveOrder.paymentType.value.equals("card", true)) {
            val card = liveOrder.paymentCard.value
            if (card != null) {
                paymentMethod.text = getString(R.string.card_no_input).format(card.cardNo)
            }else { requireActivity().supportFragmentManager.popBackStackImmediate() }
        }else {
            paymentMethod.text = liveOrder.paymentType.value?.capitalizeWords()
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
                liveOrder.paymentType.value = "card"
                liveOrder.paymentCard.value = card
                updatePaymentMethodInput()
            }

            cardsList.addView(preView)
        }
    }

    private fun setPreferredPaymentMethod() {
        val prefPayment = dataPref.getString(getString(R.string.sh_preferred_payment), null)?: "wallet"
        if (!prefPayment.equals("wallet", true) && !prefPayment.equals("cash", true)) {
            liveOrder.paymentType.value = "card"
            liveOrder.paymentCard.value = cards.find { it.id == prefPayment.toLong() }
        }else {
            liveOrder.paymentType.value = prefPayment
            liveOrder.paymentCard.value = null
        }

        updatePaymentMethodInput()
    }

    private fun placeOrder() {
        if (!operationRunning) {
            setLoading(true)

            if (liveOrder.paymentType.value != null) {
                if (liveOrder.paymentType.value == "card" && liveOrder.paymentCard.value == null) {
                    showError("An error occurred")
                    setLoading(false)
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
                showError("Select payment method")
                setLoading(false)
            }
        }
    }

    private fun orderRequest(orderRequest: OrderRequest) {
        val reqObj = JSONObject(gson.toJson(orderRequest))
        val req = object : JsonObjectRequest(
            Method.POST,
            Api.CREATE_ORDER,
            reqObj,
            Response.Listener { response ->
                if (response.getInt("status") == 200) {
                    with(dataPref.edit()) {
                        val orderJson = response.getJSONObject("data")
                        val order = gson.toJson(Dashboard().convertOrderJSonToOrder(orderJson))
                        putString(getString(R.string.sh_pending_order), order)
                        commit()
                    }

                    val intent = Intent(requireContext(), MapsActivity::class.java)
                    startActivity(intent)
                    requireActivity().finishAffinity()
                }else {
                    showError(response.getString("message"))
                }

                setLoading(false)
            },
            Response.ErrorListener { response->
                if (response.networkResponse == null) showError(getString(R.string.internet_error_msg)) else {
                    if (response.networkResponse.statusCode == 403) {
                        logout()
                    }else showError(getString(R.string.api_error_msg))
                }

                setLoading(false)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $token"
                )
            }
        }

        req.retryPolicy = defaultRetryPolicy
        req.tag = "create_order"
        queue.add(req)
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
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.cached_authentication), Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(getString(R.string.sh_authorization))
            commit()
        }

        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finishAffinity()
    }

    override fun onPause() {
        super.onPause()
        queue.cancelAll("create_order")
    }

}
