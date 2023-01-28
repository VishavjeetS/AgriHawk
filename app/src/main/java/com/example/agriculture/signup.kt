package com.example.agriculture

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.widget.Toolbar
import com.example.agriculture.databinding.ActivitySigninBinding
import com.example.agriculture.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class signup : AppCompatActivity() {
    private lateinit var binding:ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db:FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth=FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()
binding.backbutton.setOnClickListener(){
    back()
}
        binding.btnSignup.setOnClickListener(){

            if(emptycheck()){
                signuptoapp()
            }
            else
            {
                Toast.makeText(this, "All Fields are Required", Toast.LENGTH_LONG).show()
            }
        }

    }
    private fun signuptoapp(){
        var name=binding.name.text.toString()
        var phonenumber=binding.phonenumber.text.toString()
        var  email=binding.email.text.toString()
        var userpass=binding.password.text.toString()
        var confirmpass=binding.retypepassword.text.toString()
        var farmeraccount:Boolean=binding.FarmerSwitch.isChecked

        if(confirmpass==userpass){
           /* val user= hashMapOf(
                "Name" to name,
                "Phone" to phonenumber,
                "Email" to email,
                "Farmer" to farmeraccount

            )*/

            val myuser= user(email,farmeraccount,name,phonenumber)
            //val usercollection=db.collection("UserCollection").document(email).set(user)
            val usercollection=db.collection("UserCollection")
          /*  val query=usercollection.whereEqualTo("Email",email).get()
               .addOnSuccessListener {
                    it->
                    if(it.isEmpty){*/
                        firebaseAuth.createUserWithEmailAndPassword(email,userpass).addOnCompleteListener(this){
                                task->
                            if(task.isSuccessful){

                               firebaseAuth.currentUser?.sendEmailVerification()
                                   ?.addOnSuccessListener {task->
                                       usercollection.document(email).set(myuser)
                                       Toast.makeText(this, "SignUp Successfull", Toast.LENGTH_LONG).show()
                                       Toast.makeText(this, "Email Verification Sent", Toast.LENGTH_LONG).show()
                                       var intent = Intent(this,signin::class.java)
                                       startActivity(intent)
                                       finish()
                                   }
                                   ?.addOnFailureListener{
                                       Toast.makeText(this, "Wrong Email", Toast.LENGTH_LONG).show()
                                   }
                            }
                            else{
                                Toast.makeText(this, "User Already Exists", Toast.LENGTH_LONG).show()
                            }
                        }

                   /* }
                    else{
                        Toast.makeText(this, "Already Existing User", Toast.LENGTH_LONG).show()

                    }
                }*/

        }
        else{
            Toast.makeText(this, userpass, Toast.LENGTH_LONG).show()
            Toast.makeText(this, confirmpass, Toast.LENGTH_LONG).show()
        }
    }
    private fun emptycheck():Boolean{
        if(  binding.email.text.toString().trim{it<=' '}.isNotEmpty() && binding.password.text.toString().trim{it<=' '}.isNotEmpty()&& binding.retypepassword.text.toString().trim{it<' '}.isNotEmpty())
        {
            return true

        }
        return false
    }

private fun back(){
    var intent= Intent(this,signin::class.java)
    startActivity(intent)
}
    }


