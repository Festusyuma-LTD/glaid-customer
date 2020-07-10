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
import festusyuma.com.glaid.model.live.LiveOrder
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

    private lateinit var queue: RequestQueue
    private val preDefinedQuantitiesElem: MutableList<LinearLayout> = mutableListOf()

    private var selectedElem: View? = null
    private var selectedQuantity: Double? = null

    private lateinit var dataPref: SharedPreferences

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dataPref = requireActivity().getSharedPreferences("cached_data", Context.MODE_PRIVATE)
        errorMsg = requireActivity().findViewById(R.id.errorMsg)
        liveOrder = ViewModelProviders.of(requireActivity()).get(LiveOrder::class.java)

        val authSharedPref = this.activity?.getSharedPreferences("auth_token", Context.MODE_PRIVATE)
        authToken = authSharedPref?.getString(getString(R.string.auth_key_name), "")?: ""
        gasType = requireArguments().getString("type", "diesel")

        queue = Volley.newRequestQueue(requireContext())
        val req = getGasType()
        req.tag = "getGasTypeDetails"
        queue.add(req)

        quantityBtn.setOnClickListener {customOrderClickListener()}

        orderNowBtn.setOnClickListener { orderNowClickListener() }
    }

    private fun customOrderClickListener() {
        queue.cancelAll("getGasTypeDetails")
        liveOrder.quantity.value = null
        startCustomOrderFragment()
    }

    private fun orderNowClickListener() {
        if (selectedElem == null) {
            showError("Select Quantity")
        }else {
            queue.cancelAll("getGasTypeDetails")
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

    private fun getGasType(): JsonObjectRequest {

        val url = if (gasType == "diesel") Api.GET_DIESEL_LIST else Api.GET_GAS_LIST

        return object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                response ->
                Log.v("ApiLog", "Response $response")
                setDetails(response.getJSONObject("data"))
            },
            Response.ErrorListener { response->
                if (response.networkResponse == null) {
                    showError(getString(R.string.internet_error_msg))
                }else {
                    if (response.networkResponse.statusCode == 403) {
                        logout()
                    }else {
                        Log.v("ApiLog", response.networkResponse.statusCode.toString())
                        showError("An error occurred")
                    }
                }
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $authToken"
                )
            }
        }
    }

    fun setDetails(data: JSONObject) {
        val predefinedQuantitiesList = data.getJSONArray("predefinedQuantities")
        val gasTypeJson = data.getJSONObject("gasType")
        gasTypeObj = GasType(
            gasTypeJson.getLong("id"),
            gasTypeJson.getString("type"),
            gasTypeJson.getDouble("price"),
            gasTypeJson.getString("unit").capitalizeWords()
        )

        liveOrder.gasType.value = gasTypeObj
        setPredefinedQuantities(gasTypeObj,  predefinedQuantitiesList)
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
                val totalPrice = quantity * gasType.price
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

    fun logout() {
        val act = requireActivity()

        val sharedPref = act.getSharedPreferences("auth_token", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(getString(R.string.auth_key_name))
            commit()
        }

        startActivity(Intent(act, MainActivity::class.java))
        act.finishAffinity()
    }

    private fun showError(msg: String) {
        errorMsg.text = msg
        errorMsg.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        queue.cancelAll("getGasTypeDetails")
    }
}
