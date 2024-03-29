package com.example.bookapp.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bookapp.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CategoryAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryAddBinding
    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    // progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        // configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait!")
        progressDialog.setCanceledOnTouchOutside(false)

        // back button click
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        // submit buttton click
        binding.submitBtn.setOnClickListener {
            validateData()
        }


    }

    private var category = ""

    private fun validateData() {
        // extract data to be added
        category = binding.categoryEt.text.toString().trim()
        // validation check
        if(category.isEmpty()){
            Toast.makeText(this, "Enter Category Title", Toast.LENGTH_SHORT).show()
        }else{
            addCategoryToFirebase()
        }
    }

    private fun addCategoryToFirebase() {
        progressDialog.show()
        val timestamp = System.currentTimeMillis()
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"
        // add to firebase realtime db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add. Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }


    }
}