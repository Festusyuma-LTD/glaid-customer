package festusyuma.com.glaid.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp

data class FSLocation (
    val geoPoint: GeoPoint? = null,
    val userId: String? = null,
    val bearing: Float? = null,

    @ServerTimestamp
    val timestamp: Timestamp? = null
)