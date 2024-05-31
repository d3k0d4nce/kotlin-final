package ru.kishko.chat_application

// Модель сообщения
data class Message(val senderUid: String, val text: String, val timestamp: Long, val chatId: String) {
    constructor() : this("", "", 0, "")
}