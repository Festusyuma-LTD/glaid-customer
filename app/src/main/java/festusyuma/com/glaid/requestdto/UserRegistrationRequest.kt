package festusyuma.com.glaid.requestdto

import java.io.Serializable

data class UserRegistrationRequest (
    val fullName: String,
    val email: String,
    val tel: String,
    val password: String,
    val otp: String? = null
): Serializable