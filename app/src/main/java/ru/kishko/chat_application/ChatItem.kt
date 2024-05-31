package ru.kishko.chat_application

data class ChatItem(val userId: String, var lastMessage: String, var unreadCount: Int = 0, val chatId: String, val usersInChat: List<String>) {
    constructor() : this("", "", 0, "", listOf())
}