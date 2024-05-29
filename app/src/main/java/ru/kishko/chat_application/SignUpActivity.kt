package ru.kishko.chat_application

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up) // Замените на ваш макет

        // Инициализация FirebaseApp
        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firebaseRef = FirebaseDatabase.getInstance().getReference("users")

        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signUpButton = findViewById(R.id.signUpButton)

        signUpButton.setOnClickListener {
            signUp()
        }
    }

    private fun signUp() {
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка уникальности email
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result
                    if (signInMethods != null && !signInMethods.signInMethods?.isEmpty()!!) {
                        Toast.makeText(this, "Этот email уже используется", Toast.LENGTH_SHORT)
                            .show()
                        return@addOnCompleteListener
                    }

                    // Создание пользователя
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Log.d("SignUpActivity", "createUserWithEmail:success")
                                val user = auth.currentUser
                                saveUserToDatabase(user)
                            } else {
                                Log.w(
                                    "SignUpActivity",
                                    "createUserWithEmail:failure",
                                    task.exception
                                )
                                Toast.makeText(
                                    baseContext, "Ошибка регистрации",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Log.w("SignUpActivity", "fetchSignInMethodsForEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Ошибка проверки email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserToDatabase(user: FirebaseUser?) {
        if (user != null) {
            val userRef = firebaseRef.child(user.uid)
            val userName = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val userData = User(user.uid, email, userName)
            userRef.setValue(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SignInActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Log.w("SignUpActivity", "saveUserToDatabase:failure", it)
                    Toast.makeText(this, "Ошибка сохранения данных", Toast.LENGTH_SHORT).show()
                }
        }
    }
}