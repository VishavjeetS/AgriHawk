package com.example.agriculture.utils

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.agriculture.Dashboard
import com.example.agriculture.R
import com.example.agriculture.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class Detail : AppCompatActivity(), View.OnClickListener {
    private lateinit var uploaderCredentials    : LinearLayout
    private lateinit var builder        : AlertDialog.Builder
    private lateinit var requestDetails : Button
    private lateinit var productName    : TextView
    private lateinit var productPrice   : TextView
    private lateinit var farmerLoc      : TextView
    private lateinit var farmerName     : TextView
    private lateinit var farmerEmail    : TextView
    private lateinit var farmerPhone    : TextView
    private lateinit var productQty     : TextView
    private lateinit var image          : ImageView
    private lateinit var back           : ImageView
    private lateinit var mAuth          : FirebaseAuth
    private lateinit var dial           : Button
    private lateinit var qtyUpdate      : EditText
    private lateinit var qtyEdit        : Button
    private lateinit var deleteProduct  : Button
    private var currentUser             : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_farmer)
        supportActionBar?.hide()

        initialize()

        getDetails()

        getRequestStatus()

    }

    private fun initialize(){
        productName = findViewById(R.id.product_name_detail)
        productPrice = findViewById(R.id.product_price_detail)
        farmerName = findViewById(R.id.farmer_name_detail)
        farmerEmail = findViewById(R.id.farmer_email_detail)
        farmerPhone = findViewById(R.id.farmer_phone_detail)
        productQty = findViewById(R.id.qty_detail)
        farmerLoc = findViewById(R.id.farmer_loc_detail)
        image = findViewById(R.id.image_detail)
        uploaderCredentials = findViewById(R.id.uploader_credentials)
        requestDetails = findViewById(R.id.request_details)
        back = findViewById(R.id.back)
        dial = findViewById(R.id.dialFarmer)
        qtyUpdate = findViewById(R.id.qty_update)
        qtyEdit = findViewById(R.id.qty_edit)
        deleteProduct = findViewById(R.id.delete)

        mAuth = FirebaseAuth.getInstance()
        builder = AlertDialog.Builder(this)

        deleteProduct.setOnClickListener(this)
        back.setOnClickListener(this)
        requestDetails.setOnClickListener(this)
        dial.setOnClickListener(this)
        qtyEdit.setOnClickListener(this)
    }
    private fun getDetails(){
        FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)!!
                    val intent = intent.extras
                    if (intent != null) {
                        productName.text = intent.getString("name")
                        val price = intent.getString("price")
                        productPrice.text = "Rs ${price}/Kg"
                        productQty.text = intent.getString("qty")
                        val loc = intent.getSerializable("loc") as Location
                        farmerLoc.text = loc.address
                        val img = intent.getString("image")
                        Picasso.get().load(img).resize(image.width, 350).centerCrop().into(image);
                    }
                    if (user.getIsFarmer()) {
                        farmerName.text = user.getUserName()
                        farmerEmail.text = user.getUserEmail()
                        farmerPhone.text = user.getUserPhone()
                        uploaderCredentials.visibility = View.VISIBLE
                        requestDetails.visibility = View.GONE
                        deleteProduct.visibility = View.VISIBLE
                        qtyUpdate.visibility = View.GONE
                    }
                    else{
                        uploaderCredentials.visibility = View.GONE
                        requestDetails.visibility = View.VISIBLE
                        deleteProduct.visibility = View.GONE
                        qtyEdit.visibility = View.GONE
                        qtyUpdate.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
    private fun deleteModule() {
        builder.setMessage("Do you want to delete this product?")
            .setCancelable(false)
            .setPositiveButton("Yes", object: DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    val database = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid).child("products")
                    database.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(postSnapshot in snapshot.children){
                                val product = postSnapshot.getValue(Product::class.java)!!
                                Log.d("name", product.product_name)
                                if(intent.extras!!.getString("name") == product.product_name){
                                    postSnapshot.ref.removeValue().addOnSuccessListener {
                                        startActivity(Intent(this@Detail, Dashboard::class.java))
                                        this@Detail.finish()
                                    }
                                    break
                                }
                                else Log.d("not if", "nai aaya")
                            }
                        }

                        override fun onCancelled(it: DatabaseError) {
                            Toast.makeText(this@Detail, it.message.toString(), Toast.LENGTH_LONG).show()
                            Log.d("error", it.message.toString())
                        }

                    })
                }
            })
            .setNegativeButton("No") { p0, p1 -> TODO("Not yet implemented") };
        //Creating dialog box
        val alert = builder.create();
        //Setting the title manually
        alert.setTitle("Delete Product");
        alert.show();
    }
    private fun setRequestStatus(){
        val upperDb = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid)
        upperDb.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)!!
                    Log.d("uid", intent.extras!!.getString("uid")!!)
                    val database = FirebaseDatabase.getInstance().reference.child("users").child(intent.extras!!.getString("uid")!!).child("products")
                    database.addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for(postSnapshot in snapshot.children){
                                    val product = postSnapshot.getValue(Product::class.java)!!
                                    if(intent.extras!!.getString("name")!! == product.product_name) {
                                        val requestObj = Requests("pending", product.product_name, user.name, user.phone, user.email, user.uid, user.image)
                                        val request_item = RequestItem("pending", product.product_name)
                                        FirebaseDatabase.getInstance().reference.child("users")
                                            .addValueEventListener(object: ValueEventListener{
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    for(postUid in snapshot.children){
                                                        val fuser = postUid.getValue(User::class.java)!!
                                                        if(fuser.getUserUid() == intent.extras!!.getString("uid")!!){
                                                            upperDb.child("requests").child(fuser.name).child(product.product_name).setValue(request_item).addOnSuccessListener {
                                                                postSnapshot.child("requests").child(user.name).ref.setValue(requestObj)
                                                                    .addOnSuccessListener {
                                                                        Toast.makeText(this@Detail, "Request Sent", Toast.LENGTH_SHORT).show()
                                                                    }
                                                            }
                                                        }
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    TODO("Not yet implemented")
                                                }

                                            })
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
    private fun getRequestStatus(){
        FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid).child("requests")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(user_snapshot: DataSnapshot) {
                    FirebaseDatabase.getInstance().reference.child("users").child(intent.extras!!.getString("uid")!!)
                        .addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val fUser = snapshot.getValue(User::class.java)!!
                                currentUser = fUser
                                farmerName.text = fUser.name
                                farmerEmail.text = fUser.email
                                farmerPhone.text = fUser.phone
                                Log.d("user", fUser.name)
                                snapshot.child("products").ref.addValueEventListener(object :ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for(postProducts in snapshot.children){
                                            val product = postProducts.getValue(Product::class.java)!!
                                            if(product.product_name == intent.extras!!.getString("name")){
                                                user_snapshot.child(fUser.name).child(product.product_name).ref.addValueEventListener(object : ValueEventListener{
                                                    @SuppressLint("SetTextI18n")
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        for(postRequests in snapshot.children){
                                                            val requestItem = snapshot.getValue(RequestItem::class.java)!!
                                                            Log.d("item", requestItem.request_item)
                                                            when (requestItem.request_status) {
                                                                "pending" -> {
                                                                    requestDetails.text = "Request Sent"
                                                                    requestDetails.isFocusable = false
                                                                    uploaderCredentials.visibility = View.GONE
                                                                    dial.visibility = View.GONE
                                                                }
                                                                "approved" -> {
                                                                    requestDetails.visibility = View.GONE
                                                                    uploaderCredentials.visibility = View.VISIBLE
                                                                    dial.visibility = View.VISIBLE
                                                                }
                                                                else -> {
                                                                    requestDetails.visibility = View.VISIBLE
                                                                    requestDetails.text = "Request Rejected"
                                                                    requestDetails.isFocusable = false
                                                                    uploaderCredentials.visibility = View.GONE
                                                                    dial.visibility = View.GONE
                                                                }
                                                            }
                                                        }
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {
                                                        TODO("Not yet implemented")
                                                    }

                                                })
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }

                                })
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
    @SuppressLint("SetTextI18n")
    override fun onClick(view: View?) {
        when(view){
            deleteProduct ->  {
                deleteModule()
            }
            back ->  {
                startActivity(Intent(this, Dashboard::class.java))
                this.finish()
            }
            requestDetails ->  {
                requestDetails.text = "Request Sent"
                requestDetails.isFocusable = false
                setRequestStatus()
            }

            dial ->  {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel: +91${currentUser!!.getUserPhone()}")
                startActivity(intent)
            }

            qtyEdit ->  {
                if(qtyEdit.text == "Save"){
                    val updatedQty = qtyUpdate.text.toString()
                    FirebaseDatabase.getInstance().reference.child("users").child(intent.extras!!.getString("uid")!!).addValueEventListener(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val map:HashMap<String, Any> = HashMap()
                            map["product_qty"] = "$updatedQty Kg"
                            snapshot.child("products").child(intent.extras!!.getString("name")!!).ref.updateChildren(map).addOnSuccessListener {
                                productQty.text = "$updatedQty Kg"
                                Toast.makeText(this@Detail, "Updated", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                }
                else{
                    qtyUpdate.visibility = View.VISIBLE
                    qtyEdit.text = "Save"
                }
            }
        }
    }
}