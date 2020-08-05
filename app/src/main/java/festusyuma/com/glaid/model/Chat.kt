package festusyuma.com.glaid.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize


/**
 * Created by Chidozie Henry on Wednesday, June 03, 2020.
 * Email: okebugwuchidozie@gmail.com
 */
class Chat(
    var destination: String,
    var userImg: String,
    var messageBody: String,
    var messageTime: String,
    var deliveryStatus: String
)

@Parcelize
class ChatMessage(
    val text: String,
    val senderId: String,
    val recieverId: String,
    val timeStamp: Timestamp?,
    val dateString: String
) : Parcelable {
    constructor() : this("", "", "", null, "")

    override fun toString(): String {
        return "senderId: $senderId \n recieverId: $recieverId \n timeStamp: $dateString \n text: $text"
    }
}