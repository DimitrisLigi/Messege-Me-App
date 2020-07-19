package com.example.msgme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_message.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageActivity : AppCompatActivity() {

    companion object{
        var currentUser: User? = null
    }

    private val adapter  = GroupAdapter<GroupieViewHolder>()
    val latestMessageMap = HashMap<String,ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_message)
        recycler_view_latest_message.adapter = adapter
        recycler_view_latest_message.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
        adapter.setOnItemClickListener{item, view ->
            val intent = Intent(this,ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            startActivity(intent)
        }
        isUserLoggedIn()
        fetchCurrentUser()
        listenForLatestMessages()
    }

    private fun refreshRecyclerViewMessageRow(){
        adapter.clear()
        latestMessageMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages(){
        val fromID = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromID")
        ref.addChildEventListener(object :ChildEventListener{

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessageMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessageRow()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessageMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessageRow()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }


        })

    }

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                finish()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                Log.d("Latest Messages","Current User: ${currentUser?.username}")
            }

        })
    }

    private fun isUserLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            val intent = Intent(this,LogInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_new_msg -> {
                val intent = Intent(this,NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,LogInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}