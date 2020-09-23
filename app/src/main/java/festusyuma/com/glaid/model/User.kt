package festusyuma.com.glaid.model

data class User (
    val email: String?,
    var fullName: String?,
    var tel: String?,

    var id: Long? = null,
    var rating: Double = 0.0,
    var profileImage: String? = null
)