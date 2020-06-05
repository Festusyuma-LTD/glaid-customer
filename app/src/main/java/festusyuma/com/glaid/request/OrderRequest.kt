package festusyuma.com.glaid.request

import android.location.Address

data class OrderRequest (
    val quantity: Double,
    val gasTypeId: Long,
    var deliveryAddress: Address? = null,
    var paymentType: String? = null,
    var paymentCardId: Long? = null
)