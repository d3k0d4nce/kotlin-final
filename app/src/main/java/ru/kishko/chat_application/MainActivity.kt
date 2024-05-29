package ru.kishko.chat_application

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var chatListRecyclerView: RecyclerView
    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var chatList: MutableList<ChatItem>
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var addChatButton: FloatingActionButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firebaseRef = FirebaseDatabase.getInstance().getReference("messages")
        chatListRecyclerView = findViewById(R.id.chatListRecyclerView)
        chatListAdapter = ChatListAdapter(mutableListOf(), this)
        chatListRecyclerView.layoutManager = LinearLayoutManager(this)
        chatListRecyclerView.adapter = chatListAdapter
        chatList = mutableListOf()
        addChatButton = findViewById(R.id.addChatButton)
        addChatButton.setOnClickListener {
            val intent = Intent(this, AddChatActivity::class.java)
            startActivityForResult(intent, 1)
        }

        loadChatList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadChatList()
        }
    }

    private fun loadChatList() {
        val currentUserUid = auth.currentUser!!.uid
        firebaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                val chatItemsMap = mutableMapOf<String, ChatItem>()
                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)!!
                    val otherUserId = if (message.senderUid == currentUserUid) message.receiverUid else message.senderUid

                    if (otherUserId != null) {
                        val chatItem = chatItemsMap[otherUserId] ?: ChatItem(otherUserId, "")
                        chatItem.lastMessage = message.text
                        chatItem.unreadCount = if (message.senderUid != currentUserUid) chatItem.unreadCount + 1 else chatItem.unreadCount

                        chatItemsMap[otherUserId] = chatItem
                    }
                }
                chatList.addAll(chatItemsMap.values)
                chatListAdapter.chatList = chatList
                chatListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MainActivity", "loadChatList:onCancelled")
            }
        })
    }
}