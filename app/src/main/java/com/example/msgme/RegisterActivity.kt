@file:Suppress("DEPRECATION")

package com.example.msgme

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    //Variables declaration
    private lateinit var auth: FirebaseAuth
    private var selectedPhotoUri : Uri? = null

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
            selectedPhotoUri = data.data
            val myBitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            iv_circle.setImageBitmap(myBitmap)
            btn_image_upload.alpha = 0f
//            val inputStream = contentResolver.openInputStream(selectedPhotoUri!!)
//            val drawable = Drawable.createFromStream(inputStream,selectedPhotoUri.toString())
//
        }else{
            Log.d("login","Pali malakia egine!!!!!")
        }
    }

    private fun createUser(){
        val email = et_email.text.toString().trim()
        val password = et_password.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) return

        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    uploadProfileImageToFirebase()
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

    private fun uploadProfileImageToFirebase(){
        //Checking if the uri is null
        if (selectedPhotoUri == null) return
        //Giving a unique name to the photo
        val filename = UUID.randomUUID().toString()
        //Getting the reference storage in firebase
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        //Adding the image to firebase
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d("Upload Image","Image successfully upload")
            //Reference to image location inside Firebase
            ref.downloadUrl.addOnSuccessListener {
                Log.d("Upload Image","Now i have access to the image inside Firebase which is $it")
                saveUserToFirebaseDatabase(it.toString())
            }
        }.addOnFailureListener {
            Log.d("Upload image","Image didn't upload")
        }
    }
    private fun saveUserToFirebaseDatabase(profilePictureUri: String){
        val uid = FirebaseAuth.getInstance().uid
        val tempUserName = et_username.text.toString().trim()
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")
        val user = User(uid,tempUserName,profilePictureUri)
        ref.setValue(user).addOnCompleteListener {
            Log.d("register","The database for the user was created!")
        }.addOnFailureListener {
            Log.d("register","The user didn't registered in database")
        }
    }
}

class User(val uid: String?, val username: String, val profilePictureUri: String)