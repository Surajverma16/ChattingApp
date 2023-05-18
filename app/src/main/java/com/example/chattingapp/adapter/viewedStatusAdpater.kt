package com.example.chattingapp.adapter

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.R
import com.example.chattingapp.model.StatusModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class viewedStatusAdpater(val statusArray: ArrayList<StatusModel>, val clicked : onClickedStatus) :
    RecyclerView.Adapter<viewedStatusAdpater.viewStatusViewHolder>() {
    class viewStatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusImage = itemView.findViewById<ImageView>(R.id.imgStatusUsersProfilePic)
        val statusName = itemView.findViewById<TextView>(R.id.txtStatusUserName)
        val statusLayout = itemView.findViewById<ConstraintLayout>(R.id.statusLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewStatusViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_status, parent, false)
        return viewStatusViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return statusArray.size
    }

    override fun onBindViewHolder(holder: viewStatusViewHolder, position: Int) {


        val userId = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseDatabase.getInstance()
            .getReference("Status/${statusArray[position].userId}/${statusArray[position].statusId}/viewStatus")
            .get().addOnSuccessListener {
                val isViewed = it.child(userId!!).value.toString()
                Log.d("isViewed", it.child(userId!!).value.toString())


                if (isViewed == "true") {
                    holder.statusLayout.visibility = View.VISIBLE
                    holder.statusName.visibility = View.VISIBLE
                    holder.statusImage.visibility = View.VISIBLE
                    holder.statusName.text = statusArray[position].userName
                    val LocalFile = File.createTempFile("tempfile", "jpeg")
                    FirebaseStorage.getInstance()
                        .getReference("Users/Status/${statusArray[position].userId}/${statusArray[position].statusId}")
                        .getFile(LocalFile).addOnSuccessListener {
                            val bitmap = BitmapFactory.decodeFile(LocalFile.absolutePath)
                            holder.statusImage.setImageBitmap(bitmap)
                        }
                } else {
                    holder.statusLayout.visibility = View.GONE
                    holder.statusName.visibility = View.GONE
                    holder.statusImage.visibility = View.GONE
                }
                holder.itemView.setOnClickListener {
                    clicked.openStatus(statusArray[position])
                }
            }
    }
    interface onClickedStatus {
        fun openStatus(statusModel: StatusModel)
    }
}