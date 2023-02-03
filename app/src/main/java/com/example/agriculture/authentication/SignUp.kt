package com.example.agriculture.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.example.agriculture.Dashboard
import com.example.agriculture.R
import com.example.agriculture.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp : Fragment() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var database: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var phone: EditText
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var re_password: EditText
    private lateinit var isFarmer: SwitchCompat
    private lateinit var submit: Button
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)
        binding=ActivitySignupBinding.inflate(inflater)
        mAuth = FirebaseAuth.getInstance()
        phone = view.findViewById(R.id.phone)
        name = view.findViewById(R.id.name)
        email = view.findViewById(R.id.email)
        password = view.findViewById(R.id.password)
        re_password = view.findViewById(R.id.retypepassword)
        isFarmer = view.findViewById(R.id.isFarmer)
        submit = view.findViewById(R.id.signUp)

        submit.setOnClickListener {
            if(emptyCheck()){
                registerUser()
            }
            else{
                Log.d("error", "Failed")
            }
        }
        return view
    }


    private fun registerUser() {
        mAuth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString()).addOnCompleteListener { task ->
            if(task.isSuccessful){
                if(isFarmer.isChecked){
                    addToDataBase(name.text.toString(), phone.text.toString(), email.text.toString(), mAuth.currentUser?.uid!!.toString(), true)
                }
                else{
                    addToDataBase(name.text.toString(), phone.text.toString(), email.text.toString(), mAuth.currentUser?.uid!!.toString(), false)
                }
            }
            else{
                Toast.makeText(requireContext(), "Error Message: "+task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addToDataBase(name: String, phone: String, email: String, uid: String, isFarmer: Boolean){
        database = FirebaseDatabase.getInstance().reference.child("users").child(uid)
        val map = HashMap<String, Any>()
        map["name"] =  name
        map["phone"] = phone
        map["email"] = email
        map["uid"] = uid
        map["isFarmer"] = isFarmer
        database.setValue(map).addOnSuccessListener {
            updateUI()
        }
    }

    private fun updateUI() {
        val intent = Intent(requireContext(), Dashboard::class.java)
        intent.putExtra("isFarmer", isFarmer.isChecked)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun emptyCheck():Boolean{
        if(name.text.toString().isNotEmpty() && phone.text.toString().isNotEmpty() && email.text.toString().isNotEmpty() && password.text.toString().isNotEmpty() && re_password.text.toString().isNotEmpty()){
            if(password.text.toString().equals(re_password.text.toString())) return true
        }
        return false
    }
}