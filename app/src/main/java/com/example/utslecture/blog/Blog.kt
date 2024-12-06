package com.example.utslecture.blog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.utslecture.R
import com.example.utslecture.data.Komentar
import com.example.utslecture.data.ProfileUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class Blog : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var commentAdapter: CommentAdapter
    private val commentsList: MutableList<Komentar> = mutableListOf()
    private var blogId: String = ""
    private var userId : String = ""
    private var username : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Firebase.firestore
        auth = Firebase.auth
        userId = auth.currentUser?.uid ?: ""
        if (userId.isNotEmpty()) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val profileUser = document.toObject(ProfileUser::class.java)
                        username = profileUser?.username ?: "Anonymous"
                    } else {
                        Log.d("BlogFragment", "No such document")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BlogFragment", "Error getting user document", e)
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_blog, container, false)

        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        val postCommentButton = view.findViewById<Button>(R.id.post_comment_button)
        postCommentButton.setOnClickListener {
            postComment()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            blogId = it.getString("blogId") ?: ""
            val title = it.getString("title")
            val content = it.getString("content")
            val image = it.getString("image")
            val blogUsername = it.getString("username")
            val uploadDate = it.getString("uploadDate")?.toLongOrNull()?.let { timestamp -> Date(timestamp) }
            view.findViewById<TextView>(R.id.detail_news_title).text = title
            view.findViewById<TextView>(R.id.detail_news_content).text = content

            val metadataTextView = view.findViewById<TextView>(R.id.detail_news_metadata)
            val relativeTime = uploadDate?.let {
                android.text.format.DateUtils.getRelativeTimeSpanString(
                    it.time,
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.MINUTE_IN_MILLIS
                ).toString()
            } ?: "Unknown time"
            Log.d("BlogFragment", "uploadDate: $uploadDate")
            Log.d("BlogFragment", "Relative time: $relativeTime")
            Log.d("BlogFragment", "Metadata text: ${metadataTextView.text}")

            metadataTextView.text = "$blogUsername • $relativeTime"

            Glide.with(this)
                .load(image)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(view.findViewById<ImageView>(R.id.detail_news_image))
            setupRecyclerView(view)
            loadComments()
        }

    }

    private fun setupRecyclerView(view: View) {
        commentAdapter = CommentAdapter(commentsList)
        val commentsRecyclerView = view.findViewById<RecyclerView>(R.id.comments_recycler_view)
        commentsRecyclerView.layoutManager = LinearLayoutManager(context)
        commentsRecyclerView.adapter = commentAdapter
    }

    private fun postComment() {
        val commentInput = view?.findViewById<EditText>(R.id.comment_input)
        val commentText = commentInput?.text.toString().trim()

        if (commentText.isNotEmpty() && blogId.isNotEmpty() && userId.isNotEmpty() && username.isNotEmpty()) {
            val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val newComment = Komentar(userId, blogId, commentText, username, currentDate)

            db.collection("comments")
                .add(newComment)
                .addOnSuccessListener { documentReference ->
                    Log.d("BlogFragment", "Comment added with ID: ${documentReference.id}")
                    commentInput?.text?.clear()
                    loadComments()
                    Toast.makeText(context, "Comment added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w("BlogFragment", "Error adding comment", e)
                    Toast.makeText(context, "Failed to add comment", Toast.LENGTH_SHORT).show()
                }
        } else {
            if (commentText.isBlank()) {
                Toast.makeText(context, "Comment is blank", Toast.LENGTH_SHORT).show()
            }

            if (blogId.isEmpty()) {
                Toast.makeText(context, "blogId is empty", Toast.LENGTH_SHORT).show()
            }

            if(username.isEmpty()) {
                Toast.makeText(context, "username is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadComments() {
        if (blogId.isNotEmpty()) {
            db.collection("comments")
                .whereEqualTo("blogId", blogId)
                .orderBy("uploadDate")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("BlogFragment", "listen:error", e)
                        return@addSnapshotListener
                    }

                    commentsList.clear()
                    for (dc in snapshots!!.documentChanges) {
                        when (dc.type) {
                            com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                                val comment = dc.document.toObject(Komentar::class.java)
                                commentsList.add(comment)
                            }
                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                val comment = dc.document.toObject(Komentar::class.java)
                                val index = commentsList.indexOfFirst { it.uploadDate == comment.uploadDate }
                                if (index != -1) {
                                    commentsList[index] = comment
                                }

                            }
                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                val comment = dc.document.toObject(Komentar::class.java)
                                commentsList.remove(comment)
                            }
                        }
                    }
                    commentAdapter.notifyDataSetChanged()
                }
        }
    }
}