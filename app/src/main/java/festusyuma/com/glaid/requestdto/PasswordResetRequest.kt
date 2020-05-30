package festusyuma.com.glaid.requestdto

import java.io.Serializable

data class PasswordResetRequest (
    var email: String? = null,
    var tel: String?= null,
    var otp: String?= null,
    var newPassword: String? = null
): Serializable