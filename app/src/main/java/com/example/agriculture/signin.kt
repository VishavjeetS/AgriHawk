package com.example.agriculture

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.SyncStateContract.Constants
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agriculture.databinding.ActivitySigninBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.toObject
import java.util.EventListener


class signin : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()
        binding.forgotpassword.setOnClickListener(){
            forgotpassword()
        }

        binding.btnLogin.setOnClickListener() {
            if (emptycheck()) {
                signinapp()
            } else {
                Toast.makeText(this, "This is a required Field", Toast.LENGTH_LONG).show()
            }
        }
        binding.txtNavigatetosignup.setOnClickListener(){
            val intent= Intent(this,signup::class.java)
            startActivity(intent)

        }

    }

    private fun signinapp() {
        var  email=binding.emailsignin.text.toString()
        var userpass=binding.password.text.toString()
       // val confirmpass=binding.retypepassword.toString()
        firebaseAuth.signInWithEmailAndPassword(email,userpass).addOnCompleteListener(this){
                task->
            if(task.isSuccessful){
//Fetching data from firestore
                val verification=firebaseAuth.currentUser?.isEmailVerified

                if(verification==true){
                    fetchdataFromFirestore(email)
                }
                else{
                    Toast.makeText(this, "Email Not Verified", Toast.LENGTH_LONG).show()

                }

            }
            else{
                Toast.makeText(this, "Email or Password is Incorrect", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun fetchdataFromFirestore( email:String){
        val docRef = db.collection("UserCollection").document(email!!)
        docRef.get()
            .addOnSuccessListener{ documentSnapshot->
                val myuser=documentSnapshot.toObject<user>()
                if (documentSnapshot != null) {
                    val Farmer= documentSnapshot.data?.get("farmer").toString()

//val Farmer="${myuser!!.Farmer}"

                    if(Farmer.equals("true")){
                        //navigate to farmer dashboard


                            navigatetofarmer()


                    }
                    else{
                        //navigate to user dashboard
                      //  document.toObject(usermainscreen::class.java)
                        navigatetouser(email)

                    }
                } else {

                }
            }
            .addOnFailureListener { exception ->

            }
    }

    private fun emptycheck(): Boolean {
        if (binding.emailsignin.text.toString().trim { it < ' ' }
                .isNotEmpty() && binding.password.text.toString().trim { it < ' ' }.isNotEmpty()) {
            return true

        }
        return false
    }
    private fun navigatetofarmer(){
    /*  val sharedpref=getSharedPreferences("farmermypref", MODE_PRIVATE)
        val editor:SharedPreferences.Editor=sharedpref.edit()
        editor.putString(
            "email",email

        )
        editor.apply()*/
        val intent= Intent(this,farmermainscreen::class.java)

        startActivity(intent)
        finish()
    }
    private fun navigatetouser(email:String){

        val intent= Intent(this,usermainscreen::class.java)
        startActivity(intent)
        finish()
    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = firebaseAuth.currentUser
       // updateUI(currentUser)
        if(currentUser != null){

        }
    }
    private fun forgotpassword(){
        val intent= Intent(this,forgotpassword::class.java)
        startActivity(intent)
    }
}