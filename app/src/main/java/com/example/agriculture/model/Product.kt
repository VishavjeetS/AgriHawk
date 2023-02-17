package com.example.agriculture.model

class Product: java.io.Serializable{
    var product_name: String = ""
    var product_price: String = ""
    var product_img: String = ""
    var product_qty: String = ""
    var product_uid: String = ""
    var product_loc: Location = Location(0.0, 0.0, "")

    constructor(){}
    constructor(product_name: String,
                 product_price: String,
                 product_img: String,
                 product_qty: String,
                 product_uid: String){
        this.product_name = product_name
        this.product_price = product_price
        this.product_img = product_img
        this.product_qty = product_qty
        this.product_uid = product_uid

    }

    constructor(product_name: String,
                product_price: String,
                product_img: String,
                product_qty: String,
                product_uid: String,
                product_loc: Location){
        this.product_name = product_name
        this.product_price = product_price
        this.product_img = product_img
        this.product_qty = product_qty
        this.product_uid = product_uid
        this.product_loc = product_loc
    }
}
