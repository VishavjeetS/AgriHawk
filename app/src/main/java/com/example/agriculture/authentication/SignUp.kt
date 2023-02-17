package com.example.agriculture.authentication

import android.content.ContentValues
import android.content.Intent
import android.location.Location
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
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class SignUp : Fragment(), View.OnClickListener {
    private lateinit var database: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var phone: EditText
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var re_password: EditText
    private lateinit var isFarmer: SwitchCompat
    private lateinit var submit: Button
    private lateinit var verifyNum: Button
    private lateinit var verifyEmail: Button
    private lateinit var phoneOtp: EditText
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var latitude = 0.0
    private var longitude = 0.0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)
        mAuth = FirebaseAuth.getInstance()
        phone = view.findViewById(R.id.phone)
        name = view.findViewById(R.id.name)
        email = view.findViewById(R.id.email)
        password = view.findViewById(R.id.password)
        re_password = view.findViewById(R.id.retypepassword)
        isFarmer = view.findViewById(R.id.isFarmer)
        submit = view.findViewById(R.id.signUp)
        verifyNum = view.findViewById(R.id.verify_num)
        verifyEmail = view.findViewById(R.id.verify_email)
        phoneOtp = view.findViewById(R.id.phone_otp)

        submit.isFocusable = false
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                val code = credential.smsCode;
                phoneOtp.setText(code.toString())
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("error", e.message.toString())
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                Log.d(ContentValues.TAG, "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
            }
        }

        verifyNum.setOnClickListener (this)

        verifyEmail.setOnClickListener (this)

        submit.setOnClickListener (this)


        return view
    }


    private fun registerUser() {
        val credential: EmailAuthCredential = EmailAuthProvider.getCredential(
            email.text.toString().trim(),
            password.text.toString().trim()
        ) as EmailAuthCredential
        mAuth.currentUser!!.linkWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (isFarmer.isChecked) {
                    addToDataBase(
                        name.text.toString(),
                        phone.text.toString(),
                        email.text.toString(),
                        mAuth.currentUser?.uid!!.toString(),
                        true
                    )
                } else {
                    addToDataBase(
                        name.text.toString(),
                        phone.text.toString(),
                        email.text.toString(),
                        mAuth.currentUser?.uid!!.toString(),
                        false
                    )
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error Message: " + task.exception!!.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "signInWithCredential:success")
                    Toast.makeText(requireContext(), "Phone Number Verified ", Toast.LENGTH_LONG)
                        .show()
                    submit.isFocusable = true

                } else {
                    var message: String = task.exception!!.message.toString()
                    Log.d("error", message)
                    Toast.makeText(requireContext(), task.exception!!.message, Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    private fun addToDataBase(
        name: String,
        phone: String,
        email: String,
        uid: String,
        isFarmer: Boolean,
    ) {
        database = FirebaseDatabase.getInstance().reference.child("users").child(uid)
        val map = HashMap<String, Any>()
        map["name"] = name
        map["phone"] = phone
        map["email"] = email
        map["uid"] = uid
        map["image"] = ""
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

    private fun emptyCheck(): Boolean {
        if (name.text.toString().isNotEmpty() && phone.text.toString()
                .isNotEmpty() && email.text.toString().isNotEmpty() && password.text.toString()
                .isNotEmpty() && re_password.text.toString().isNotEmpty()
        ) {
            if (password.text.toString() == re_password.text.toString()) return true
        }
        return false
    }

    override fun onClick(view: View?) {
        when (view) {
            verifyNum -> {
                phoneOtp.visibility = View.VISIBLE
                phone.visibility = View.GONE
                if (verifyNum.text == "Verify Otp") {
                    if (phoneOtp.text.isNotEmpty()) {
                        verifyPhoneNumberWithCode(
                            storedVerificationId,
                            phoneOtp.text.toString().trim()
                        )
                    }
                } else {
                    if (phone.text.toString().isNotEmpty()) {
                        val number = phone.text.toString().trim()
                        verifyNum.text = "Verify Otp"
                        startPhoneNumberVerification(number)
                    }
                }
            }

            verifyEmail -> {
                mAuth.currentUser?.sendEmailVerification()!!.addOnSuccessListener {
                    Toast.makeText(requireContext(), "Verification Sent", Toast.LENGTH_SHORT).show()
                }
            }

            submit -> {
                if (emptyCheck()) {
                    registerUser()
                } else {
                    Log.d("error", "Failed")
                }
            }
        }
    }


}