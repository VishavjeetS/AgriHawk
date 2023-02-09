package com.example.agriculture.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.agriculture.R
import com.example.agriculture.model.Product
import com.example.agriculture.model.RequestItem
import com.example.agriculture.model.Requests
import com.example.agriculture.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ResponseAdapter(var context: Context, var responseUserList: ArrayList<Requests>):RecyclerView.Adapter<ResponseAdapter.ResponseViewHolder>() {
    lateinit var mListener: onItemClickListener
    private var mContext = context

    interface onItemClickListener{
        fun onItemClick(position: Int);
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

    class ResponseViewHolder(itemView: View, private var listener: onItemClickListener, private var context: Context):RecyclerView.ViewHolder(itemView){
        fun onBind(response: Requests){
            itemView.findViewById<TextView>(R.id.user_name_response).text = response.name
            itemView.findViewById<TextView>(R.id.user_product).text = response.request_product
            if(response.image.isEmpty()){
                itemView.findViewById<ImageView>(R.id.user_image_response).setImageResource(R.drawable.profile)
            }
            else{
                Picasso.get().load(response.image).into(itemView.findViewById<ImageView>(R.id.user_image_response))
            }
            when(response.request_status){
                "approved" -> {
                    itemView.findViewById<LinearLayout>(R.id.response_layout).visibility = View.GONE
                    itemView.findViewById<LinearLayout>(R.id.result_layout).visibility = View.VISIBLE
                    itemView.findViewById<TextView>(R.id.result_approved).visibility = View.VISIBLE
                    itemView.findViewById<TextView>(R.id.result_decline).visibility = View.GONE
                }
                "rejected" -> {
                    itemView.findViewById<LinearLayout>(R.id.response_layout).visibility = View.GONE
                    itemView.findViewById<LinearLayout>(R.id.result_layout).visibility = View.VISIBLE
                    itemView.findViewById<TextView>(R.id.result_decline).visibility = View.VISIBLE
                    itemView.findViewById<TextView>(R.id.result_approved).visibility = View.GONE
                }
                else -> {
                    itemView.findViewById<LinearLayout>(R.id.response_layout).visibility = View.VISIBLE
                    itemView.findViewById<LinearLayout>(R.id.result_layout).visibility = View.GONE
                }
            }
            itemView.findViewById<Button>(R.id.reject_response).setOnClickListener {
                val map: HashMap<String, Any> = HashMap()
                map["request_status"] = "rejected"
                FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userFarmer = snapshot.getValue(User::class.java)!!
                        snapshot.child("products").ref.addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for(postProducts in snapshot.children){
                                    val productsFarmer = postProducts.getValue(Product::class.java)!!
                                    postProducts.child("requests").ref.addValueEventListener(object:ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for(postRequests in snapshot.children){
                                                val requestsFarmer = postRequests.getValue(Requests::class.java)!!
                                                if(requestsFarmer.request_product == response.request_product){
                                                    postRequests.ref.updateChildren(map).addOnSuccessListener {
                                                        Log.d("user uid", requestsFarmer.uid)
                                                        FirebaseDatabase.getInstance().reference.child("users").child(requestsFarmer.uid).addValueEventListener(object:ValueEventListener{
                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                val userClient = snapshot.getValue(User::class.java)!!
                                                                snapshot.child("requests").ref.addValueEventListener(object: ValueEventListener{
                                                                    override fun onDataChange(snapshot: DataSnapshot, ) {
                                                                        for(postUserRequests in snapshot.children){
                                                                            postUserRequests.ref.addValueEventListener(object : ValueEventListener{
                                                                                override fun onDataChange(snapshot: DataSnapshot, ) {
                                                                                    for(requests in snapshot.children){
                                                                                        requests.ref.addValueEventListener(object : ValueEventListener{
                                                                                            override fun onDataChange(snapshot: DataSnapshot, ) {
                                                                                                Log.d("child", snapshot.key.toString())
                                                                                                val requestItem = snapshot.getValue(RequestItem::class.java)
                                                                                                Log.d("requestItem", requestItem.toString())
                                                                                                if(requestItem!!.request_item == response.request_product){
                                                                                                    Log.d("in if", "ha haiga a")
                                                                                                    snapshot.ref.updateChildren(map).addOnSuccessListener {
                                                                                                        Toast.makeText(context, "Rejected", Toast.LENGTH_SHORT).show()
                                                                                                    }
                                                                                                }
                                                                                            }

                                                                                            override fun onCancelled(error: DatabaseError, ) {
                                                                                                TODO(
                                                                                                    "Not yet implemented"
                                                                                                )
                                                                                            }

                                                                                        })
                                                                                    }
                                                                                }

                                                                                override fun onCancelled(
                                                                                    error: DatabaseError,
                                                                                ) {
                                                                                    TODO("Not yet implemented")
                                                                                }

                                                                            })
                                                                        }
                                                                    }

                                                                    override fun onCancelled(error: DatabaseError) {
                                                                        TODO("Not yet implemented")
                                                                    }

                                                                })
                                                            }

                                                            override fun onCancelled(error: DatabaseError) {
                                                                TODO("Not yet implemented")
                                                            }

                                                        })
                                                    }
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }

                                    })
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
            itemView.findViewById<Button>(R.id.accept_response).setOnClickListener {
                val map: HashMap<String, Any> = HashMap()
                map["request_status"] = "approved"
                FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userFarmer = snapshot.getValue(User::class.java)!!
                        snapshot.child("products").ref.addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for(postProducts in snapshot.children){
                                    val productsFarmer = postProducts.getValue(Product::class.java)!!
                                    postProducts.child("requests").ref.addValueEventListener(object:ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for(postRequests in snapshot.children){
                                                val requestsFarmer = postRequests.getValue(Requests::class.java)!!
                                                if(requestsFarmer.request_product == response.request_product){
                                                    postRequests.ref.updateChildren(map).addOnSuccessListener {
                                                        Log.d("user uid", requestsFarmer.uid)
                                                        FirebaseDatabase.getInstance().reference.child("users").child(requestsFarmer.uid).addValueEventListener(object:ValueEventListener{
                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                val userClient = snapshot.getValue(User::class.java)!!
                                                                snapshot.child("requests").ref.addValueEventListener(object: ValueEventListener{
                                                                    override fun onDataChange(snapshot: DataSnapshot, ) {
                                                                        for(postUserRequests in snapshot.children){
                                                                            postUserRequests.ref.addValueEventListener(object : ValueEventListener{
                                                                                override fun onDataChange(snapshot: DataSnapshot, ) {
                                                                                    for(requests in snapshot.children){
                                                                                        requests.ref.addValueEventListener(object : ValueEventListener{
                                                                                            override fun onDataChange(snapshot: DataSnapshot, ) {
                                                                                                Log.d("child", snapshot.key.toString())
                                                                                                val requestItem = snapshot.getValue(RequestItem::class.java)
                                                                                                Log.d("requestItem", requestItem.toString())
                                                                                                if(requestItem!!.request_item == response.request_product){
                                                                                                    Log.d("in if", "ha haiga a")
                                                                                                    snapshot.ref.updateChildren(map).addOnSuccessListener {
                                                                                                        Toast.makeText(context, "Approved", Toast.LENGTH_SHORT).show()
                                                                                                    }
                                                                                                }
                                                                                            }

                                                                                            override fun onCancelled(error: DatabaseError, ) {
                                                                                                TODO(
                                                                                                    "Not yet implemented"
                                                                                                )
                                                                                            }

                                                                                        })
                                                                                    }
                                                                                }

                                                                                override fun onCancelled(
                                                                                    error: DatabaseError,
                                                                                ) {
                                                                                    TODO("Not yet implemented")
                                                                                }

                                                                            })
                                                                        }
                                                                    }

                                                                    override fun onCancelled(error: DatabaseError) {
                                                                        TODO("Not yet implemented")
                                                                    }

                                                                })
                                                            }

                                                            override fun onCancelled(error: DatabaseError) {
                                                                TODO("Not yet implemented")
                                                            }

                                                        })
                                                    }
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }

                                    })
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            }
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResponseViewHolder {
        return ResponseViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.requests_layout, parent, false),mListener, mContext
        )
    }

    override fun getItemCount(): Int {
        return responseUserList.size
    }

    override fun onBindViewHolder(holder: ResponseViewHolder, position: Int) {
        holder.onBind(responseUserList[position])
    }
}