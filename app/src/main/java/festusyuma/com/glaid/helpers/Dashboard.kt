package festusyuma.com.glaid.helpers

import android.content.Context
import android.util.Log
import festusyuma.com.glaid.gson
import festusyuma.com.glaid.model.*
import org.json.JSONObject

class Dashboard {

    companion object {
        fun store(context: Context, data: JSONObject) {
            val dashboard = Dashboard()
            Log.v("ApiLog", "Response lass: $data")

            val sharedPref = context.getSharedPreferences("cached_data", Context.MODE_PRIVATE)
            val user = gson.toJson(dashboard.getUser(data.getJSONObject("user")))
            val wallet = gson.toJson(dashboard.getWallet(data.getJSONObject("wallet")))

            with(sharedPref.edit()) {
                clear()
                putString("userDetails", user)
                putString("wallet", wallet)
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

    /*fun getAddress(data: JSONObject): Address {

    }

    fun getOrders(data: JSONObject): Orders {

    }

    fun getPaymentCards(data: JsonObject): PaymentCards {

    }*/
}