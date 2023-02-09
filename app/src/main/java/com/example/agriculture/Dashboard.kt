package com.example.agriculture

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.agriculture.authentication.Authentication
import com.example.agriculture.model.User
import com.example.agriculture.utils.Farmer.Home
import com.example.agriculture.utils.Farmer.Products
import com.example.agriculture.utils.Farmer.Profile
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Dashboard : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var navBar: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        supportActionBar?.hide()
        navBar = findViewById(R.id.bottom_navBar)
        mAuth = FirebaseAuth.getInstance()

        val home = Home()
        val products = Products()
        val profile = Profile()

        makeCurrentScreen(home)

        val database = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid)
        database.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("id", mAuth.currentUser!!.uid)
                val user = snapshot.getValue(User::class.java)!!
                if(!user.getIsFarmer()){
                    navBar.menu.findItem(R.id.products).isVisible = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        navBar.setOnNavigationItemSelectedListener {
            when (it.itemId){
                R.id.home -> {
                    makeCurrentScreen(home)
                }
                R.id.products -> {
                    makeCurrentScreen(products)
                }
                R.id.profile -> {
                    makeCurrentScreen(profile)
                }
            }
            true
        }
    }

    private fun makeCurrentScreen(fragment: Fragment) = supportFragmentManager.beginTransaction().apply {
        replace(R.id.wrapperFrame, fragment)
        commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_items, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.help_item -> Toast.makeText(applicationContext, "Help", Toast.LENGTH_SHORT)
                .show()
            R.id.logout_item -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(applicationContext, Authentication::class.java)
                Toast.makeText(applicationContext,"Log out", Toast.LENGTH_SHORT).show()
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item);
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }
}