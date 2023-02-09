package com.example.agriculture.authentication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.agriculture.Dashboard
import com.example.agriculture.R
import com.example.agriculture.model.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit

class Login : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var phone: EditText
    private lateinit var getOtp: Button
    private lateinit var numberLayout: LinearLayout
    private lateinit var otpLayout: LinearLayout
    private lateinit var otpIp: EditText
    private lateinit var option: TextView
    private lateinit var passwordLayout: LinearLayout
    private lateinit var passwordEd: EditText
    private lateinit var emailLayout: LinearLayout
    private lateinit var emailEd: EditText
    private var verificationId: String = ""
    private var mContext: Context? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        mAuth = FirebaseAuth.getInstance()
        phone = view.findViewById(R.id.phoneLogin)
        getOtp = view.findViewById(R.id.getOtp)
        numberLayout = view.findViewById(R.id.numberLayout)
        otpLayout = view.findViewById(R.id.enterOtpLayout)
        otpIp = view.findViewById(R.id.enterOtp)
        option = view.findViewById(R.id.email_pass)
        passwordLayout = view.findViewById(R.id.passwordLayout)
        emailLayout = view.findViewById(R.id.emailLayout)
        emailEd = view.findViewById(R.id.emailLogin)
        passwordEd = view.findViewById(R.id.password)

        option.setOnClickListener {
            emailLayout.visibility = View.VISIBLE
            passwordLayout.visibility = View.VISIBLE
            getOtp.setText("Login")
        }

        getOtp.setOnClickListener {
            if(getOtp.text=="Verify OTP"){
                verifyCode(otpIp.text.toString())
            }
            else if(getOtp.text == "Login"){
                loginUser()
            }
            else{
                val input = "+91" + phone.text.toString()
                Log.d("num", input)
                sendVerificationCode(input)
                numberLayout.visibility = View.GONE
                otpLayout.visibility    = View.VISIBLE
                getOtp.text              = "Verify OTP"
                otpIp.hint               = "Enter Otp"
            }
        }
        return view
    }

    private fun loginUser() {
        val email = emailEd.text.toString()
        val password = passwordEd.text.toString()
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                Log.d("id", task.result.user!!.uid + " " + mAuth.currentUser!!.uid)
            database = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid)
                database.addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)!!
                        updateUI(user.getIsFarmer())
                        Log.d("isFarmer", user.getIsFarmer().toString())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("error", error.message.toString())
                    }

                })
            }
            else {
                Toast.makeText(requireContext(), task.exception!!.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    database = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid)
                    database.addValueEventListener(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(postSnapshot in snapshot.children){
                                val user = postSnapshot.getValue(User::class.java)!!
                                updateUI(user.getIsFarmer())
                                Log.d("isFarmer", user.getUserUid())
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("error", error.message.toString())
                        }

                    })
                } else {
                    Toast.makeText(requireContext(), task.exception!!.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun sendVerificationCode(input: String) {
        val options: PhoneAuthOptions = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(input) // Phone number to verify
            .setTimeout(60, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(mCallBack) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private var mCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(s: String, p1: PhoneAuthProvider.ForceResendingToken) {
            verificationId = s
        }
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            val code = phoneAuthCredential.smsCode;
            otpIp.setText(code.toString())
        }

        override fun onVerificationFailed(error: FirebaseException) {
            Log.d("error", error.message.toString())
        }

    }

    private fun updateUI(isFarmer: Boolean) {
        if(isAdded)
        {
            val intent = Intent(mContext, Dashboard::class.java)
            intent.putExtra("isFarmer", isFarmer)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = if (context is Activity) context else null
    }
}