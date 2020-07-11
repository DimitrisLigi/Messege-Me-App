@file:Suppress("DEPRECATION")

package com.example.msgme

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()

        buttonsManager()
    }
    private fun buttonsManager(){
        //Upload a profile image
        btn_image_upload.setOnClickListener {
            Log.d("login","Uploading an image")
            val imageIntent = Intent(Intent.ACTION_PICK)
            imageIntent.type = "image/*"
            startActivityForResult(imageIntent,0)
        }
        //Register button
        btn_register.setOnClickListener {
            createUser()
        }
        //Going back to login activity
        tv_already_an_account.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && -1 == Activity.RESULT_OK  && data != null){
            Log.d("login","Photo was selected!")
            var uri = data.data
            val inputStream = contentResolver.openInputStream(uri!!)
            val drawable = Drawable.createFromStream(inputStream,uri.toString())
            btn_image_upload.setBackgroundDrawable(drawable)
        }else{
            Log.d("login","Pali malakia egine!!!!!")
        }
    }
    private fun createUser(){
        var email = et_email.text.toString().trim()
        var password = et_password.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) return

        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("register", "createUserWithEmail:success")
                    val user = auth.currentUser
                    Log.d("register", "The uid is ${user?.uid}")
                    Toast.makeText(this,"User has successfully registered!",Toast.LENGTH_SHORT).show()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("register", "createUserWithEmail:failure", it.exception)
                    Toast.makeText(
                        this, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Log.d("register","Fail to create a user!")
                Toast.makeText(
                    this, "Failed to create new user!",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}