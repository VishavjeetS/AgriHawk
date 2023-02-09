package com.example.agriculture.model

class User {
    var name: String = ""
    var phone: String = ""
    var email: String = ""
    var uid: String = ""
    var image: String = ""
    private var isFarmer: Boolean = false

    constructor(){}

    constructor(name: String, phone: String, uid: String, image: String){
        this.name = name
        this.phone = phone
        this.uid = uid
        this.image = image
    }
    constructor(name: String, phone: String, email: String, uid: String, image: String){
        this.name = name
        this.phone = phone
        this.email = email
        this.uid = uid
        this.image = image
    }

    fun getUserImage(): String{
        return image
    }
    fun getUserUid():String{
        return uid
    }
    fun getUserEmail(): String{
        return email
    }
    fun getUserPhone(): String{
        return phone
    }
    fun getUserName(): String{
        return name
    }
    fun getIsFarmer():Boolean{
        return isFarmer
    }
}
