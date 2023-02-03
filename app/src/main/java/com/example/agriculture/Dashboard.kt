package com.example.agriculture

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.agriculture.authentication.Authentication
import com.example.agriculture.utils.Home
import com.example.agriculture.utils.Products
import com.example.agriculture.utils.Profile
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class Dashboard : AppCompatActivity() {
    private lateinit var navBar: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        navBar = findViewById(R.id.bottom_navBar)

        val home = Home()
        val products = Products()
        val profile = Profile()
        makeCurrentScreen(home)

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