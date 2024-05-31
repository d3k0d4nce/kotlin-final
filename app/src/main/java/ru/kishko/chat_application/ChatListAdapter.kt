package ru.kishko.chat_application

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatListAdapter(var chatList: MutableList<ChatItem>, val context: Context) :
    RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatItemTextView: TextView = itemView.findViewById(R.id.chatItemTextView)
        val chatUserNameTextView: TextView = itemView.findViewById(R.id.chatUserNameTextView)
        val unreadCountTextView: TextView = itemView.findViewById(R.id.unreadCountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatItem = chatList[position]
        holder.chatItemTextView.text = chatItem.lastMessage
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("chatId", chatItem.chatId)
            // Передаем список пользователей в Intent
            intent.putStringArrayListExtra("usersInChat", ArrayList(chatItem.usersInChat)) // Преобразование в ArrayList
            context.startActivity(intent)
        }

        // Получение имени пользователя из базы данных
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        usersRef.child(chatItem.userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        holder.chatUserNameTextView.text = user.name
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("ChatListAdapter", "onCancelled")
            }
        })

        // Отображение количества непрочитанных сообщений
        if (chatItem.unreadCount > 0) {
            holder.unreadCountTextView.text = chatItem.unreadCount.toString()
            holder.unreadCountTextView.visibility = View.VISIBLE
        } else {
            holder.unreadCountTextView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = chatList.size

    // Метод обновления списка чатов
    fun updateChatList(newChatList: MutableList<ChatItem>) {
        chatList = newChatList // Обновляем список чатов
        notifyDataSetChanged() // Оповещаем адаптер о изменениях
    }
}