package festusyuma.com.glaid.model

import java.time.LocalDateTime

data class Order (
    var quantity: Double? = null,
    var addressType: String? = null,
    val gasTypeId: Long,
    val deliveryAddress: Address,
    var paymentType: String? = "cash",
    val paymentCardId: Long? = null,
    var scheduledDate: LocalDateTime? = null
)