package festusyuma.com.glaid

import com.android.volley.DefaultRetryPolicy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

val db = Firebase.firestore
val auth = FirebaseAuth.getInstance()
val gson = Gson()

const val EXTRA_FORGOT_PASSWORD_CHOICE = "email"
const val EXTRA_RECOVERY_TYPE = "recoverType"
const val EXTRA_QUESTION = "question"
const val COUNTRY_CODE = "+234"

const val API_LOG_TAG = "apiLog"
const val APP_LOG_TAG = "appLog"
const val FIRE_STORE_LOG_TAG = "fireStoreLog"

//messages
const val CHECK_YOUR_INTERNET = "Error, please check your internet"
const val ERROR_OCCURRED_MSG = "An error occurred"
const val INVALID_ORDER_ID = "Invalid order id"
const val INVALID_DRIVER_ID = "Invalid driver id"
const val DRIVER_HAS_NO_TRUCK = "Driver has not been assigned to any truck yet"
const val DRIVER_BUSY = "Driver is still has pending order"
const val DRIVER_ASSIGNED = "A driver has been assigned to this order"
const val NO_PENDING_ORDER = "no pending order"
const val TRIP_STARTED = "trip started"
const val ORDER_COMPLETED = "order completed"

val defaultRetryPolicy = DefaultRetryPolicy(
    0,
    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
)