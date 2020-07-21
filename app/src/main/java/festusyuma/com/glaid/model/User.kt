package festusyuma.com.glaid.model

data class User (
    val email: String,
    var fullName: String,
    var tel: String,

    var id: Long? = null
)