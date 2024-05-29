package ru.kishko.chat_application

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Date

class ChatActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messagesAdapter: ChatAdapter
    private lateinit var messageEditText: EditText
    private lateinit var sendMessageButton: Button
    private lateinit var user: User
    private lateinit var userId: String // Добавьте это
    private lateinit var currentUserName: String // Добавьте это

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firebaseRef = FirebaseDatabase.getInstance().getReference("messages")
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messagesAdapter = ChatAdapter(mutableListOf())
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.adapter = messagesAdapter
        messageEditText = findViewById(R.id.messageEditText)
        sendMessageButton = findViewById(R.id.sendMessageButton)
        userId = intent.getStringExtra("userId")!! // Получите ID пользователя из Intent

        // Получение имени текущего пользователя
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userRef = firebaseRef.parent!!.child("users").child(currentUser.uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        user = snapshot.getValue(User::class.java)!!
                        currentUserName = user.name // Сохраните имя текущего пользователя
                        loadMessages()
                    } else {
                        Log.w("ChatActivity", "loadUser: User not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("ChatActivity", "loadUser:onCancelled")
                }
            })
        }

        sendMessageButton.setOnClickListener {
            sendMessage()
        }

        loadMessages()
    }

    private fun loadMessages() {
        val currentUserUid = auth.currentUser!!.uid
        firebaseRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)!!
                    if ((message.senderUid == userId && message.receiverUid == currentUserUid) || (message.senderUid == currentUserUid && message.receiverUid == userId)) {
                        messages.add(message)
                    }
                }
                messagesAdapter.messages.clear()
                messagesAdapter.messages.addAll(messages)
                messagesAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("ChatActivity", "loadMessages:onCancelled")
            }
        })
    }

    private fun sendMessage() {
        val messageText = messageEditText.text.toString()
        if (messageText.isEmpty()) return

        val message = Message(auth.currentUser!!.uid, messageText, Date().time, userId)
        firebaseRef.push().setValue(message)
        messageEditText.setText("")
    }
}