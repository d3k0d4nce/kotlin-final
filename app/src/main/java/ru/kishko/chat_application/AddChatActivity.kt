package ru.kishko.chat_application

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_chat)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firebaseRef = database.getReference("messages")
        usersRef = database.getReference("users")

        emailEditText = findViewById(R.id.emailEditText)
        addChatButton = findViewById(R.id.addChatButton)

        addChatButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Введите Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Проверка, существует ли пользователь с этим email
            usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Получите ID пользователя
                        val userId = snapshot.children.iterator().next().key // Получение ID пользователя
                        // Запустите ChatActivity и передайте ID пользователя
                        val intent = Intent(this@AddChatActivity, ChatActivity::class.java)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                        setResult(RESULT_OK)
                        finish()
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