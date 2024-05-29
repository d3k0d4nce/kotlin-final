package ru.kishko.chat_application

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(val messages: MutableList<Message>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val senderTextView: TextView = itemView.findViewById(R.id.senderTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTextView.text = message.text
        val currentUserUid = FirebaseAuth.getInstance().currentUser!!.uid
        holder.senderTextView.text = if (message.senderUid == currentUserUid) {
            "Вы: "
        } else {
            "Отправитель: "
        }
    }

    override fun getItemCount(): Int = messages.size
}