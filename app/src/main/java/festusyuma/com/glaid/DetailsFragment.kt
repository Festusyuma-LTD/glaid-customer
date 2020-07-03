package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.model.GasType
import kotlinx.android.synthetic.main.fragment_details.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class DetailsFragment : Fragment(R.layout.fragment_details) {

    private var operationRunning = true
    private lateinit var gasType: String
    private lateinit var authToken: String
    private lateinit var gasTypeObj: GasType
    private lateinit var errorMsg: TextView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        errorMsg = requireActivity().findViewById(R.id.errorMsg)

        val authSharedPref = this.activity?.getSharedPreferences("auth_token", Context.MODE_PRIVATE)
        authToken = authSharedPref?.getString(getString(R.string.auth_key_name), "")?: ""
        gasType = requireArguments().getString("type", "diesel")

        val queue = Volley.newRequestQueue(requireContext())
        val req = getGasType()
        req.tag = "getGasTypeDetails"
        queue.add(req)

        quantityBtn.setOnClickListener {
            queue.cancelAll("getGasTypeDetails")
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .replace(R.id.frameLayoutFragment, QuantityFragment())
                .addToBackStack(null)
                .commit()
        }

        orderNowBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .replace(R.id.frameLayoutFragment, PaymentFragment.PaymentFragmentInstance())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun continueOrder(view: View) {
        Log.v("ApiLog", "clicked")
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
            gasTypeJson.getString("type"),
            gasTypeJson.getDouble("price"),
            gasTypeJson.getString("unit")
        )

        setPredefinedQuantities(gasTypeObj,  predefinedQuantitiesList)
    }

    private fun setPredefinedQuantities(gasType: GasType, data: JSONArray) {
        val currency = Currency.getInstance("ngn")
        val numberFormatter = NumberFormat.getInstance()
        val imgDrawable = if (gasType.type == "gas") {
            R.drawable.gas
        }else R.drawable.pump

        if (context != null) {
            for (i in 0 until data.length()) {
                val preView: View = LayoutInflater.from(requireContext()).inflate(R.layout.predefined_quantity, ConstraintLayout(requireContext()))
                val quantity = data[i] as Double
                val totalPrice = quantity * gasType.price
                val imgV = preView.findViewById<ImageView>(R.id.predefinedImg)
                val quantityTV = preView.findViewById<TextView>(R.id.quantity)
                val addressTypeTV = preView.findViewById<TextView>(R.id.addressType)
                val priceTV = preView.findViewById<TextView>(R.id.price)

                preView.setOnClickListener{ continueOrder(it) }
                imgV.setImageResource(imgDrawable)
                quantityTV.text = getString(R.string.predefined_quantity).format(quantity, gasType.unit)
                priceTV.text = getString(R.string.formatted_amount).format(numberFormatter.format(totalPrice))
                addressTypeTV.text = getString(R.string.predefined_address_type).format("Home Delivery . 3 Min")

                predefinedQuantities.addView(preView)
            }
        }
        Log.v("ApiLog", data.toString())
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

    companion object {
        fun newInstance() = DetailsFragment()

    }
}
