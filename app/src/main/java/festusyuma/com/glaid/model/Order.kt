package festusyuma.com.glaid.model

import org.threeten.bp.LocalDateTime


data class Order (
    var quantity: Double? = null,
    val gasTypeId: Long,
    val deliveryAddress: Address? = null,
    var paymentType: String? = "cash",
    val paymentCardId: Long? = null,
    var scheduledDate: LocalDateTime? = null
)