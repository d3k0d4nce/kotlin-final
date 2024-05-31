package ru.kishko.chat_application

data class Chat(
    val chatId: String = "",
    val usersInChat: List<String> = emptyList(),
    val lastMessage: String? = null,
    val unreadCount: Int? = 0
) {
    constructor() : this("", listOf(), "", 0) // Пустой конструктор

}
