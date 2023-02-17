package com.example.agriculture.utils.Farmer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.test.core.app.ApplicationProvider
import com.example.agriculture.R
import com.example.agriculture.model.Product
import com.example.agriculture.utils.Constants
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList


class Products : Fragment(), AdapterView.OnItemSelectedListener, View.OnClickListener{
    private lateinit var productName: EditText
    private lateinit var productDesc: EditText
    private lateinit var productPrice: EditText
    private lateinit var getImage: Button
    private lateinit var upload: Button
    private lateinit var database: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var imageUpload: ImageView
    private lateinit var fileUri: Uri
    private lateinit var progressBar: ProgressBar
    private lateinit var totalProducts: TextView
    private lateinit var viewProducts: Button
    private lateinit var productList: ArrayList<Product>
    private lateinit var reduce: FloatingActionButton
    private lateinit var add: FloatingActionButton
    private lateinit var quantity: TextView
    private lateinit var spin: Spinner
    private lateinit var startForProfileImageResult: ActivityResultLauncher<Intent>
    private var totalQty = 0
    private var num = 0
    private val qty = arrayListOf<String>("10 Kg", "20 Kg", "50 Kg")
    private val REQUEST_LOCATION_PERMISSION = 1
    var count = 0
    private var latitude = 0.0
    private var longitude = 0.0
    private var address = ""
    private lateinit var view: View
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startForProfileImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        //Image Uri will not be null for RESULT_OK
                        fileUri = data?.data!!
                        imageUpload.setImageURI(fileUri)
                        imageUpload.visibility = View.VISIBLE
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
    ): View {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_products, container, false)

        initialize()

        reduce.setOnClickListener(this)
        add.setOnClickListener(this)
        viewProducts.setOnClickListener(this)
        getImage.setOnClickListener(this)
        upload.setOnClickListener(this)

        showTotalProducts()

        return view
    }

    private fun makeCurrentScreen(fragment: Fragment) {
        requireFragmentManager().beginTransaction().apply {
            replace(R.id.wrapperFrame, fragment)
            commit()
        }
    }


    @SuppressLint("MissingPermission")
    private fun addToDatabase(pName: String, pPrice: String) {
        var imagePath = ""
        val storageRef = FirebaseStorage.getInstance().reference;
        val uploadImageRef = storageRef.child("images/"+fileUri.lastPathSegment);
        val uploadTask = uploadImageRef.putFile(fileUri);
        totalQty = num * quantity.text.toString().toInt()

        uploadTask.addOnSuccessListener { snapshot ->
            snapshot!!.storage.downloadUrl
                .addOnCompleteListener(OnCompleteListener<Uri?> { task ->
                    imagePath = task.result.toString()
                    if(progressBar.progress == 100){
                        database = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid).child("products").child(pName)
                        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                            if (location != null) {
                                // Use the location
                                latitude = location.latitude
                                longitude = location.longitude
                                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                                address = addresses?.get(0)!!.getAddressLine(0)
                                val pLocation = com.example.agriculture.model.Location(longitude, latitude, address)
                                val product = Product(pName, pPrice, imagePath, "$totalQty Kg", mAuth.currentUser!!.uid, pLocation)
                                database.setValue(product).addOnSuccessListener {
                                    Toast.makeText(requireActivity(), "Product Added", Toast.LENGTH_SHORT).show()
                                    productName.setText("")
                                    productPrice.setText("")
                                    imageUpload.setImageURI(Uri.parse(""))
                                    progressBar.progress = 0
                                    quantity.text = "0"

                                }.addOnFailureListener {
                                    Toast.makeText(requireActivity(), "Error - " + it.message.toString(), Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
                            }
                        }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
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
                if(quantity.text.toString().toInt() < 1){
                    Toast.makeText(requireContext(), "Quantity cannot be less than 1", Toast.LENGTH_SHORT).show()
                }
                else if(productName.text.toString().isEmpty()){
                    Toast.makeText(requireContext(), "Please write product name", Toast.LENGTH_SHORT).show()
                }
                else{
                    progressBar.max = 100
                    val progress: Double = 100.0 * snapshot.bytesTransferred / snapshot.totalByteCount
                    progressBar.progress = progress.toInt()
                }
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val currentItem = qty[p2]
        Log.d("current", currentItem)
        num = currentItem.substring(0, 2).toInt()
        Log.d("current", num.toString())
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun initialize(){
        productName    = view.findViewById(R.id.product_name_frag)
        productDesc    = view.findViewById(R.id.product_desc)
        productPrice   = view.findViewById(R.id.product_price)
        getImage       = view.findViewById(R.id.get_image)
        upload          = view.findViewById(R.id.upload)
        imageUpload    = view.findViewById(R.id.image_upload)
        progressBar     = view.findViewById(R.id.progressBar)
        totalProducts   = view.findViewById(R.id.total_products)
        viewProducts    = view.findViewById(R.id.view_products)
        reduce          = view.findViewById(R.id.reduce)
        add             = view.findViewById(R.id.add)
        quantity        = view.findViewById(R.id.quantity)
        spin            = view.findViewById(R.id.spinner1)
        productList     = ArrayList()
        mAuth           = FirebaseAuth.getInstance()

        spin.onItemSelectedListener = this;

        val aa = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, qty)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spin.adapter = aa
    }

    private fun showTotalProducts(){
        database = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid).child("products")
        database.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for(postSnapshot in snapshot.children){
                    val product = postSnapshot.getValue(Product::class.java)!!
                    productList.add(product)
                    totalProducts.text = productList.size.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    override fun onClick(view: View?) {
        when(view){
            reduce -> {
                if(quantity.text.toString().toInt() > 0){
                    count -= 1
                    quantity.text = count.toString()
                }
            }
            add -> {
                count += 1
                quantity.text = count.toString()
            }
            viewProducts ->  {
                makeCurrentScreen(Home())
            }

            getImage ->  {
                ImagePicker.Companion.with(requireActivity()).createIntent { intent -> startForProfileImageResult.launch(intent) }
            }

            upload ->  {
                val pName = productName.text.toString()
                val pPrice = productPrice.text.toString()
                addToDatabase(pName, pPrice)
            }
        }
    }
}