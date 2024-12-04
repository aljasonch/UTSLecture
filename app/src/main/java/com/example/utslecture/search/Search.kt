package com.example.utslecture.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utslecture.R
import com.example.utslecture.blog.BlogAdapter
import com.example.utslecture.data.Blog
import com.google.firebase.firestore.FirebaseFirestore

class Search : Fragment() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.queryHint = "Search"

        // Set click listeners for the CardViews
        view.findViewById<CardView>(R.id.politics_card).setOnClickListener {
            navigateToCategory("Politics")
        }

        view.findViewById<CardView>(R.id.finance_card).setOnClickListener {
            navigateToCategory("Finance")
        }

        view.findViewById<CardView>(R.id.education_card).setOnClickListener {
            navigateToCategory("Education")
        }

        view.findViewById<CardView>(R.id.health_card).setOnClickListener {
            navigateToCategory("Health")
        }

        return view
    }

    private fun navigateToCategory(category: String) {
        val bundle = Bundle().apply {
            putString("category", category)
        }
        findNavController().navigate(R.id.Category, bundle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerViewPopular = view.findViewById<RecyclerView>(R.id.recyclerViewPopular)
        val recyclerViewRecent = view.findViewById<RecyclerView>(R.id.recyclerViewRecent)
        val recyclerViewTrending = view.findViewById<RecyclerView>(R.id.recyclerViewTrending)

        db.collection("blogs")
            .get()
            .addOnSuccessListener { documents ->
                val allBlogs = documents.mapNotNull { it.toObject(Blog::class.java) }

                val popularBlogs = allBlogs.sortedByDescending { it.likes }.take(5) // Misalnya, popular berdasarkan likes
                val recentBlogs = allBlogs.sortedByDescending { it.uploadDate }.take(5)
                val trendingBlogs = allBlogs.sortedByDescending { it.views }.take(5) // Misalnya, trending berdasarkan views

                val navigateToBlogDetail: (Blog) -> Unit = { blog ->
                    val bundle = Bundle().apply {
                        putString("title", blog.title)
                        putString("content", blog.content)
                        putString("image", blog.image)
                        putString("username", blog.username)
                        putString("uploadDate", blog.uploadDate?.time.toString())
                    }
                    findNavController().navigate(R.id.action_search_to_blog, bundle)
                }

                recyclerViewPopular.adapter = BlogAdapter(popularBlogs, navigateToBlogDetail)
                recyclerViewRecent.adapter = BlogAdapter(recentBlogs, navigateToBlogDetail)
                recyclerViewTrending.adapter = BlogAdapter(trendingBlogs, navigateToBlogDetail)

                recyclerViewPopular.layoutManager = LinearLayoutManager(requireContext())
                recyclerViewRecent.layoutManager = LinearLayoutManager(requireContext())
                recyclerViewTrending.layoutManager = LinearLayoutManager(requireContext())

            }
            .addOnFailureListener { exception ->
                Log.w("SearchFragment", "Error getting documents: ", exception)
                // Handle error, misalnya tampilkan pesan ke pengguna
            }
    }
}