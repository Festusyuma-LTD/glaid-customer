package festusyuma.com.glaid.requestdto

data class PreferredPayment (
    val type: String,
    val cardId: Long? = null
)