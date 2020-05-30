package festusyuma.com.glaid.helpers

class Api {
    companion object {
        private const val API_BASE_URL: String = "https://glaid.herokuapp.com/"
        const val LOGIN: String = "${API_BASE_URL}login"
        const val REGISTER: String = "${API_BASE_URL}customer/register"
        const val RESET_PASSWORD: String = "${API_BASE_URL}reset_password"
        const val VALIDATE_OTP: String = "${API_BASE_URL}validate_otp"
    }
}