package com.example.agriculture.utils

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.agriculture.Dashboard
import com.example.agriculture.R
import com.example.agriculture.model.Product
import com.example.agriculture.model.RequestItem
import com.example.agriculture.model.Requests
import com.example.agriculture.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class Detail : AppCompatActivity() {
    private lateinit var product_name: TextView
    private lateinit var product_desc: TextView
    private lateinit var farmer_name: TextView
    private lateinit var farmer_email: TextView
    private lateinit var farmer_phone: TextView
    private lateinit var product_qty: TextView
    private lateinit var image: ImageView
    private lateinit var back: ImageView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var builder: AlertDialog.Builder
    private lateinit var uploader_credentials: LinearLayout
    private lateinit var request_details: Button
    private lateinit var dial: Button
    private lateinit var qty_update: EditText
    private lateinit var qty_edit: Button

    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_farmer)

        product_name = findViewById(R.id.product_name_detail)
        product_desc = findViewById(R.id.product_desc_detail)
        farmer_name = findViewById(R.id.farmer_name_detail)
        farmer_email = findViewById(R.id.farmer_email_detail)
        farmer_phone = findViewById(R.id.farmer_phone_detail)
        product_qty = findViewById(R.id.qty_detail)
        image = findViewById(R.id.image_detail)
        uploader_credentials = findViewById(R.id.uploader_credentials)
        request_details = findViewById(R.id.request_details)
        back = findViewById(R.id.back)
        dial = findViewById(R.id.dialFarmer)
        qty_update = findViewById(R.id.qty_update)
        qty_edit = findViewById(R.id.qty_edit)

        mAuth = FirebaseAuth.getInstance()
        builder = AlertDialog.Builder(this)

        back.setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
            this.finish()
        }

        val intent = intent.extras
        if (intent != null) {
            product_name.text = intent.getString("name")
            product_desc.text = intent.getString("desc")
            product_qty.text = intent.getString("qty")
            val img = intent.getString("image")
            Picasso.get().load(img).resize(image.width, 350).centerCrop().into(image);
        }

        FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if(!user!!.getIsFarmer()){
                        supportActionBar?.hide()
                        qty_edit.visibility = View.GONE
                        qty_update.visibility = View.GONE
                    }
                    else{
                        uploader_credentials.visibility = View.VISIBLE
                        request_details.visibility = View.GONE
                        qty_update.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        val database = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid)
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)!!
                if (user.getIsFarmer()) {
                    farmer_name.text = user.getUserName()
                    farmer_email.text = user.getUserEmail()
                    farmer_phone.text = user.getUserPhone()
                    uploader_credentials.visibility = View.VISIBLE
                    request_details.visibility = View.GONE
                }
                else{
                    uploader_credentials.visibility = View.GONE
                    request_details.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        getRequestStatus()

        request_details.setOnClickListener {
            request_details.text = "Request Sent"
            request_details.isFocusable = false
            setRequestStatus()
        }

        dial.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel: +91${currentUser!!.getUserPhone()}")
            startActivity(intent)

        }

        qty_edit.setOnClickListener {
            if(qty_edit.text == "Save"){
                val updatedQty = qty_update.text.toString()
                FirebaseDatabase.getInstance().reference.child("users").child(intent!!.getString("uid")!!).addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val map:HashMap<String, Any> = HashMap()
                        map["product_qty"] = "$updatedQty Kg"
                        snapshot.child("products").child(intent.getString("name")!!).ref.updateChildren(map).addOnSuccessListener {
                            product_qty.text = updatedQty
                            Toast.makeText(this@Detail, "Updated", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
            else{
                qty_update.visibility = View.VISIBLE
                qty_edit.text = "Save"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.detail_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit -> Toast.makeText(applicationContext, "Help", Toast.LENGTH_SHORT)
                .show()
            R.id.delete -> {
                alert()
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private fun alert() {
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
                                val f_user = snapshot.getValue(User::class.java)!!
                                currentUser = f_user
                                farmer_name.text = f_user.name
                                farmer_email.text = f_user.email
                                farmer_phone.text = f_user.phone
                                Log.d("user", f_user.name)
                                snapshot.child("products").ref.addValueEventListener(object :ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for(postProducts in snapshot.children){
                                            val product = postProducts.getValue(Product::class.java)!!
                                            if(product.product_name == intent.extras!!.getString("name")){
                                                user_snapshot.child(f_user.name).child(product.product_name).ref.addValueEventListener(object : ValueEventListener{
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        for(postRequests in snapshot.children){
                                                            val request_item = snapshot.getValue(RequestItem::class.java)!!
                                                            Log.d("item", request_item.request_item)
                                                            if(request_item.request_status == "pending"){
                                                                request_details.text = "Request Sent"
                                                                request_details.isFocusable = false
                                                                uploader_credentials.visibility = View.GONE
                                                                dial.visibility = View.GONE
                                                            }
                                                            else if(request_item.request_status == "approved"){
                                                                request_details.visibility = View.GONE
                                                                uploader_credentials.visibility = View.VISIBLE
                                                                dial.visibility = View.VISIBLE
                                                            }
                                                            else{
                                                                request_details.visibility = View.VISIBLE
                                                                request_details.text = "Request Rejected"
                                                                request_details.isFocusable = false
                                                                uploader_credentials.visibility = View.GONE
                                                                dial.visibility = View.GONE
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
}