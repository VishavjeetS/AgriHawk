package com.example.agriculture

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agriculture.adapter.ResponseAdapter
import com.example.agriculture.model.Product
import com.example.agriculture.model.RequestItem
import com.example.agriculture.model.Requests
import com.example.agriculture.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Requests : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ResponseAdapter
    private lateinit var responseList: ArrayList<Requests>
    private lateinit var mAuth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_requests, container, false)
        recyclerView = view.findViewById(R.id.request_rv)
        mAuth = FirebaseAuth.getInstance()
        responseList = ArrayList()
        adapter = ResponseAdapter(requireContext(),responseList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid).child("products")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    responseList.clear()
                    for(postProducts in snapshot.children){
                        val product = postProducts.getValue(Product::class.java)!!
                        postProducts.child("requests").ref
                            .addValueEventListener(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for(postRequest in snapshot.children){
                                        Log.d("request children", postRequest.key.toString())
                                        val request = postRequest.getValue(Requests::class.java)!!
                                        Log.d("request", request.toString())
                                        responseList.add(request)
                                        recyclerView.adapter = adapter
                                        adapter.setOnItemClickListener(object: ResponseAdapter.onItemClickListener{
                                            override fun onItemClick(position: Int) {
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
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        return view
    }

}