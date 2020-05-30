package festusyuma.com.glaid.requestdto

import java.io.Serializable

data class PasswordResetRequest (
    var email: String? = null,
    var tel: String?= null,
    val otp: String?= null,
    val newPassword: String? = null
): Serializable