package com.example.agriculture.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.agriculture.Dashboard
import com.example.agriculture.adapter.AuthenticationPagerAdapter
import com.example.agriculture.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class Authentication : AppCompatActivity(), FirebaseAuth.AuthStateListener {
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPagerAdapter: AuthenticationPagerAdapter
    private val titles = arrayListOf("Login", "SignUp")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        supportActionBar?.hide()

        viewPager2 = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)
        viewPagerAdapter = AuthenticationPagerAdapter(this)
        viewPager2.adapter = viewPagerAdapter
        TabLayoutMediator(tabLayout, viewPager2){tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(this)
        if(FirebaseAuth.getInstance().currentUser!=null){
            val intent = Intent(this,Dashboard::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onAuthStateChanged(p0: FirebaseAuth) {
//        if(p0.currentUser!=null){
//            val intent = Intent(this,MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(this)
    }
}