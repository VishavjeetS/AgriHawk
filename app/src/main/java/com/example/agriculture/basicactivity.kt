package com.example.agriculture

import android.app.Dialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

open class basicactivity : AppCompatActivity() {

    private lateinit var progressidalog:ProgressDialog
    fun showprogressdialog(text:String){
        progressidalog= Dialog(this) as ProgressDialog
        progressidalog.setContentView(R.layout.progressdialog)
        progressidalog.setCancelable(false)
        progressidalog.setCanceledOnTouchOutside(false)
        progressidalog.show()
    }
    fun hideprogressdialog(){
        progressidalog.dismiss()
    }
}