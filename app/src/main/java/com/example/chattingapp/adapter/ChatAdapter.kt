package com.example.chattingapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.R
import com.example.chattingapp.model.MessageData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class ChatAdapter(val chatList: List<MessageData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val itemSend = 1
    val itemReceive = 2

    class SenderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val message = view.findViewById<TextView>(R.id.txtSendMessgae)
    }

    class ReceiverViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val message = view.findViewById<TextView>(R.id.txtReceiveMessgae)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == itemSend) {
            val itenmView =
                LayoutInflater.from(parent.context).inflate(R.layout.send_message, parent, false)
            return SenderViewHolder(itenmView)
        } else {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.receive_message, parent, false)
            return ReceiverViewHolder(itemView)
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (FirebaseAuth.getInstance().currentUser?.uid == chatList[position].senderId) {
            itemSend
        } else {
            itemReceive
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.javaClass == SenderViewHolder::class.java) {
            val viewHolder: SenderViewHolder = holder as SenderViewHolder
            viewHolder.message.text = chatList[position].message
        } else {
            val viewHolder: ReceiverViewHolder = holder as ReceiverViewHolder
            viewHolder.message.text = chatList[position].message
        }
    }
}