package com.example.agriculture.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.agriculture.R
import com.example.agriculture.model.User
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class Profile : Fragment() {
    private lateinit var user_name: TextView
    private lateinit var user_phone: TextView
    private lateinit var user_email: TextView
    private lateinit var user_account: TextView
    private lateinit var image_picker: ImageView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var startForProfileImageResult: ActivityResultLauncher<Intent>
    private var fileUri: Uri? = null
    private lateinit var user_image: CircleImageView
    private lateinit var name_update: EditText
    private lateinit var phone_update: EditText
    private lateinit var email_update: EditText
    private lateinit var image_path: String
    private lateinit var upload: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startForProfileImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        //Image Uri will not be null for RESULT_OK
                        fileUri = data?.data!!
                        user_image.setImageURI(fileUri)
                        uploadImage(fileUri!!)
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        user_name = view.findViewById(R.id.user_name)
        user_phone = view.findViewById(R.id.user_phone)
        user_email = view.findViewById(R.id.user_email)
        user_image = view.findViewById(R.id.user_image)
        image_picker = view.findViewById(R.id.profile_img_picker)
        name_update = view.findViewById(R.id.name_update)
        phone_update = view.findViewById(R.id.phone_update)
        email_update = view.findViewById(R.id.email_update)
        user_account = view.findViewById(R.id.user_account)
        upload = view.findViewById(R.id.profile_update)
        mAuth = FirebaseAuth.getInstance()

        upload.setOnClickListener {
            addToDatabase()
        }

        image_picker.setOnClickListener {
            ImagePicker.Companion.with(requireActivity()).createIntent { intent -> startForProfileImageResult.launch(intent) }
        }

        val database = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid)
        database.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)!!
                Log.d("user", user.name)
                user_name.text = user.name
                user_phone.text = user.phone
                user_email.text = "(${user.email})"
                name_update.setText(user.getUserName())
                email_update.setText(user.getUserEmail())
                phone_update.setText(user.getUserPhone())
                if(user.getIsFarmer()) user_account.text = "Farmer"
                else user_account.text = "User"
                Picasso.get().load(user.getUserImage()).into(user_image)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        return view
    }
    private fun addToDatabase() {
        val name = name_update.text
        val phone = phone_update.text
        val email = email_update.text

        val database = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid)
        database.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val map: HashMap<String, Any> = HashMap()
                map["name"] = name.toString()
                map["email"] = email.toString()
                map["phone"] = phone.toString()
                if(snapshot.exists()){
                    database.updateChildren(map).addOnSuccessListener {
                        Toast.makeText(requireContext(), "Details Updated", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Log.d("uploadError", it.message.toString())
                    }
                }
                else{
                    database.setValue(map).addOnSuccessListener {
                        Toast.makeText(requireContext(), "Details Updated", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Log.d("new", it.message.toString())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun uploadImage(fileUri: Uri) {
        image_path = " "
        val storageRef = FirebaseStorage.getInstance().reference;
        val uploadImageRef = storageRef.child("images/"+fileUri.lastPathSegment);
        val uploadTask = uploadImageRef.putFile(fileUri);
        uploadTask.addOnSuccessListener { snapshot ->
            snapshot!!.storage.downloadUrl
                .addOnCompleteListener(OnCompleteListener<Uri?> { task ->
                    image_path = task.result.toString()
                    val database = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid)
                    database.addValueEventListener(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(postSnapshot in snapshot.children){
                                val map: HashMap<String, Any> = HashMap()
                                map["image"] = image_path
                                database.updateChildren(map).addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Image Uploaded", Toast.LENGTH_SHORT).show()
                                }.addOnFailureListener {
                                    Log.d("error", it.message.toString())
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                })
        }.addOnFailureListener {
            Toast.makeText(
                requireActivity(),
                "Failed to Upload Image",
                Toast.LENGTH_SHORT
            ).show();
        }
    }
}