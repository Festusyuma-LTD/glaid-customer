package festusyuma.com.glaid.adapters

import com.squareup.picasso.Picasso
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import festusyuma.com.glaid.R
import festusyuma.com.glaid.model.ChatMessage
import kotlinx.android.synthetic.main.chatbox_reciever.view.*
import kotlinx.android.synthetic.main.chatbox_sender.view.*


/**
 * Created by Chidozie Henry on Sunday, August 02, 2020.
 * Email: okebugwuchidozie@gmail.com
 */
class ChatSendItem(
    private val chatDetail: ChatMessage
) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
//        Picasso.get().load(friendDetail.photoUrl).into(viewHolder.itemView.chat_send_img)
        viewHolder.itemView.reciever_chatbox.text = chatDetail.text
        viewHolder.itemView.reciever_time.text = chatDetail.dateString
    }

    override fun getLayout() = R.layout.chatbox_reciever

}

class ChatRecieveItem(
    private val friend_img: String,
    private val chatDetail: ChatMessage
) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        Picasso.get().load(friend_img).placeholder(R.drawable.pic)
            .error(R.drawable.pic).into(viewHolder.itemView.senderImg)
        viewHolder.itemView.sender_chatbox.text = chatDetail.text
        viewHolder.itemView.senderTime.text = chatDetail.dateString

    }

    override fun getLayout() = R.layout.chatbox_sender

}
