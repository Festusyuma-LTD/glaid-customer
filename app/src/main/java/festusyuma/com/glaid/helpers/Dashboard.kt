package festusyuma.com.glaid.helpers

import android.content.Context
import android.util.Log
import festusyuma.com.glaid.R
import festusyuma.com.glaid.gson
import festusyuma.com.glaid.model.*
import org.json.JSONArray
import org.json.JSONObject
import org.threeten.bp.LocalDateTime

class Dashboard {

    companion object {
        fun store(context: Context, data: JSONObject) {
            val dashboard = Dashboard()
            Log.v("ApiLog", "Response lass: $data")

            val sharedPref = context.getSharedPreferences(context.getString(R.string.cached_data), Context.MODE_PRIVATE)
            val user = gson.toJson(dashboard.getUser(data.getJSONObject("user")))
            val prefPayment = if (data.isNull("preferredPaymentMethod")) {
                "wallet"
            }else dashboard.getPreferredPayment(data.getJSONObject("preferredPaymentMethod"))
            val wallet = gson.toJson(dashboard.getWallet(data.getJSONObject("wallet")))
            val paymentCards = gson.toJson(dashboard.getPaymentCards(data.getJSONArray("paymentCards")))
            val homeAddress = gson.toJson(dashboard.getHomeAddress(data.getJSONArray("address")))
            val businessAddress = gson.toJson(dashboard.getBusinessAddress(data.getJSONArray("address")))
            val pendingOrder = dashboard.pendingOrder(data.getJSONArray("orders"))
            val orders = gson.toJson(dashboard.getOrders(data.getJSONArray("orders")))

            dashboard.getOrders(data.getJSONArray("orders"))

            with(sharedPref.edit()) {
                clear()
                putString(context.getString(R.string.sh_user_details), user)
                putString(context.getString(R.string.sh_wallet), wallet)
                putString(context.getString(R.string.sh_payment_cards), paymentCards)
                putString(context.getString(R.string.sh_preferred_payment), prefPayment)
                putString(context.getString(R.string.sh_home_address), homeAddress)
                putString(context.getString(R.string.sh_business_address), businessAddress)
                putString(context.getString(R.string.sh_orders), orders)
                if (pendingOrder != null) {
                    putString(context.getString(R.string.sh_pending_order), gson.toJson(pendingOrder))
                }
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

            if (type == "home") return getAddress(addressJson)
        }

        return null
    }

    fun getBusinessAddress(data: JSONArray): Address? {
        for (i in 0 until data.length()) {
            val addressJson = data[i] as JSONObject
            val type = addressJson.getString("type")

            if (type == "business") return getAddress(addressJson)
        }

        return null
    }

    private fun getAddress(addressJson: JSONObject): Address {
        val address = Address(
            addressJson.getLong("id"),
            addressJson.getString("address"),
            addressJson.getString("type")
        )

        val locationJson = addressJson.getJSONObject("location")
        address.lat = locationJson.getDouble("lat")
        address.lng = locationJson.getDouble("lng")

        return address
    }

    fun pendingOrder(data: JSONArray): Order? {
        for (i in 0 until data.length()) {
            val order = convertOrderJSonToOrder(data[i] as JSONObject)
            if (order.statusId < 4) {
                return order
            }
        }

        return null
    }

    fun getOrders(data: JSONArray): MutableList<Order> {
        val orders: MutableList<Order> = mutableListOf()

        for (i in 0 until data.length()) {
            orders.add(convertOrderJSonToOrder(data[i] as JSONObject))
        }

        return orders
    }

    fun convertOrderJSonToOrder(data: JSONObject): Order {
        val id = data.getLong("id")
        val quantity = data.getDouble("quantity")
        val amount = data.getDouble("amount")
        val deliveryPrice = data.getDouble("deliveryPrice")
        val tax = data.getDouble("tax")

        val scheduledDate = if (!data.isNull("scheduledDate")) {
            LocalDateTime.parse(data.getString("scheduledDate"))
        }else null

        val address = getAddress(data.getJSONObject("deliveryAddress"))
        val paymentJson = data.getJSONObject("payment")
        val paymentTypeJson = paymentJson.getString("type")
        val paymentMethod = if (paymentTypeJson == "card") {
            val paymentCard = paymentJson.getJSONObject("paymentCard")
            "Card (***** ${paymentCard.getString("carNo")})"
        }else paymentTypeJson

        val gasTypeJson = data.getJSONObject("gasType")
        val gasType = gasTypeJson.getString("type")
        val gasUnit = gasTypeJson.getString("unit")

        val statusJson = data.getJSONObject("status")
        val statusId = statusJson.getLong("id")

        val truck = if (!data.isNull("driver")) {
            val driverJson = data.getJSONObject("driver")
            val truckJson = data.getJSONObject("truck")
            val driverUser = driverJson.getJSONObject("user")

            Truck (
                truckJson.getString("make"),
                truckJson.getString("model"),
                truckJson.getString("year"),
                truckJson.getString("color")
            )
        }else null


        val driver = if (!data.isNull("driver")) {
            val driverJson = data.getJSONObject("driver")
            val userJson = driverJson.getJSONObject("user")

            User(
                userJson.getString("email"),
                userJson.getString("fullName"),
                userJson.getString("tel"),
                userJson.getLong("id")
            )
        }else null

        val driverRating = if (!data.isNull("driverRating")) {
            val rating =  data.getJSONObject("driverRating")
            rating.getDouble("userRating")
        }else null

        val customerRating = if (!data.isNull("customerRating")) {
            val rating =  data.getJSONObject("customerRating")
            rating.getDouble("userRating")
        }else null

        val tripStarted = if (!data.isNull("tripStarted")) {
            val timeJson = data.getString("tripStarted")
            LocalDateTime.parse(timeJson)
        }else null

        val tripEnded = if (!data.isNull("tripEnded")) {
            val timeJson = data.getString("tripEnded")
            LocalDateTime.parse(timeJson)
        }else null

        val timeJson = data.getString("created")
        val created = LocalDateTime.parse(timeJson)

        return Order(
            driver,
            paymentMethod,
            gasType,
            gasUnit,
            quantity,
            amount,
            deliveryPrice,
            tax, statusId,
            address,
            scheduledDate,
            truck,
            id = id,
            driverRating = driverRating,
            customerRating = customerRating,
            tripStarted = tripStarted,
            tripEnded = tripEnded,
            created = created
        )
    }
}