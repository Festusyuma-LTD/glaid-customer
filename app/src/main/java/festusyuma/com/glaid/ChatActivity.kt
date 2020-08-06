package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
//import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import festusyuma.com.glaid.adapters.ChatRecieveItem
import festusyuma.com.glaid.adapters.ChatSendItem
import festusyuma.com.glaid.helpers.*
import festusyuma.com.glaid.model.ChatMessage
import festusyuma.com.glaid.model.User
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_maps.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    //    lateinit var chatAdapter: ChatAdapter
    private var user: User? = null
    private val TAG = "ChatActivity"
    lateinit var apdater: GroupAdapter<GroupieViewHolder>
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
        initDriverDetails()
        //listen for new messages
        listenForMessages()
//        chatAdapter = ChatAdapter(this, DataServices.chatTestBlock)
        val layoutManager = LinearLayoutManager(this)

        //use beloow for chat+
        layoutManager.reverseLayout = false
        layoutManager.stackFromEnd = true
        chatView.layoutManager = layoutManager
        chatView.adapter = apdater
        // for performance when we know the layout sizes wont be changing
        chatView.setHasFixedSize(true)

    }

    private fun listenForMessages() {
        val customerEmail = if (intent.getStringExtra(CHAT_EMAIL) == user?.email) {
            "customerCare@glaidDriver.com"
        } else {
            intent.getStringExtra(CHAT_EMAIL)!!
        }
        apdater = GroupAdapter()
        db.collection("chat_log/${user?.email}/$customerEmail")
            .orderBy("timeStamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w(TAG, "listen:error", error)
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val newChat = dc.document.toObject(ChatMessage::class.java)
                            Log.d(TAG, "New message: $newChat")
                            //if message is sent by current loggin user
                            if (newChat.senderId == user?.email) {
                                apdater.add(ChatSendItem(newChat))
                            } else {
                                apdater.add(ChatRecieveItem("friendDetail", newChat))
                            }
                            //scrool recycler to new message
                            chatView.scrollToPosition(apdater.itemCount - 1)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.d(TAG, "Modified message: ${dc.document.data}")
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.d(TAG, "Removed message: ${dc.document.data}")
                        }
                    }
                }
            }
    }

    private fun initDriverDetails() {

        val dataPref = getSharedPreferences(getString(R.string.cached_data), Context.MODE_PRIVATE)
        val userJson = dataPref.getString("userDetails", "null")

        if (userJson != null) {
            user = gson.fromJson(userJson, User::class.java)

        }
        chatTitle.text = if (intent.getStringExtra(CHAT_EMAIL) == user?.email) {
            "Customer Support"
        } else {
            getString(R.string.chat_user_name, intent.getStringExtra(CHAT_NAME)!!.getFirst())
        }

    }

    fun editBackBtnClick(view: View) {
        view.startAnimation(buttonClickAnim)
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)

    }

    fun sendMessageClick(view: View) {
        if (chatMessageBox.editableText.isEmpty()) return
        view.startAnimation(buttonClickAnim)

        val receiverId = if (intent.getStringExtra(CHAT_EMAIL) == user?.email) {
            "customerCare@glaidDriver.com"
        } else {
            intent.getStringExtra(CHAT_EMAIL)!!
        }
        val senderId = user!!.email
        val timeStamp = Timestamp.now()
        val dateFormat: DateFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        val dateString: String = dateFormat.format(Date()).toString()
        val newChat =
            ChatMessage(
                chatMessageBox.editableText.toString(),
                senderId!!,
                receiverId,
                timeStamp,
                dateString
            )
        chatMessageBox.editableText.clear()
//        Toast.makeText(this, chatMessageBox.editableText, Toast.LENGTH_SHORT).show()
        db.collection("chat_log/$senderId/$receiverId").add(newChat)
            .addOnSuccessListener {
                Log.d(TAG, "$senderId mmessage to $receiverId log: $newChat")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
        db.collection("chat_log/$receiverId/$senderId").add(newChat)
            .addOnSuccessListener {
                Log.d(TAG, "$senderId mmessage to $receiverId log: $newChat")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

        //scroll recycler to new message
        chatView.scrollToPosition(apdater.itemCount - 1)
    }

}
