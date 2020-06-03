package festusyuma.com.glaid.model

import java.time.LocalDateTime

data class Orders (
    var truck: Truck? = null,
    val paymentMethod: String,
    val quantity: Double,
    val amount: Double,
    val deliveryPrice: Double,
    val tax: Double,
    val scheduledDate: LocalDateTime,
    var status: String
)