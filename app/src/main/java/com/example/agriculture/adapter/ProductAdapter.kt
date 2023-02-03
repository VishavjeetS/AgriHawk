package com.example.agriculture.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.agriculture.R
import com.example.agriculture.model.Product
import com.example.agriculture.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ProductAdapter(private val productList: ArrayList<Product>): RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    lateinit var mListener: onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position: Int);
    }
    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

    class ProductViewHolder(itemView: View, private var listener: onItemClickListener): ViewHolder(itemView){
        fun onBind(product: Product){
            val database = FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid)
            database.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)!!
                    if(user.getIsFarmer())
                        itemView.findViewById<TextView>(R.id.farmer_name).text = user.getUserName()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
            itemView.findViewById<TextView>(R.id.product_name).text = product.product_name
            if(product.product_desc.length > 20){
                itemView.findViewById<TextView>(R.id.product_desc).text = product.product_desc.substring(0, 19) + "..."
            }
            else itemView.findViewById<TextView>(R.id.product_desc).text = product.product_desc
            Picasso.get().load(product.product_img).into(itemView.findViewById<ImageView>(R.id.product_image));
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.dashboard_layout, parent, false), mListener
        )
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.onBind(productList[position])
    }
}