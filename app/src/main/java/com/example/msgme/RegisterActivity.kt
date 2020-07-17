package com.example.msgme

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
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
            val intent = Intent(this,LogInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK  && data != null){
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
        val passwordConf = et_confirm_password.text.toString().trim()
        val userName = et_username.text.toString().trim()

       if (conditionChecking(userName,email,password,passwordConf)){
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
                    Log.d("register","Failed to create a user!")
                    Toast.makeText(
                        this, "Failed to create new user!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        else return
    }

    private fun conditionChecking(username: String, email: String,password: String,passwordConf: String): Boolean{
        return if (username.isEmpty() || email.isEmpty() || password.isEmpty() || selectedPhotoUri == null){
            Toast.makeText(this,"One of the fields is empty",Toast.LENGTH_LONG).show()
            false
        }else if (measureStringLength(username) || measureStringLength(email) || measureStringLength(password) || measureStringLength(passwordConf)){
            Toast.makeText(this,"Username and password should be greater than 4 characters length!",Toast.LENGTH_LONG).show()
            false
        }else if (password != passwordConf){
            Toast.makeText(this,"The passwords does not match",Toast.LENGTH_LONG).show()
            false
        }else true
    }

    private fun uploadProfileImageToFirebase(){

        //Checking if the photo's uri is null
        if (selectedPhotoUri == null) return

        //Giving a unique name to the photo file
        val filename = UUID.randomUUID().toString()

        //Getting the reference storage in firebase and naming the file with the value filename
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        //Adding the image to firebase
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d("Upload Image","Image successfully uploaded")
            //Reference to image location inside Firebase
            ref.downloadUrl.addOnSuccessListener {
                Log.d("Upload Image","Now i have access to the image's Uri inside Firebase which is $it")
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
            val intent = Intent(this,LatestMessageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }.addOnFailureListener {
            Log.d("register","The user didn't registered in database")
        }
    }

    private fun measureStringLength(s: String): Boolean = s.length <= 4
}

