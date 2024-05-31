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

class AddParticipantActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var addParticipantButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private lateinit var chatId: String
    private lateinit var usersInChat: MutableList<String>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_participant)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firebaseRef = database.getReference("messages")
        usersRef = database.getReference("users")
        chatId = intent.getStringExtra("chatId") ?: ""
        usersInChat = intent.getStringArrayListExtra("usersInChat") ?: mutableListOf()

        emailEditText = findViewById(R.id.emailEditText)
        addParticipantButton = findViewById(R.id.addParticipantButton)

        addParticipantButton.setOnClickListener {
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
                            // Добавьте нового пользователя в чат
                            val chatRef = firebaseRef.parent!!.child("chats").child(chatId)
                            usersInChat.add(userId!!) // Добавьте нового пользователя
                            usersInChat.forEach {
                                chatRef.child("users").child(it).setValue(true)
                            }

                            // Передаем обновленный список пользователей обратно в ChatActivity
                            val intent = Intent()
                            intent.putStringArrayListExtra("usersInChat", ArrayList(usersInChat))
                            setResult(RESULT_OK, intent) // Устанавливаем результат
                            finish() // Закрываем AddParticipantActivity
                        } else {
                            Toast.makeText(this@AddParticipantActivity, "Пользователь с таким Email не найден", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@AddParticipantActivity, "Пользователь с таким Email не найден", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w("AddParticipantActivity", "loadUser:onCancelled")
                }
            })
        }
    }
}