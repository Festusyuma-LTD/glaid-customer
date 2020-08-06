package festusyuma.com.glaid.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import festusyuma.com.glaid.R
import festusyuma.com.glaid.model.Chat


/**
 * Created by Chidozie Henry on Wednesday, June 03, 2020.
 * Email: okebugwuchidozie@gmail.com
 */
class ChatAdapter(
    val context: Context,
    val chat: List<Chat>
) : RecyclerView.Adapter<ViewHolder>() {
    private val VIEW_TYPE_MESSAGE_SENT = 1
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2

    //Add a view holder
    inner class SenderHolder(itemView: View?) : ViewHolder(itemView!!) {
        private val reciever_chatbox = itemView?.findViewById<TextView>(R.id.reciever_chatbox)
        private val reciever_time = itemView?.findViewById<TextView>(R.id.reciever_time)

        fun bindData(context: Context, chat: Chat) {
            reciever_chatbox?.text = chat.messageBody
            reciever_time?.text = chat.messageTime
        }
    }

    inner class ReceiverHolder(itemView: View?) : ViewHolder(itemView!!) {
        private val sender_chatbox = itemView?.findViewById<TextView>(R.id.sender_chatbox)
        private val senderTime = itemView?.findViewById<TextView>(R.id.senderTime)
        private val senderImg = itemView?.findViewById<ImageView>(R.id.senderImg)

        fun bindData(context: Context, chat: Chat) {
            //getting a resource id for use
            val resourceId =
                context.resources.getIdentifier(chat.userImg, "drawable", context.packageName)
            senderImg?.setImageResource(resourceId)
            sender_chatbox?.text = chat.messageBody
            senderTime?.text = chat.messageTime
        }
    }

    // Determines the appropriate ViewType according to the sender of the message.
    override fun getItemViewType(position: Int): Int {
        val message: Chat = chat[position]
        return if (message.destination === "SENDER") {
            // If the current user is the sender of the message
            VIEW_TYPE_MESSAGE_SENT
        } else {
            // If some other user sent the message
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            SenderHolder(
                LayoutInflater.from(context).inflate(R.layout.chatbox_reciever, parent, false)
            )
        } else ReceiverHolder(
            LayoutInflater.from(context).inflate(R.layout.chatbox_sender, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return chat.count()
    }

    // Bind the inner class Holder here to reuse
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (chat[position].destination === "SENDER") {
            (holder as SenderHolder).bindData(context, chat[position])
        } else {
            (holder as ReceiverHolder).bindData(context, chat[position])
        }
    }

}