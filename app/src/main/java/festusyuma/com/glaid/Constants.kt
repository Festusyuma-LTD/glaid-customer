package festusyuma.com.glaid

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
const val API_LOG_TAG = "apiLog"
const val APP_LOG_TAG = "appLog"
const val FIRE_STORE_LOG_TAG = "fireStoreLog"