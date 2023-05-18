package com.example.chattingapp

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.lang.UCharacter.GraphemeClusterBreak.V
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.chattingapp.adapter.ChatAdapter
import com.example.chattingapp.databinding.FragmentChattingBinding
import com.example.chattingapp.model.DeleteMessageData
import com.example.chattingapp.model.DisplayAllUser
import com.example.chattingapp.model.DisplayUserData
import com.example.chattingapp.model.MessageData
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import im.zego.zegoexpress.utils.ZegoLogUtil.getFileName
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Chatting(private val displayAllUser: DisplayAllUser) : Fragment() {
    private lateinit var binding: FragmentChattingBinding
    private lateinit var database: DatabaseReference
    private var senderRoom = ""
    private var receiverRoom = ""
    private var token = ""
    private var name = ""
    private var message = ""
    lateinit var chatAdapter: ChatAdapter
    private var senderId = ""
    private var receiverId = ""
    private var messageArray = arrayListOf<DeleteMessageData>()
    private var switchIcons = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentChattingBinding.inflate(layoutInflater, container, false)
        EmojiManager.install(GoogleEmojiProvider())
        return binding.root
    }


    @SuppressLint("QueryPermissionsNeeded", "InflateParams")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = Firebase.database.reference
        senderRoom = FirebaseAuth.getInstance().currentUser!!.uid
        receiverRoom = displayAllUser.userID


        senderId = FirebaseAuth.getInstance().currentUser?.uid!!
        receiverId = displayAllUser.userID
        senderRoom = senderId + receiverId
        receiverRoom = receiverId + senderId

        binding.txtReceiverName.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, ViewUsersProfile(displayAllUser))
                addToBackStack(this@Chatting.toString())
                commit()
            }
        }

        val LocalFile = File.createTempFile("tempfile", "jpeg")
        FirebaseStorage.getInstance().getReference()
            .child("Users/Profile_Pictures/+91${displayAllUser.number}/${displayAllUser.userID}")
            .getFile(LocalFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(LocalFile.absolutePath)
                binding.imgReceiverProfile.setImageBitmap(bitmap)
            }

        binding.txtReceiverName.text = displayAllUser.name

        FirebaseDatabase.getInstance().getReference("User/${displayAllUser.userID}")
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val listData = snapshot.getValue(DisplayUserData::class.java)
                            Log.d("Display user data", listData.toString())

                            Log.d("OnlineStatus", listData?.onlineStatus.toString())
                            if (listData!!.onlineStatus) {
                                binding.txtReceiverOnlineStatus.visibility = View.VISIBLE
                                binding.txtReceiverOnlineStatus.text = "Online"
                            } else if (listData.lastTime != "") {
                                val currentDate =
                                    SimpleDateFormat("dd/MM/yy").format(Calendar.getInstance().time)
                                if (currentDate != listData.lastDate) {
                                    binding.txtReceiverOnlineStatus.visibility = View.VISIBLE
                                    binding.txtReceiverOnlineStatus.text =
                                        "Last seen ${listData.lastDate} at ${listData.lastTime} "
                                } else {
                                    binding.txtReceiverOnlineStatus.visibility = View.VISIBLE
                                    binding.txtReceiverOnlineStatus.text =
                                        "Last seen today at ${listData.lastTime}"
                                }
                            } else {
                                binding.txtReceiverOnlineStatus.visibility = View.GONE
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                }
            )


        binding.imgBackButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.imgVideoCall.setOnClickListener {
            val dialog = Dialog(requireContext())
            val dialogView = layoutInflater.inflate(R.layout.video_call_layout, null)
            val videoCall = dialogView.findViewById<ZegoSendCallInvitationButton>(R.id.txtVideoCall)
            val cancel = dialogView.findViewById<TextView>(R.id.txtCancel)
            dialog.setContentView(dialogView)
            dialog.show()
            videoCall.setIsVideoCall(true)
            videoCall.resourceID = "zego_uikit_call"
                val users = ArrayList<ZegoUIKitUser>()
                users.add(ZegoUIKitUser(displayAllUser.userID, displayAllUser.name))
                videoCall.setInvitees(users)
                videoCall.setInvitees(Collections.singletonList(ZegoUIKitUser(displayAllUser.userID, displayAllUser.name)))


            cancel.setOnClickListener {
                dialog.dismiss()
            }
        }
        binding.imgVoiceCall.setOnClickListener {
            val dialog = Dialog(requireContext())
            val dialogView = layoutInflater.inflate(R.layout.voice_call_layout, null)
            val voiceCall = dialogView.findViewById<ZegoSendCallInvitationButton>(R.id.txtVoiceCall)
            val cancel = dialogView.findViewById<TextView>(R.id.txtCancel)
            dialog.setContentView(dialogView)
            dialog.show()
            voiceCall.setIsVideoCall(false)
            voiceCall.resourceID = "zego_uikit_call"
                val users = ArrayList<ZegoUIKitUser>()
                users.add(ZegoUIKitUser(displayAllUser.userID, displayAllUser.name))
                voiceCall.setInvitees(users)


            cancel.setOnClickListener {
                dialog.dismiss()
            }
        }


        val popup = EmojiPopup(binding.rootLayout, binding.editInputMsg)
        binding.imgEmoji.setOnClickListener {
            popup.toggle()
            if (!switchIcons) {
                binding.imgEmoji.setImageResource(R.drawable.ic_keyboard)
            } else {
                binding.imgEmoji.setImageResource(R.drawable.ic_emoji)
            }
            switchIcons = !switchIcons
        }


        val userId = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseDatabase.getInstance().getReference("User/$userId").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val listData = snapshot.getValue(DisplayUserData::class.java)
                        name = listData!!.name
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            }
        )

        FirebaseDatabase.getInstance().getReference("User/${displayAllUser.userID}")
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val listData = snapshot.getValue(DisplayUserData::class.java)
                            token = listData!!.token
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                }
            )
        Log.d("Current Date", SimpleDateFormat("dd/MM/yy").format(Calendar.getInstance().time))
        Log.d("Current Time", SimpleDateFormat("hh:mm a").format(Calendar.getInstance().time))



        binding.editInputMsg.addTextChangedListener(messageTextWatcher)
        binding.btnSendMsg.setOnClickListener {
            val msg = MessageData(
                userId!!,
                binding.editInputMsg.text.trim().toString(),
                SimpleDateFormat("hh:mm a").format(Calendar.getInstance().time).toLowerCase(),
                SimpleDateFormat("dd/MM/yy").format(Calendar.getInstance().time),
                "",
                "",
                "",
                false
            )

            Log.d("msg", msg.toString())

            val pushId = FirebaseDatabase.getInstance().getReference("ChatRoom").push().key
            database.child("ChatRoom/$senderRoom").child("Message").child(pushId!!).setValue(msg)
                .addOnSuccessListener {
                    database.child("ChatRoom/$receiverRoom").child("Message").child(pushId!!)
                        .setValue(msg)
                        .addOnSuccessListener {
                            sendNotification(
                                name,
                                message,
                                token
                            )
                        }
                }
            binding.editInputMsg.setText("")
        }

        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.stackFromEnd = true

        messageArray = arrayListOf<DeleteMessageData>()
        chatAdapter =
            ChatAdapter(messageArray, requireContext(), object : ChatAdapter.onClickMessage {
                override fun onDelete(
                    messageData: DeleteMessageData,
                ) {

                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle("Delete the message")
                    alertDialog.setMessage("Are you sure ?")
                    alertDialog.setPositiveButton("Delete") { dialog, id ->
                        FirebaseDatabase.getInstance().getReference("ChatRoom/$senderRoom/Message")
                            .addValueEventListener(
                                object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            for (i in snapshot.children) {
                                                val listData =
                                                    i.getValue(DeleteMessageData::class.java)
                                                listData?.key = i.key.toString()
                                                Log.d("Delete Message", listData?.key.toString())
                                                if (messageData.key == listData?.key) {
                                                    Log.d("SenderRoom", senderRoom)
                                                    Log.d("ReceiverRoom", receiverRoom)
                                                    FirebaseDatabase.getInstance()
                                                        .getReference("ChatRoom/$senderRoom/Message/${listData.key}")
                                                        .removeValue()
                                                    FirebaseDatabase.getInstance()
                                                        .getReference("ChatRoom/$receiverRoom/Message/${listData.key}")
                                                        .removeValue()
                                                    FirebaseStorage.getInstance()
                                                        .getReference("Users/Media/${listData.senderId}/${listData.imageUri}")
                                                        .delete().addOnSuccessListener {
                                                            Toast.makeText(
                                                                requireContext(),
                                                                "Deleted Image",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    FirebaseStorage.getInstance()
                                                        .getReference("Users/Files/${listData.senderId}/${listData.fileName}")
                                                        .delete().addOnSuccessListener {
                                                            Toast.makeText(
                                                                requireContext(),
                                                                "Delete File",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                                .show()
                                                        }
                                                    dialog.dismiss()

                                                }
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("DatabaseError", error.message)
                                    }
                                }
                            )
                        chatAdapter.notifyDataSetChanged()
                    }

                    alertDialog.setNegativeButton("Cancel") { dialog, id ->
                        chatAdapter.notifyDataSetChanged()
                        dialog.dismiss()
                    }
                    alertDialog.show()
                }

                override fun viewPdf(deleteMessageData: DeleteMessageData) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.type = "application/pdf"
                    intent.setData(Uri.parse(deleteMessageData.fileUrl))
                    startActivity(intent)
                }
            })
        Log.d("ReceiverId :", receiverId)
        Log.d("SenderID ", senderId!!)
        FirebaseDatabase.getInstance().getReference("ChatRoom/$senderRoom/Message")
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        messageArray.clear()
                        if (snapshot.exists()) {
                            for (i in snapshot.children) {
                                val getMessage = i.getValue(DeleteMessageData::class.java)
                                getMessage?.key = i.key.toString()
                                messageArray.add(getMessage!!)
                                message = getMessage.message

                                if (getMessage.senderId == receiverId) {
                                    val messageRemark =
                                        HashMap<String, Any>()
                                    messageRemark.put("messageRemark", true)
                                    FirebaseDatabase.getInstance()
                                        .getReference("ChatRoom/$senderRoom/Message/${getMessage.key}")
                                        .updateChildren(messageRemark)
                                        .addOnSuccessListener {
                                            FirebaseDatabase.getInstance()
                                                .getReference("ChatRoom/$receiverRoom/Message/${getMessage.key}")
                                                .updateChildren(
                                                    messageRemark
                                                )
                                        }

                                }
                                val recyclerView = binding.recyclerViewChatting
                                val position = recyclerView.adapter?.itemCount
                                binding.recyclerViewChatting.smoothScrollToPosition(position!!)
                                chatAdapter.notifyDataSetChanged()
                            }
                        } else {
                            chatAdapter.notifyDataSetChanged()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("DatabaseError", error.message)
                    }
                }
            )





        binding.recyclerViewChatting.layoutManager = linearLayoutManager
        binding.recyclerViewChatting.adapter = chatAdapter


        binding.imgCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 1)
        }

        binding.imgAttach.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val attachView =
                layoutInflater.inflate(R.layout.botttom_attach_dialog, null)
            dialog.setContentView(attachView)

            val gallery = attachView.findViewById<ImageButton>(R.id.imgGallery)
            val camera = attachView.findViewById<ImageButton>(R.id.imgCamera)
            val document = attachView.findViewById<ImageButton>(R.id.imgDocuments)
            dialog.show()

            gallery.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                imagePicker.launch(intent)
                dialog.dismiss()
            }

            camera.setOnClickListener {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, 1)
            }

            document.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/pdf"
                filePicker.launch(intent)
                dialog.dismiss()
            }
        }
    }


    private var filePicker: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val fileUri: Uri? = it.data?.data

            val fileName = getFileName(fileUri!!)
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            FirebaseStorage.getInstance().getReference("Users/Files/$userId/$fileName")
                .putFile(fileUri!!).addOnSuccessListener {

                    val fileUrl = it.storage.downloadUrl
                    while (!fileUrl.isComplete);
                    val url = fileUrl.result

                    val msg = MessageData(
                        userId!!,
                        "",
                        SimpleDateFormat("hh:mm a").format(Calendar.getInstance().time)
                            .toLowerCase(),
                        SimpleDateFormat("dd/MM/yy").format(Calendar.getInstance().time),
                        "",
                        url.toString(),
                        fileName,
                        false
                    )
                    Toast.makeText(requireContext(), "File Uploaded", Toast.LENGTH_SHORT).show()

                    Log.d("msg", msg.toString())

                    val pushId = FirebaseDatabase.getInstance().getReference("ChatRoom").push().key
                    database.child("ChatRoom/$senderRoom").child("Message").child(pushId!!)
                        .setValue(msg)
                        .addOnSuccessListener {
                            database.child("ChatRoom/$receiverRoom").child("Message")
                                .child(pushId!!)
                                .setValue(msg)
                                .addOnSuccessListener {
                                    sendNotification(
                                        name,
                                        "File",
                                        token
                                    )
                                }
                        }
                    binding.editInputMsg.setText("")
                    chatAdapter.notifyDataSetChanged()

                }
        }

    @SuppressLint("Range")
    private fun getFileName(fileUri: Uri): String {
        var fileName: String? = null
        if (fileUri.scheme.equals("content")) {
            val cursor = requireActivity().contentResolver.query(fileUri, null, null, null, null)
            cursor.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (fileName == null) {
            fileName = fileUri.path
            val cut = fileName!!.lastIndexOf('/')
            if (cut != -1) {
                fileName = fileName!!.substring(cut + 1)
            }
        }
        return fileName!!
    }


    private var imagePicker: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val imageUri: Uri? = it.data?.data
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val pushId = FirebaseDatabase.getInstance().getReference("Users").push().key
            FirebaseStorage.getInstance().getReference("Users/Media/$userId/$pushId")
                .putFile(imageUri!!).addOnSuccessListener {
                    val msg = MessageData(
                        userId!!,
                        "",
                        SimpleDateFormat("hh:mm a").format(Calendar.getInstance().time)
                            .toLowerCase(),
                        SimpleDateFormat("dd/MM/yy").format(Calendar.getInstance().time),
                        pushId!!,
                        "",
                        "",
                        false
                    )

                    Log.d("msg", msg.toString())

                    val pushId = FirebaseDatabase.getInstance().getReference("ChatRoom").push().key
                    database.child("ChatRoom/$senderRoom").child("Message").child(pushId!!)
                        .setValue(msg)
                        .addOnSuccessListener {
                            database.child("ChatRoom/$receiverRoom").child("Message")
                                .child(pushId!!)
                                .setValue(msg)
                                .addOnSuccessListener {
                                    sendNotification(
                                        name,
                                        "Photo",
                                        token
                                    )
                                }
                        }
                    binding.editInputMsg.setText("")
                    chatAdapter.notifyDataSetChanged()

                }

        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.d("IMage data", data.toString())
            val photo: Bitmap = data!!.extras!!["data"] as Bitmap
            Log.d("photo", photo.toString())
            val baos = ByteArrayOutputStream()
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val byteArray = baos.toByteArray()

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val pushId = FirebaseDatabase.getInstance().getReference("Users").push().key
            FirebaseStorage.getInstance().getReference("Users/Media/$userId/$pushId")
                .putBytes(byteArray).addOnSuccessListener {
                    val msg = MessageData(
                        userId!!,
                        "",
                        SimpleDateFormat("hh:mm a").format(Calendar.getInstance().time)
                            .toLowerCase(),
                        SimpleDateFormat("dd/MM/yy").format(Calendar.getInstance().time),
                        pushId!!,
                        "",
                        "",
                        false
                    )

                    Log.d("msg", msg.toString())

                    val pushId = FirebaseDatabase.getInstance().getReference("ChatRoom").push().key
                    database.child("ChatRoom/$senderRoom").child("Message").child(pushId!!)
                        .setValue(msg)
                        .addOnSuccessListener {
                            database.child("ChatRoom/$receiverRoom").child("Message")
                                .child(pushId!!)
                                .setValue(msg)
                                .addOnSuccessListener {
                                    sendNotification(
                                        name,
                                        "Photo",
                                        token
                                    )
                                }
                        }
                    binding.editInputMsg.setText("")
                    chatAdapter.notifyDataSetChanged()
                }
        }
    }

    private var messageTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            binding.btnSendMsg.isEnabled = false
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (binding.editInputMsg.text.trim().length > 0) {
                binding.btnSendMsg.isEnabled = true
            }
        }

        override fun afterTextChanged(s: Editable?) {
            if (binding.editInputMsg.text.trim().length > 0) {
                binding.btnSendMsg.isEnabled = true
            }
        }
    }


    private fun sendNotification(name: String, message: String, token: String) {
        val requestQueue =
            Volley.newRequestQueue(context?.applicationContext)     // using Volley

        val url = "https://fcm.googleapis.com/fcm/send"

        val jsonObject = JSONObject()
        jsonObject.put("title", name)
        jsonObject.put("body", message)

        val data = JSONObject()
        data.put("title", "Title of Your Notification in Title")
        data.put("body", "Click here")

        //  combining all the data into one as notificationData
        val notificationData = JSONObject()
        notificationData.put("to", token)   //  `here `token` is the token of other person
        notificationData.put("collapse_key", "type_a")
        notificationData.put("notification", jsonObject)    //  notification data
        notificationData.put("data", data)
        Log.d("notificationData", notificationData.toString())

        //  adding Header in request
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, notificationData,
            object : Response.Listener<JSONObject?> {
                override fun onResponse(response: JSONObject?) {
                    Log.d("jsonObject Response :", response.toString())
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    Log.e("VolleyError", error!!.message.toString())
                }
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map["Authorization"] =
                    "key=AAAAK4Vguho:APA91bHhhtwG-XAM4WkCRBM2QTkgynj_fNBUffHr7mS81e2miZmG2Nkp8CBLTkw_c7tJ3LeSMDfavoCdcA3OjB5slzJv9JzFFk1TLhTug46lOQ9kr9NT4lo8aklwt-plWsa-u9dRzfZi"   // server key
                map["Content-Type"] = "application/json"
                return map
            }
        }
        requestQueue.add(jsonObjectRequest)
    }



    override fun onDestroy() {
        super.onDestroy()
        Log.d("Chat destroy", "Destroyed")
        receiverId = ""
    }

}