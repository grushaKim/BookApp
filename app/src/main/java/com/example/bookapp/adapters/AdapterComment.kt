package com.example.bookapp.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookapp.MyApplication
import com.example.bookapp.R
import com.example.bookapp.databinding.RowCommentBinding
import com.example.bookapp.models.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterComment: RecyclerView.Adapter<AdapterComment.HolderComment> {

    //firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth
    //context
    private var context: Context
    //arraylist for comment holder
    var commentArrayList: ArrayList<ModelComment>

    //viewBinding
    private lateinit var binding: RowCommentBinding

    constructor(context: Context, commentArrayList: ArrayList<ModelComment>) : super() {
        this.context = context
        this.commentArrayList = commentArrayList
    }

    //inflate row_comment.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComment(binding.root)
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        //init auth
        firebaseAuth = FirebaseAuth.getInstance()

        //get data
        val model = commentArrayList[position]
        val date = MyApplication.formatTimeStamp(model.timestamp.toLong())

        holder.dateTv.text = date
        holder.commentTv.text = model.comment

        //get profileImage and name of the user
        loadUserDetails(model, holder)
        //delete comment
        holder.itemView.setOnClickListener {
            //check if the user logged in and matches the writer of the comment
          if(firebaseAuth.currentUser != null && firebaseAuth.currentUser!!.uid == model.uid){
                deleteCommentDialog(model, holder)
          }else if(firebaseAuth.currentUser == null){
              Toast.makeText(context, "Please log in", Toast.LENGTH_SHORT).show()
          }else if(firebaseAuth.currentUser!!.uid != model.uid){
              Toast.makeText(context, "The comment made by another user", Toast.LENGTH_SHORT).show()
          }
        }
    }

    private fun deleteCommentDialog(model: ModelComment, holder: AdapterComment.HolderComment) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete comment")
            .setMessage("Do you want to delete this comment?")
            .setPositiveButton("Delete"){ dialog, e ->
                dialog.dismiss()
                //delete from DB
                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(model.bookId).child("comments").child(model.id)
                    .removeValue()
                    .addOnSuccessListener {
                     Toast.makeText(context, "Deleted your comment.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to delete comment. Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel"){dialog, e ->
                dialog.dismiss()
            }
            .show()
    }

    private fun loadUserDetails(model: ModelComment, holder: AdapterComment.HolderComment) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")

        ref.child(model.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    //set data
                    holder.nameTv.text = name
                    try{
                        Glide.with(context)
                            .load(profileImage)
                            .into(holder.profileIv)
                    }catch(e: Exception){
                        holder.profileIv.setImageResource(R.drawable.ic_person_gray)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun getItemCount(): Int {
        return commentArrayList.size
    }

    inner class HolderComment(itemView: View): RecyclerView.ViewHolder(itemView){
        // init ui view
        var profileIv = binding.profileIv
        var nameTv = binding.nameTv
        var dateTv = binding.dateTv
        var commentTv = binding.commentTv
    }

}