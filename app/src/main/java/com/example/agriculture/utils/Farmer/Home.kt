package com.example.agriculture.utils.Farmer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agriculture.R
import com.example.agriculture.adapter.ProductAdapter
import com.example.agriculture.model.Product
import com.example.agriculture.model.User
import com.example.agriculture.utils.Detail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage

class Home : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var productList: ArrayList<Product>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var searchLayout: LinearLayout
    private lateinit var searchView: SearchView
    private lateinit var filter: ImageView
    private lateinit var recyclerViewUser: RecyclerView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerViewUser = view.findViewById(R.id.rv_user)
        searchLayout = view.findViewById(R.id.searchLayout)
        searchView = view.findViewById(R.id.searchView)
        filter = view.findViewById(R.id.filter)
        productList = ArrayList()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewUser.layoutManager = LinearLayoutManager(requireContext())
        adapter = ProductAdapter(productList)
        recyclerView.setHasFixedSize(true)
        recyclerViewUser.setHasFixedSize(true)
        mAuth = FirebaseAuth.getInstance()
        FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)!!
                if(!user.getIsFarmer()){
                    searchLayout.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    recyclerViewUser.visibility = View.VISIBLE

                    FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(postSnapshot in snapshot.children){
                                val users = postSnapshot.getValue(User::class.java)!!
                                if(users.getIsFarmer()){
                                    FirebaseDatabase.getInstance().reference.child("users").child(users.getUserUid()).child("products").addValueEventListener(object: ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for(productSnapshot in snapshot.children){
                                                val product = productSnapshot.getValue(Product::class.java)!!
                                                productList.add(product)
                                                recyclerViewUser.adapter = adapter
                                                adapter.setOnItemClickListener(object: ProductAdapter.onItemClickListener{
                                                    override fun onItemClick(position: Int) {
                                                        val intent = Intent(requireContext(), Detail::class.java)
                                                        intent.putExtra("name", productList[position].product_name)
                                                        intent.putExtra("desc", productList[position].product_desc)
                                                        intent.putExtra("image", productList[position].product_img)
                                                        intent.putExtra("qty", productList[position].product_qty)
                                                        intent.putExtra("uid", productList[position].product_uid)
                                                        startActivity(intent)
                                                    }

                                                })
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }

                                    })
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                }
                else{
                    FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid).child("products").addValueEventListener(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            productList.clear()
                            for(postSnapshot in snapshot.children){
                                val product = postSnapshot.getValue(Product::class.java)
                                productList.add(product!!)
                                recyclerView.adapter = adapter
                                adapter.setOnItemClickListener(object: ProductAdapter.onItemClickListener{
                                    override fun onItemClick(position: Int) {
                                        val intent = Intent(requireContext(), Detail::class.java)
                                        intent.putExtra("name", productList[position].product_name)
                                        intent.putExtra("desc", productList[position].product_desc)
                                        intent.putExtra("image", productList[position].product_img)
                                        intent.putExtra("qty", productList[position].product_qty)
                                        intent.putExtra("uid", productList[position].product_uid)
                                        startActivity(intent)
                                    }

                                })
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
        return view
    }
}