package festusyuma.com.glaid.request

import festusyuma.com.glaid.model.Address
import org.threeten.bp.LocalDateTime


data class OrderRequest (
    var quantity: Double? = null,
    val gasTypeId: Long,
    val deliveryAddress: Address? = null,
    var paymentType: String? = "cash",
    val paymentCardId: Long? = null,
    var scheduledDate: LocalDateTime? = null
)