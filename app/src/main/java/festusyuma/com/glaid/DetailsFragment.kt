package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.helpers.capitalizeWords
import festusyuma.com.glaid.model.Address
import festusyuma.com.glaid.model.GasType
import festusyuma.com.glaid.model.GasTypeQuantities
import festusyuma.com.glaid.model.live.LiveOrder
import festusyuma.com.glaid.request.GasRequests
import festusyuma.com.glaid.request.LoadingAndErrorHandler
import kotlinx.android.synthetic.main.fragment_details.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat


/**
 * A simple [Fragment] subclass.
 */
class DetailsFragment : Fragment(R.layout.fragment_details) {

    private lateinit var liveOrder: LiveOrder

    private var operationRunning = true
    private lateinit var gasType: String
    private lateinit var authToken: String
    private lateinit var gasTypeObj: GasType
    private lateinit var errorMsg: TextView

    private val preDefinedQuantitiesElem: MutableList<LinearLayout> = mutableListOf()
    private var selectedElem: View? = null
    private var selectedQuantity: Double? = null

    private lateinit var dataPref: SharedPreferences

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dataPref = requireActivity().getSharedPreferences(getString(R.string.cached_data), Context.MODE_PRIVATE)
        errorMsg = requireActivity().findViewById(R.id.errorMsg)
        liveOrder = ViewModelProviders.of(requireActivity()).get(LiveOrder::class.java)

        val authSharedPref = this.activity?.getSharedPreferences(getString(R.string.cached_authentication), Context.MODE_PRIVATE)
        authToken = authSharedPref?.getString(getString(R.string.sh_authorization), "")?: ""
        gasType = requireArguments().getString("type", "diesel")

        GasRequests(requireActivity()).getGasType(gasType) {
            setDetails(it)
        }

        quantityBtn.setOnClickListener {customOrderClickListener()}
        orderNowBtn.setOnClickListener { orderNowClickListener() }
    }

    private fun customOrderClickListener() {
        liveOrder.quantity.value = null
        startCustomOrderFragment()
    }

    private fun orderNowClickListener() {
        if (selectedElem == null) {
            LoadingAndErrorHandler(requireActivity()).showError("Select Quantity")
        }else {
            liveOrder.quantity.value = selectedQuantity

            val homeAddressJson = dataPref.getString(getString(R.string.sh_home_address), null)
            val homeAddress = if (homeAddressJson != null) {
                gson.fromJson(homeAddressJson, Address::class.java)
            }else null

            if (homeAddress == null) startCustomOrderFragment() else {
                liveOrder.addressType.value = "home"
                liveOrder.deliveryAddress.value = homeAddress

                startPaymentFragment()
            }
        }
    }

    private fun startCustomOrderFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
            .replace(R.id.frameLayoutFragment, QuantityFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun startPaymentFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
            .replace(R.id.frameLayoutFragment, PaymentFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun setDetails(data: JSONObject) {
        val predefinedQuantitiesList = data.getJSONArray("predefinedQuantities")
        val gasTypeJson = data.getJSONObject("gasType")
        gasTypeObj = GasType(
            gasTypeJson.getLong("id"),
            gasTypeJson.getString("type"),
            gasTypeJson.getDouble("price"),
            gasTypeJson.getString("unit").capitalizeWords(),
            gasTypeJson.getBoolean("hasFixedQuantity")
        )

        if (gasTypeObj.hasFixedQuantities) {
            gasTypeObj.fixedQuantities = getFixedQuantities(gasTypeJson.getJSONArray("fixedQuantities"))
        }

        liveOrder.gasType.value = gasTypeObj
        setPredefinedQuantities(gasTypeObj,  predefinedQuantitiesList)
    }

    private fun getFixedQuantities(data: JSONArray): MutableList<GasTypeQuantities> {
        val gasTypeQuantities: MutableList<GasTypeQuantities> = mutableListOf()

        for (i in 0 until data.length()) {
            val fixedQuantityJson = data[i] as JSONObject
            val fixedQuantity = GasTypeQuantities(
                fixedQuantityJson.getDouble("quantity"),
                fixedQuantityJson.getDouble("price")
            )

            gasTypeQuantities.add(fixedQuantity)
        }

        return gasTypeQuantities
    }

    private fun setPredefinedQuantities(gasType: GasType, data: JSONArray) {
        val numberFormatter = NumberFormat.getInstance()
        val imgDrawable = if (gasType.type == "gas") {
            R.drawable.gas
        }else R.drawable.pump

        if (context != null) {
            for (i in 0 until data.length()) {
                val preView: View = LayoutInflater.from(requireContext()).inflate(R.layout.predefined_quantity, ConstraintLayout(requireContext()))
                val prevViewElement = preView.findViewById<LinearLayout>(R.id.predefinedQCover)
                val quantity = data[i] as Double

                val totalPrice = if (gasType.hasFixedQuantities) {
                    gasType.fixedQuantities.find { it.quantity == quantity }?.price?: return
                }else quantity * gasType.price

                val imgV = preView.findViewById<ImageView>(R.id.predefinedImg)
                val quantityTV = preView.findViewById<TextView>(R.id.quantity)
                val addressTypeTV = preView.findViewById<TextView>(R.id.addressTypeLabel)
                val priceTV = preView.findViewById<TextView>(R.id.price)

                imgV.setImageResource(imgDrawable)
                quantityTV.text = getString(R.string.formatted_quantity).format(quantity, gasType.unit)
                priceTV.text = getString(R.string.formatted_amount).format(numberFormatter.format(totalPrice))
                addressTypeTV.text = getString(R.string.predefined_address_type).format("Home Delivery")

                prevViewElement.setOnClickListener {
                    togglePredefinedQuantity(it, quantity)
                }

                preDefinedQuantitiesElem.add(prevViewElement)
                predefinedQuantities.addView(preView)
            }
        }
    }

    private fun togglePredefinedQuantity(selected: View, quantity: Double) {
        for (elem in preDefinedQuantitiesElem) {
            elem.background = ContextCompat.getDrawable(requireContext(), R.drawable.fragment_btn_drawable)
        }

        selected.background = ContextCompat.getDrawable(requireContext(), R.drawable.fragmentbuttonchecked)
        selectedElem = selected
        selectedQuantity = quantity
    }
}
