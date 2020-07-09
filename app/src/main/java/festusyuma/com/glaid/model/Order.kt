package festusyuma.com.glaid.model

import org.threeten.bp.LocalDateTime

data class Order (
    val paymentMethod: String,
    val gasType: String,
    val quantity: Double,
    val amount: Double,
    val deliveryPrice: Double,
    val tax: Double,
    var statusId: Long,
    var deliveryAddress: Address,
    val scheduledDate: LocalDateTime? = null,
    var truck: Truck? = null,
    var driverRating: Double? = null
)