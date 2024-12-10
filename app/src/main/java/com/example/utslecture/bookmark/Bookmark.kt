package com.example.utslecture.bookmark

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utslecture.R
import com.example.utslecture.blog.BlogAdapter
import com.example.utslecture.data.Blog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class Bookmark : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth
    private lateinit var recyclerViewBookmarked: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bookmark, container, false)

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.queryHint = "Search"

        recyclerViewBookmarked = view.findViewById(R.id.recyclerViewBookmarked)
        recyclerViewBookmarked.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewBookmarked.visibility = View.GONE

        view.findViewById<CardView>(R.id.politics_card).setOnClickListener {
            navigateToBookmarkedCategory("International Politics")
        }
        view.findViewById<CardView>(R.id.finance_card).setOnClickListener {
            navigateToBookmarkedCategory("Finance")
        }
        view.findViewById<CardView>(R.id.education_card).setOnClickListener {
            navigateToBookmarkedCategory("Education")
        }
        view.findViewById<CardView>(R.id.health_card).setOnClickListener {
            navigateToBookmarkedCategory("Health")
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank()) {
                    searchBookmarkedBlogs(newText)
                    recyclerViewBookmarked.visibility = View.VISIBLE
                    view.findViewById<androidx.gridlayout.widget.GridLayout>(R.id.grid_layout).visibility = View.GONE
                    view.findViewById<TextView>(R.id.category).visibility = View.GONE
                } else {
                    recyclerViewBookmarked.visibility = View.GONE
                    view.findViewById<androidx.gridlayout.widget.GridLayout>(R.id.grid_layout).visibility = View.VISIBLE
                    view.findViewById<TextView>(R.id.category).visibility = View.VISIBLE
                    recyclerViewBookmarked.adapter = null
                }
                return true
            }
        })

        return view
    }

    private fun navigateToBookmarkedCategory(category: String) {
        val bundle = Bundle().apply {
            putString("category", category)
        }
        findNavController().navigate(R.id.BookmarkedCategory, bundle)
    }

    private fun searchBookmarkedBlogs(query: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("blogs")
            .get()
            .addOnSuccessListener { documents ->
                val bookmarkedBlogs = mutableListOf<Blog>()

                for (document in documents) {
                    val blogId = document.id
                    val blog = document.toObject(Blog::class.java)
                    if (blog.title.contains(query, ignoreCase = true)) {
                        db.collection("blogs").document(blogId)
                            .collection("bookmarks").document(currentUserId)
                            .get()
                            .addOnSuccessListener { bookmarkDoc ->
                                if (bookmarkDoc.exists()) {
                                    bookmarkedBlogs.add(blog)
                                    updateRecyclerView(bookmarkedBlogs)
                                }
                            }
                    }
                }

                if (bookmarkedBlogs.isEmpty() && !documents.isEmpty) {
                    updateRecyclerView(bookmarkedBlogs)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Bookmark", "Error getting documents: ", exception)
            }
    }

    private fun updateRecyclerView(blogs: List<Blog>) {
        val adapter = BlogAdapter(blogs) { blog ->
            val bundle = Bundle().apply {
                putString("blogId", blog.blogId)
                putString("title", blog.title)
                putString("content", blog.content)
                putString("image", blog.image)
                putString("username", blog.username)
                putString("uploadDate", blog.uploadDate?.time.toString())
            }
            findNavController().navigate(R.id.action_bookmark_to_blog, bundle)
        }
        recyclerViewBookmarked.adapter = adapter
    }
}