package com.example.agriculture.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.agriculture.R
import com.example.agriculture.model.Product
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage


class Products : Fragment() {
    private lateinit var product_name: EditText
    private lateinit var product_desc: EditText
    private lateinit var get_image: Button
    private lateinit var upload: Button
    private lateinit var database: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var image_upload: ImageView
    private lateinit var fileUri: Uri
    private lateinit var progressBar: ProgressBar
    private lateinit var total_products: TextView
    private lateinit var view_products: Button
    private lateinit var productList: ArrayList<Product>
    private lateinit var reduce: FloatingActionButton
    private lateinit var add: FloatingActionButton
    private lateinit var quantity: TextView
    private lateinit var startForProfileImageResult: ActivityResultLauncher<Intent>

    companion object{
        const val RESULT_OK = 1101
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startForProfileImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        //Image Uri will not be null for RESULT_OK
                        fileUri = data?.data!!
                        image_upload.setImageURI(fileUri)
                        image_upload.visibility = View.VISIBLE
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_products, container, false)
        product_name = view.findViewById(R.id.product_name_frag)
        product_desc = view.findViewById(R.id.product_desc)
        get_image = view.findViewById(R.id.get_image)
        upload = view.findViewById(R.id.upload)
        image_upload = view.findViewById(R.id.image_upload)
        progressBar = view.findViewById(R.id.progressBar)
        total_products = view.findViewById(R.id.total_products)
        view_products = view.findViewById(R.id.view_products)
        reduce = view.findViewById(R.id.reduce)
        add = view.findViewById(R.id.add)
        quantity = view.findViewById(R.id.quantity)
        productList = ArrayList()
        mAuth = FirebaseAuth.getInstance()

        var count = 0

        reduce.setOnClickListener {
            if(quantity.text.toString().toInt() > 0){
                count -= 1
                quantity.text = count.toString()
            }
        }
        add.setOnClickListener {
            count += 1
            quantity.text = count.toString()
        }

        view_products.setOnClickListener {
            makeCurrentScreen(Home())
        }

        get_image.setOnClickListener {
            ImagePicker.Companion.with(requireActivity()).createIntent { intent -> startForProfileImageResult.launch(intent) }
        }

        upload.setOnClickListener {
            val pname = product_name.text.toString()
            val pdesc = product_desc.text.toString()
            addToDatabase(pname, pdesc)
        }

        database = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid).child("products")
        database.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    val product = postSnapshot.getValue(Product::class.java)!!
                    Log.d("product", product.product_name)
                    productList.add(product)
                }
                Log.d("product", productList.size.toInt().toString())
                total_products.text = productList.size.toString()
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        return view
    }

    private fun makeCurrentScreen(fragment: Fragment) {
        requireFragmentManager().beginTransaction().apply {
            replace(R.id.wrapperFrame, fragment)
            commit()
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun addToDatabase(pname: String, pdesc: String) {
        var image_path = " "
        val storageRef = FirebaseStorage.getInstance().reference;
        val uploadImageRef = storageRef.child("images/"+fileUri.lastPathSegment);
        val uploadTask = uploadImageRef.putFile(fileUri);

        uploadTask.addOnSuccessListener { snapshot ->
            snapshot!!.storage.downloadUrl
                .addOnCompleteListener(OnCompleteListener<Uri?> { task ->
                    image_path = task.result.toString()
                    if(progressBar.progress == 100){
                        Log.d("done", "done")
                        Log.d("imagePath", image_path)
                        database = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid).child("products").child(pname)
                        if(quantity.text.toString().toInt() < 1){
                            Toast.makeText(requireContext(), "Quantity cannot be less than 1", Toast.LENGTH_SHORT).show()
                        }
                        else if(product_name.text.toString().isEmpty()){
                            Toast.makeText(requireContext(), "Please write product name", Toast.LENGTH_SHORT).show()
                        }
                        else if(image_path.isEmpty()){
                            Toast.makeText(requireContext(), "Please write product image", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            val product = Product(pname, pdesc, image_path, quantity.text.toString().toInt())
                            database.setValue(product).addOnSuccessListener {
                                Toast.makeText(requireActivity(), "Product Added", Toast.LENGTH_SHORT).show()
                                product_name.setText("")
                                product_desc.setText("")
                                image_upload.setImageURI(Uri.parse(""))
                                progressBar.resetPivot()
                                quantity.text = "0"

                            }.addOnFailureListener {
                                Toast.makeText(requireActivity(), "Error - " + it.message.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
        }.addOnFailureListener {
            Toast.makeText(
                requireActivity(),
                "Failed to Upload Image",
                Toast.LENGTH_SHORT
            ).show();
        }
            .addOnProgressListener { snapshot ->
            progressBar.max = 100
            val progress: Double = 100.0 * snapshot.bytesTransferred / snapshot.totalByteCount
            progressBar.progress = progress.toInt()
//            val progressString = progress.toInt().toString() + "% done"
//            image_path = progressString
//            if(progressBar.progress == 100){
//                Log.d("done", "done")
////               helperFunction(pname, pdesc, image_path)
//            }
        }
    }

    private fun helperFunction(pname:String, pdesc:String, image_path: String) {
//        var currentUser: User? = null
//        val userDatabase = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid)
//        userDatabase.addValueEventListener(object: ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val user = snapshot.getValue(User::class.java)
//                if(user!!.getIsFarmer()){
//                    Log.d("user", user.uid)
//                    currentUser = user
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//        })
    }
}