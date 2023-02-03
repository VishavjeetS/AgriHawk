package com.example.agriculture.model

class User {
    var name: String = ""
    var phone: String = ""
    var email: String = ""
    var uid: String = ""
    var image: String = ""
    private var isFarmer: Boolean = false

    fun getUserImage(): String{
        return image
    }
    fun setUserImage(image: String){
        this.image = image
    }

    fun setUserUid(uid: String){
        this.uid = uid
    }
    fun getUserUid():String{
        return uid
    }

    fun setUserEmail(email: String){
        this.email = email
    }
    fun getUserEmail(): String{
        return email
    }

    fun setUserPhone(phone: String){
        this.phone = phone
    }
    fun getUserPhone(): String{
        return phone
    }

    fun setUserName(name: String){
        this.name = name
    }
    fun getUserName(): String{
        return name
    }

    fun setIsFarmer(isFarmer:Boolean){
        this.isFarmer = isFarmer
    }
    fun getIsFarmer():Boolean{
        return isFarmer
    }
}
