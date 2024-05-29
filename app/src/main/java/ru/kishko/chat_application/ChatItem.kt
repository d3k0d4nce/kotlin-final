package ru.kishko.chat_application

data class ChatItem(val userId: String, var lastMessage: String, var unreadCount: Int = 0) {
    constructor() : this("", "", 0) // Пустой конструктор
}