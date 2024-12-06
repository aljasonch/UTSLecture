package com.example.utslecture.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utslecture.R
import com.example.utslecture.blog.BlogAdapter
import com.example.utslecture.auth.BaseAuth
import com.example.utslecture.data.Blog
import com.example.utslecture.data.ProfileUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

class Home : BaseAuth() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""
    private var username: String = ""
    private lateinit var usernameTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = Firebase.firestore
        auth = Firebase.auth
        userId = auth.currentUser?.uid ?: ""
        // Ambil data profileUser dari Firestore untuk mendapatkan username
        if (userId.isNotEmpty()) {
            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val profileUser = document.toObject(ProfileUser::class.java)
                        username = profileUser?.username ?: "Anonymous"
                        usernameTextView.text = "Hello, $username!"
                    } else {
                        Log.d("ProfileFragment", "No such document")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileFragment", "Error getting user document", e)
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as HomeActivity).showBottomNavigation()
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        usernameTextView = view.findViewById(R.id.username_profile)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val newsRecyclerView = view.findViewById<RecyclerView>(R.id.newsRecyclerView)
        usernameTextView.text = "Hello, $username!"
        db.collection("blogs")
            .get()
            .addOnSuccessListener { documents ->
                val blogs = documents.mapNotNull { document ->
                    document.toObject(Blog::class.java)
                }

                val adapter = BlogAdapter(blogs) { blog ->
                    val bundle = bundleOf(
                        "blogId" to blog.blogId,
                        "title" to blog.title,
                        "content" to blog.content,
                        "image" to blog.image,
                        "username" to blog.username,
                        "uploadDate" to blog.uploadDate?.time.toString()
                    )
                    findNavController().navigate(R.id.action_home_to_blog, bundle)
                }

                newsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                newsRecyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w("Home", "Error getting documents: ", exception)
            }
    }
}