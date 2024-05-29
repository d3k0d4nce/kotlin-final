package ru.kishko.chat_application

// Модель пользователя
data class User(val uid: String, val email: String, val name: String) {
    constructor() : this("", "", "") // Пустой конструктор
}
