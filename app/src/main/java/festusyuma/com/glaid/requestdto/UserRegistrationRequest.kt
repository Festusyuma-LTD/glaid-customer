package festusyuma.com.glaid.requestdto

data class UserRegistrationRequest (
    val fullName: String,
    val email: String,
    val tel: String,
    val password: String
)