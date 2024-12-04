package com.example.utslecture.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utslecture.R
import com.example.utslecture.blog.BlogAdapter
import com.example.utslecture.auth.BaseAuth
import com.example.utslecture.data.Blog
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class Home : BaseAuth() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as HomeActivity).showBottomNavigation()
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newsRecyclerView = view.findViewById<RecyclerView>(R.id.newsRecyclerView)

        db.collection("blogs")
            .get()
            .addOnSuccessListener { documents ->
                val blogs = documents.mapNotNull { document ->
                    document.toObject(Blog::class.java)
                }

                val adapter = BlogAdapter(blogs) { blog ->
                    val bundle = bundleOf(
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