package festusyuma.com.glaid.helpers

import android.content.Context
import android.util.Log
import festusyuma.com.glaid.R
import festusyuma.com.glaid.gson
import festusyuma.com.glaid.model.*
import org.json.JSONArray
import org.json.JSONObject

class Dashboard {

    companion object {
        fun store(context: Context, data: JSONObject) {
            val dashboard = Dashboard()
            Log.v("ApiLog", "Response lass: $data")

            val sharedPref = context.getSharedPreferences("cached_data", Context.MODE_PRIVATE)
            val user = gson.toJson(dashboard.getUser(data.getJSONObject("user")))
            val prefPayment = if (data.isNull("preferredPaymentMethod")) {
                "wallet"
            }else dashboard.getPreferredPayment(data.getJSONObject("preferredPaymentMethod"))
            val wallet = gson.toJson(dashboard.getWallet(data.getJSONObject("wallet")))
            val paymentCards = gson.toJson(dashboard.getPaymentCards(data.getJSONArray("paymentCards")))
            val homeAddress = gson.toJson(dashboard.getHomeAddress(data.getJSONArray("address")))
            val businessAddress = gson.toJson(dashboard.getBusinessAddress(data.getJSONArray("address")))

            with(sharedPref.edit()) {
                clear()
                putString(context.getString(R.string.sh_user_details), user)
                putString(context.getString(R.string.sh_wallet), wallet)
                putString(context.getString(R.string.sh_payment_cards), paymentCards)
                putString(context.getString(R.string.sh_preferred_payment), prefPayment)
                putString(context.getString(R.string.sh_home_address), homeAddress)
                putString(context.getString(R.string.sh_business_address), businessAddress)
                commit()
            }
        }
    }

    fun getUser(data: JSONObject): User {

        Log.v("ApiLog", "user: ${data.getString("email")}")

        return User(
            data.getString("email").capitalizeWords(),
            data.getString("fullName").capitalizeWords(),
            data.getString("tel")
        )
    }

    fun getWallet(data: JSONObject): Wallet {

        return Wallet(
            data.getDouble("wallet"),
            data.getDouble("bonus")
        )
    }

    fun getPaymentCards(data: JSONArray): MutableList<PaymentCards> {

        val cards = mutableListOf<PaymentCards>()

        for (i in 0 until data.length()) {
            val cardJson = data[i] as JSONObject
            val wallet = PaymentCards(
                cardJson.getLong("id"),
                cardJson.getString("carNo"),
                cardJson.getString("expMonth"),
                cardJson.getString("expYear")
            )

            cards.add(wallet)
        }

        return cards
    }

    fun getPreferredPayment(data: JSONObject): String {
        Log.v("ApiLog", "pref_payment: $data")
        val type = data.getString("type")

        return if (type in listOf("wallet", "cash")) {
            type
        }else data.getString("cardId")
    }

    fun getHomeAddress(data: JSONArray): Address? {
        for (i in 0 until data.length()) {
            val addressJson = data[i] as JSONObject
            val type = addressJson.getString("type")

            if (type == "home") return getAddress(addressJson, type)
        }

        return null
    }

    fun getBusinessAddress(data: JSONArray): Address? {
        for (i in 0 until data.length()) {
            val addressJson = data[i] as JSONObject
            val type = addressJson.getString("type")

            if (type == "business") return getAddress(addressJson, type)
        }

        return null
    }

    private fun getAddress(addressJson: JSONObject, type: String): Address {
        val address = Address(
            addressJson.getLong("id"),
            addressJson.getString("address"),
            type
        )

        val locationJson = addressJson.getJSONObject("location")
        address.lat = locationJson.getDouble("lat")
        address.lng = locationJson.getDouble("lng")

        return address
    }

    /*fun getOrders(data: JSONObject): Orders {

    }*/
}