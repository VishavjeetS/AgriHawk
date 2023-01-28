package com.example.agriculture

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.agriculture.databinding.ActivityForgotpasswordBinding
import com.example.agriculture.databinding.ActivitySigninBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class forgotpassword : AppCompatActivity() {
    private lateinit var binding: ActivityForgotpasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotpasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnResetpassword.setOnClickListener(){
            val email=binding.forgotemail.text.toString().trim{it <= ' '}
            if(email.isEmpty()){
                Toast.makeText(this, "Please Enter Email ", Toast.LENGTH_LONG).show()
            }
            else{
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(){
                        task->
                        if(task.isSuccessful){
                            Toast.makeText(this, "Email Sent Successfully", Toast.LENGTH_LONG).show()
                            finish()
                        }
                        else{
                            Toast.makeText(this, "Some Error Occured", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }
}