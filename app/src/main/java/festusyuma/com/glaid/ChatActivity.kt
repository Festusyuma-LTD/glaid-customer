package festusyuma.com.glaid

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.xwray.groupie.GroupAdapter
import festusyuma.com.glaid.adapters.ChatReceiveItem
import festusyuma.com.glaid.adapters.ChatSendItem
import festusyuma.com.glaid.model.User
import festusyuma.com.glaid.model.fs.FSChatMessage
import festusyuma.com.glaid.requestdto.Chat

class ChatActivity : AppCompatActivity() {

    private var user: User? = null
    private lateinit var adapter: GroupAdapter<com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder>

    private lateinit var chat: Chat
    private lateinit var chatTitle: TextView
    private lateinit var chatView: RecyclerView
    private lateinit var chatMessageBox: EditText

    override fun onCreate(savedInstanceState: Bundle?) {

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatTitle = findViewById(R.id.chatTitle)
        chatView = findViewById(R.id.chatView)
        chatMessageBox = findViewById(R.id.chatMessageBox)
        adapter = GroupAdapter()

        val chatJson = intent.getStringExtra(CHAT)
        if (chatJson != null) {
            chat = gson.fromJson(chatJson, Chat::class.java)
        }else finish()

        chatTitle.text = getString(R.string.chat_user_name).format(chat.recipientName)
        listenForMessages()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = false
        layoutManager.stackFromEnd = true

        chatView.layoutManager = layoutManager
        chatView.adapter = adapter
    }

    private fun listenForMessages() {
        getChatRoomQuery()
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w(FIRE_STORE_LOG_TAG, "listen:error", error)
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val newChat = dc.document.toObject(FSChatMessage::class.java)
                            Log.d(FIRE_STORE_LOG_TAG, "New message: $newChat")

                            if (newChat.sender == chat.sender) {
                                adapter.add(ChatSendItem(newChat))
                            } else {
                                adapter.add(ChatReceiveItem(newChat))
                            }

                            chatView.scrollToPosition(adapter.itemCount - 1)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.d(FIRE_STORE_LOG_TAG, "Modified message: ${dc.document.data}")
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.d(FIRE_STORE_LOG_TAG, "Removed message: ${dc.document.data}")
                        }
                    }
                }
            }
    }

    fun editBackBtnClick(view: View) {
        view.startAnimation(buttonClickAnim)
        finish()
    }

    fun sendMessageClick(view: View) {
        view.startAnimation(buttonClickAnim)
        val message = chatMessageBox.text.toString()
        if (message.isBlank()) return
        val chatMessage = FSChatMessage(chat.sender, message)

        getChatRoomQuery()
            .add(chatMessage)
            .addOnSuccessListener {
                Log.v(FIRE_STORE_LOG_TAG, "message sent")
            }
            .addOnFailureListener {
                Log.v(FIRE_STORE_LOG_TAG, "error sending message ${it.message}")
            }

        chatMessageBox.text.clear()
        chatView.scrollToPosition(adapter.itemCount - 1)
    }

    private fun getChatRoomQuery(): CollectionReference {
        val chatRoomPath = if (chat.isOrder) {
            getString(R.string.fs_order_messages)
        } else getString(R.string.fs_support_messages)

        return db.collection(chatRoomPath)
            .document(chat.chatRoomId)
            .collection(getString(R.string.fs_messages))
    }
}
