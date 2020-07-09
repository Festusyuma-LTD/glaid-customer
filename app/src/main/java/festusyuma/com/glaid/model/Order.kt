package festusyuma.com.glaid.model

import java.time.LocalDateTime

data class Order (
    var truck: Truck? = null,
    val paymentMethod: String,
    val quantity: Double,
    val amount: Double,
    val deliveryPrice: Double,
    val tax: Double,
    val scheduledDate: LocalDateTime? = null,
    var status: String,
    var deliveryAddress: Address
)