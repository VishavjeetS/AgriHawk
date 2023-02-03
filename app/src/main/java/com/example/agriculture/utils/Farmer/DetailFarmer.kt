package com.example.agriculture.utils.Farmer

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.agriculture.Dashboard
import com.example.agriculture.R
import com.example.agriculture.authentication.Authentication
import com.example.agriculture.model.Product
import com.example.agriculture.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class DetailFarmer : AppCompatActivity() {
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
        back = findViewById(R.id.back)

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
            product_qty.text = intent.getInt("qty").toString()
            val img = intent.getString("image")
            Picasso.get().load(img).resize(image.width, 350).centerCrop().into(image);
        }

        val database =
            FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid)
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)!!
                if (user.getIsFarmer()) {
                    farmer_name.text = user.getUserName()
                    farmer_email.text = user.getUserEmail()
                    farmer_phone.text = user.getUserPhone()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
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
                                        startActivity(Intent(this@DetailFarmer, Dashboard::class.java))
                                        this@DetailFarmer.finish()
                                    }
                                    break
                                }
                                else Log.d("not if", "nai aaya")
                            }
                        }

                        override fun onCancelled(it: DatabaseError) {
                            Toast.makeText(this@DetailFarmer, it.message.toString(), Toast.LENGTH_LONG).show()
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
}