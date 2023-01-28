package com.example.agriculture

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.agriculture.databinding.ActivityFarmermainscreenBinding

class farmermainscreen : AppCompatActivity() {
    private lateinit var binding:ActivityFarmermainscreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityFarmermainscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
    /*val sharedpref=getSharedPreferences("farmermypref", MODE_PRIVATE)
val email=sharedpref.getString("email","")!!
        binding.usernameofloggedin.setText(email)*/

        var userdetails:user=user()
        if(intent.hasExtra("extraobject")){
            userdetails= intent.getSerializableExtra("extraobject") as user
        }
    }
}