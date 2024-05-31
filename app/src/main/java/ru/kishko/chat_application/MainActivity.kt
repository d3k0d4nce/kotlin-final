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

        // Загрузка списка чатов при создании активности
        loadChatList()

        addChatButton.setOnClickListener {
            val intent = Intent(this, AddChatActivity::class.java)
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadChatList() // Обновляем список чатов после добавления нового
        }
    }

    private fun loadChatList() {
        val currentUserUid = auth.currentUser!!.uid
        val chatsRef = firebaseRef.parent!!.child("chats")
        chatList.clear() // Очищаем текущий список чатов

        chatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val chatId = child.key!!
                    val usersRef = child.child("users")
                    val usersInChat = usersRef.children.mapNotNull { it.key }.toMutableList()

                    if (usersInChat.contains(currentUserUid)) {
                        val lastMessageRef = firebaseRef.orderByChild("chatId").equalTo(chatId)
                        lastMessageRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(lastMessageSnapshot: DataSnapshot) {
                                if (lastMessageSnapshot.exists()) {
                                    val lastMessage = lastMessageSnapshot.children.last().getValue(Message::class.java)
                                    if (lastMessage != null) {
                                        val chatItem = ChatItem(
                                            usersInChat.firstOrNull { it != currentUserUid } ?: "",
                                            lastMessage.text ?: "",
                                            0,
                                            chatId,
                                            usersInChat
                                        )
                                        chatList.add(chatItem) // Добавляем чат в список
                                    }
                                }
                                chatListAdapter.updateChatList(chatList) // Обновляем адаптер
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.w("MainActivity", "onCancelled")
                            }
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("MainActivity", "loadChatList:onCancelled")
            }
        })
    }
}