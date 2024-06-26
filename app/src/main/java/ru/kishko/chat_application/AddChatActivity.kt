package ru.kishko.chat_application

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddChatActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var addChatButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private lateinit var chatId: String
    private lateinit var usersInChat: MutableList<String>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_chat)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firebaseRef = database.getReference("messages")
        usersRef = database.getReference("users")
        usersInChat = mutableListOf()

        emailEditText = findViewById(R.id.emailEditText)
        addChatButton = findViewById(R.id.addChatButton)

        addChatButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Введите Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val iterator = snapshot.children.iterator()
                        if (iterator.hasNext()) {
                            val userId = iterator.next().key
                            val currentUserUid = auth.currentUser!!.uid
                            chatId = "${currentUserUid}-${userId}" // Сгенерируйте ID чата
                            // Добавьте пользователей в чат
                            val chatRef = firebaseRef.parent!!.child("chats").child(chatId)
                            usersInChat.add(currentUserUid) // Добавьте текущего пользователя
                            usersInChat.add(userId!!) // Добавьте нового пользователя
                            usersInChat.forEach {
                                chatRef.child("users").child(it).setValue(true)
                            }

                            // Запускаем ChatActivity с данными
                            val intent = Intent(this@AddChatActivity, ChatActivity::class.java)
                            intent.putExtra("chatId", chatId) // Передаем chatId
                            intent.putExtra("userId", userId) // Передаем chatId
                            intent.putStringArrayListExtra("usersInChat", ArrayList(usersInChat)) // Передаем список пользователей
                            startActivity(intent)
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            Toast.makeText(this@AddChatActivity, "Пользователь с таким Email не найден", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@AddChatActivity, "Пользователь с таким Email не найден", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w("AddChatActivity", "loadUser:onCancelled")
                }
            })
        }
    }
}