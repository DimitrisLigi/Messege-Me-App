package com.example.msgme

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()
    companion object{
        const val TAG = "Chatlog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        recycler_view_chat_log.adapter = adapter

        supportActionBar?.title = user.username
        setUpTheMessagesViews()
        buttonManager()

    }

    private fun buttonManager(){
        btn_send.setOnClickListener {
            Log.d(TAG, "Attempting to send message")
            attemptToSendMessage()
        }
    }

    private fun setUpTheMessagesViews(){

        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.toString())
                    val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
                    if (chatMessage.fromID == FirebaseAuth.getInstance().uid) {
                        Log.d(TAG,"Adding message to fromitem")
                        adapter.add(NewChatFromItem(chatMessage.text))
                    } else {
                        Log.d(TAG,"Adding message to toitem")
                        adapter.add(NewChatToItem(chatMessage.text,toUser))
                    }
                }else return
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

        })
    }
    //Attempt to send message by pressing the send button
    private fun attemptToSendMessage(){
        val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val message = et_sent_message.text.toString()
        val fromID = FirebaseAuth.getInstance().uid ?: return
        val toID = toUser.uid
        val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
        val chatMessage = ChatMessage(ref.key,message,fromID,toID, System.currentTimeMillis()/1000)

        ref.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG,"The message was saved to database at ${ref.key}")
        }
    }
}

class NewChatToItem(val text : String,val user: User): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val targetImageView = viewHolder.itemView.iv_to_row
        val uri = user.profilePictureUri
        viewHolder.itemView.tv_to_row.text = text
        Picasso.get().load(uri).into(targetImageView)
    }

}

class NewChatFromItem(val text: String): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
//        val targetImageView = viewHolder.itemView.iv_from_row
//        val uri = user.profilePictureUri
        viewHolder.itemView.tv_from_row.text = text
//        Picasso.get().load(uri).into(targetImageView)
    }
}

