package com.example.chattingapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chattingapp.R
import com.example.chattingapp.model.DeleteMessageData
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class ChatAdapter(
    val chatList: List<DeleteMessageData>,
    val context: Context,
    val clicked: onClickMessage,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    val itemSend = 1
    val itemReceive = 2

    class SenderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val message = view.findViewById<TextView>(R.id.txtSendMessgae)
        val time = view.findViewById<TextView>(R.id.txtSendTime)
        val layout = view.findViewById<ConstraintLayout>(R.id.sendLayout)
        val image = view.findViewById<ImageView>(R.id.imgSendImage)
        val fileImage = view.findViewById<ImageView>(R.id.imgFileImage)
        val fileName = view.findViewById<TextView>(R.id.txtFileName)
    }

    class ReceiverViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val message = view.findViewById<TextView>(R.id.txtReceiveMessgae)
        val time = view.findViewById<TextView>(R.id.txtReceiveTime)
        val image = view.findViewById<ImageView>(R.id.imgReceiveImage)
        val fileImage = view.findViewById<ImageView>(R.id.imgFileImage)
        val fileName = view.findViewById<TextView>(R.id.txtFileName)

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

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder.javaClass == SenderViewHolder::class.java) {
            val viewHolder: SenderViewHolder = holder as SenderViewHolder

            if (chatList[position].message != "") {

                viewHolder.fileImage.visibility = View.GONE
                viewHolder.fileName.visibility = View.GONE
                viewHolder.image.visibility = View.GONE
                viewHolder.message.visibility = View.VISIBLE
                viewHolder.message.text = chatList[position].message
                viewHolder.time.text = chatList[position].messageTime

            } else if (chatList[position].imageUri != "") {

                viewHolder.fileImage.visibility = View.GONE
                viewHolder.fileName.visibility = View.INVISIBLE
                viewHolder.message.visibility = View.INVISIBLE
                viewHolder.message.visibility = View.GONE
                viewHolder.image.visibility = View.VISIBLE
                val mStorage = FirebaseStorage.getInstance();
                val storageRef =
                    mStorage.getReference("Users/Media/${chatList[position].senderId}/${chatList[position].imageUri}");
                storageRef.getDownloadUrl().addOnSuccessListener(object : OnSuccessListener<Uri?> {
                    override fun onSuccess(p0: Uri?) {
                        Glide.with(context)
                            .load(p0)
                            .into(viewHolder.image)
                    }

                })
                viewHolder.time.text = chatList[position].messageTime

            }
            else if( chatList[position].fileUrl != ""){

                viewHolder.fileImage.visibility = View.VISIBLE
                viewHolder.fileName.visibility = View.VISIBLE
                viewHolder.message.visibility = View.INVISIBLE
                viewHolder.message.visibility = View.GONE
                viewHolder.image.visibility = View.GONE

                viewHolder.fileName.text = chatList[position].fileName
                viewHolder.fileImage.setOnClickListener {
                    clicked.viewPdf(chatList[position])
                }
                viewHolder.time.text = chatList[position].messageTime
            }

            viewHolder.layout.setOnLongClickListener {
                clicked.onDelete(chatList[position])
                true
            }

        } else {
            val viewHolder: ReceiverViewHolder = holder as ReceiverViewHolder
            if (chatList[position].message != "") {

                viewHolder.fileImage.visibility = View.GONE
                viewHolder.fileName.visibility = View.GONE
                viewHolder.image.visibility = View.GONE
                viewHolder.message.visibility = View.VISIBLE
                viewHolder.message.text = chatList[position].message
                viewHolder.time.text = chatList[position].messageTime

            } else if (chatList[position].imageUri != "") {

                viewHolder.fileImage.visibility = View.GONE
                viewHolder.fileName.visibility = View.INVISIBLE
                viewHolder.message.visibility = View.INVISIBLE
                viewHolder.message.visibility = View.GONE
                viewHolder.image.visibility = View.VISIBLE
                val mStorage = FirebaseStorage.getInstance();
                val storageRef =
                    mStorage.getReference("Users/Media/${chatList[position].senderId}/${chatList[position].imageUri}");
                storageRef.getDownloadUrl().addOnSuccessListener(object : OnSuccessListener<Uri?> {
                    override fun onSuccess(p0: Uri?) {
                        Glide.with(context)
                            .load(p0)
                            .into(viewHolder.image)
                    }

                })
                viewHolder.time.text = chatList[position].messageTime

            }
            else if( chatList[position].fileUrl != ""){

                viewHolder.fileImage.visibility = View.VISIBLE
                viewHolder.fileName.visibility = View.VISIBLE
                viewHolder.message.visibility = View.INVISIBLE
                viewHolder.message.visibility = View.GONE
                viewHolder.image.visibility = View.GONE

                viewHolder.fileName.text = chatList[position].fileName
                viewHolder.fileImage.setOnClickListener {
                    clicked.viewPdf(chatList[position])
                }
                viewHolder.time.text = chatList[position].messageTime
            }

        }
    }

    interface onClickMessage {
        fun onDelete(messageData: DeleteMessageData)
        fun viewPdf(deleteMessageData: DeleteMessageData)
    }
}