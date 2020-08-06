package festusyuma.com.glaid.model.fs

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class FSChatMessage (
    val sender: String? = null,
    val message: String? = null,

    @ServerTimestamp
    val timestamp: Timestamp? = Timestamp.now()
)