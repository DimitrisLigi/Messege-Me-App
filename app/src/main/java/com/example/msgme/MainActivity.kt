package com.example.msgme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        buttonManager()
    }

    private fun buttonManager(){
        //Login button
        btn_log_in.setOnClickListener {
            //Setting variables
            val email = et_email_login.text.toString().trim()
            val password = et_password_login.text.toString().trim()
            //Checking for null strings
            if (email.isEmpty() || password.isEmpty()) return@setOnClickListener
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener (this){
                //If signing is successful
                if (it.isSuccessful)
                {
                    Log.d("login","Successful login")
                    Toast.makeText(this,"Successful login!",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this,LatestMessageActivity::class.java)
                    startActivity(intent)
                    finish()
                }else
                {
                    Toast.makeText(this,"Unsuccessful login",Toast.LENGTH_SHORT).show()
                    Log.d("login","Unsuccessful login")
                }
            }.addOnFailureListener {
                Log.d("login","Unsuccessful connection with firebase")
                Toast.makeText(this,"Unsuccessful connection with firebase!",Toast.LENGTH_SHORT).show()
            }
        }


        tv_subcribe_here.setOnClickListener {
            val intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}