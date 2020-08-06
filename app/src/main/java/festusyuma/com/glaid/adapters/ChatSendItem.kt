package festusyuma.com.glaid.adapters

import android.widget.TextView
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import festusyuma.com.glaid.R
import festusyuma.com.glaid.helpers.appFormat
import festusyuma.com.glaid.model.fs.FSChatMessage

data class ChatSendItem (
    private val message: FSChatMessage
) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val msgTxt = viewHolder.itemView.findViewById<TextView>(R.id.sentMessage)
        val timeTxt = viewHolder.itemView.findViewById<TextView>(R.id.sentTime)

        msgTxt.text = message.message
        timeTxt.text = message.timestamp?.appFormat()
    }

    override fun getLayout() = R.layout.chatbox_send
}