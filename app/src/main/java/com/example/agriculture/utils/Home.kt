package com.example.agriculture.utils

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agriculture.R
import com.example.agriculture.adapter.ProductAdapter
import com.example.agriculture.model.Product
import com.example.agriculture.utils.Farmer.DetailFarmer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class Home : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var productList: ArrayList<Product>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var firebaseStorage: FirebaseStorage
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        productList = ArrayList()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ProductAdapter(productList)
        recyclerView.setHasFixedSize(true)
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid).child("products")
        database.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for(postSnapshot in snapshot.children){
                    val product = postSnapshot.getValue(Product::class.java)
                    productList.add(product!!)
                    Log.d("product", product.product_name)
                    recyclerView.adapter = adapter
                    adapter.notifyDataSetChanged()
                    val bundle = Bundle()
                    adapter.setOnItemClickListener(object: ProductAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val intent = Intent(requireContext(), DetailFarmer::class.java)
                            intent.putExtra("name", productList[position].product_name)
                            intent.putExtra("desc", productList[position].product_desc)
                            intent.putExtra("image", productList[position].product_img)
                            intent.putExtra("qty", productList[position].product_qty)
                            startActivity(intent)
                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        adapter.notifyDataSetChanged()
        return view
    }
}