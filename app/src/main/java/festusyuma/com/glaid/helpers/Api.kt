package festusyuma.com.glaid.helpers

class Api {
    companion object {
        private const val API_BASE_URL: String = "https://glaid.herokuapp.com/"
        const val LOGIN: String = "${API_BASE_URL}login"
        const val REGISTER: String = "${API_BASE_URL}customer/register"
        const val RESET_PASSWORD: String = "${API_BASE_URL}reset_password"
        const val VALIDATE_OTP: String = "${API_BASE_URL}validate_otp"
        const val DASHBOARD: String = "${API_BASE_URL}customer/dashboard"
        const val VALIDATE_TOKEN: String = "${API_BASE_URL}customer/dashboard"
        const val GET_DIESEL_LIST: String = "${API_BASE_URL}customer/gas/diesel/list"
        const val GET_GAS_LIST: String = "${API_BASE_URL}customer/gas/gas/list"
        const val ADD_CARD: String = "${API_BASE_URL}customer/payment/card/save"
        const val ADD_CARD_INIT: String = "${API_BASE_URL}customer/payment/card/save/init"
        const val GET_CARDS_LIST: String = "${API_BASE_URL}customer/payment/cards/list"
        const val FUND_WALLET: String = "${API_BASE_URL}customer/wallet/credit"
        fun removeCardUrl(cardId: Long): String {
            return "${API_BASE_URL}customer/payment/card/$cardId/remove"
        }
    }
}