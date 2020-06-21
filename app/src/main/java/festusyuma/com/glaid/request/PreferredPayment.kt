package festusyuma.com.glaid.request

data class PreferredPayment (
    val type: String,
    val cardId: Long? = null
)