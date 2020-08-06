package festusyuma.com.glaid.model

data class Chat (
    val chatRoomId: String,
    val sender: String,
    val senderName: String,
    val recipient: String,
    val recipientName: String,
    val isOrder: Boolean = false
)