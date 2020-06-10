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
            val wallet = gson.toJson(dashboard.getWallet(data.getJSONObject("wallet")))
            val paymentCards = dashboard.getPaymentCards(data.getJSONArray("paymentCards"))

            with(sharedPref.edit()) {
                clear()
                putString(context.getString(R.string.sh_user_details), user)
                putString(context.getString(R.string.sh_wallet), wallet)
                putStringSet(context.getString(R.string.sh_payment_cards), paymentCards)
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

    fun getPaymentCards(data: JSONArray): MutableSet<String> {

        val cards = mutableSetOf<String>()

        for (i in 0 until data.length()) {
            val cardJson = data[i] as JSONObject
            val wallet = PaymentCards(
                cardJson.getLong("id"),
                cardJson.getString("carNo"),
                cardJson.getString("expMonth"),
                cardJson.getString("expYear")
            )

            cards.add(gson.toJson(wallet))
        }

        return cards
    }

    /*fun getAddress(data: JSONObject): Address {

    }

    fun getOrders(data: JSONObject): Orders {

    }

    fun getPaymentCards(data: JsonObject): PaymentCards {

    }*/
}