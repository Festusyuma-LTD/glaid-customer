package festusyuma.com.glaid.requestdto

data class LoginRequest (
    val email: String? = "",
    val password: String? = "",
    val role: Long = 3,
    val token: String? = null,
    val loginType: String? = "email"
)