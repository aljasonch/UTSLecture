package com.example.utslecture.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utslecture.R
import com.example.utslecture.blog.BlogAdapter
import com.example.utslecture.data.Blog
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.TextView
import androidx.gridlayout.widget.GridLayout

class Search : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerViewPopular: RecyclerView
    private lateinit var recyclerViewRecent: RecyclerView
    private lateinit var recyclerViewTrending: RecyclerView
    private lateinit var categoryTitle: TextView
    private lateinit var gridLayout: GridLayout
    private lateinit var popularTitle: TextView
    private lateinit var recentTitle: TextView
    private lateinit var trendingTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.queryHint = "Search"

        categoryTitle = view.findViewById(R.id.category_title)
        gridLayout = view.findViewById(R.id.grid_layout)
        popularTitle = view.findViewById(R.id.popular_title)
        recentTitle = view.findViewById(R.id.new_title)
        trendingTitle = view.findViewById(R.id.trending_title)
        recyclerViewPopular = view.findViewById(R.id.recyclerViewPopular)
        recyclerViewRecent = view.findViewById(R.id.recyclerViewRecent)
        recyclerViewTrending = view.findViewById(R.id.recyclerViewTrending)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val separatorViews = findViewsWithTag(view, "separator")
                if (!newText.isNullOrBlank()) {
                    searchBlogs(newText)
                    categoryTitle.visibility = View.GONE
                    gridLayout.visibility = View.GONE
                    popularTitle.visibility = View.GONE
                    recentTitle.visibility = View.GONE
                    trendingTitle.visibility = View.GONE
                    for (separator in separatorViews) {
                        separator.visibility = View.GONE
                    }
                } else {
                    displayInitialData()
                    categoryTitle.visibility = View.VISIBLE
                    gridLayout.visibility = View.VISIBLE
                    popularTitle.visibility = View.VISIBLE
                    recentTitle.visibility = View.VISIBLE
                    trendingTitle.visibility = View.VISIBLE
                    for (separator in separatorViews) {
                        separator.visibility = View.VISIBLE
                    }
                }
                return true
            }
        })

        view.findViewById<CardView>(R.id.politics_card).setOnClickListener {
            navigateToCategory("International Politics")
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

        recyclerViewPopular.isNestedScrollingEnabled = false
        recyclerViewRecent.isNestedScrollingEnabled = false
        recyclerViewTrending.isNestedScrollingEnabled = false

        displayInitialData()
    }

    private fun searchBlogs(query: String) {
        val endQuery = query + "\uf8ff"

        db.collection("blogs")
            .whereGreaterThanOrEqualTo("title", query)
            .whereLessThanOrEqualTo("title", endQuery)
            .get()
            .addOnSuccessListener { documents ->
                val allBlogs = documents.mapNotNull { it.toObject(Blog::class.java) }
                val filteredBlogs = allBlogs.filter { it.title.contains(query, ignoreCase = true) }

                Log.d("SearchFragment", "Search Results: ${filteredBlogs.size}")

                val navigateToBlogDetail: (Blog) -> Unit = { blog ->
                    val bundle = bundleOf(
                        "blogId" to blog.blogId,
                        "title" to blog.title,
                        "content" to blog.content,
                        "image" to blog.image,
                        "username" to blog.username,
                        "uploadDate" to blog.uploadDate?.time.toString()
                    )
                    findNavController().navigate(R.id.action_search_to_blog, bundle)
                }

                recyclerViewPopular.adapter = BlogAdapter(filteredBlogs, navigateToBlogDetail)
                recyclerViewRecent.adapter = BlogAdapter(filteredBlogs, navigateToBlogDetail)
                recyclerViewTrending.adapter = BlogAdapter(filteredBlogs, navigateToBlogDetail)

                recyclerViewPopular.layoutManager = LinearLayoutManager(requireContext())
                recyclerViewRecent.layoutManager = LinearLayoutManager(requireContext())
                recyclerViewTrending.layoutManager = LinearLayoutManager(requireContext())

                recyclerViewPopular.adapter?.notifyDataSetChanged()
                recyclerViewRecent.adapter?.notifyDataSetChanged()
                recyclerViewTrending.adapter?.notifyDataSetChanged()

            }
            .addOnFailureListener { exception ->
                Log.w("SearchFragment", "Error getting documents: ", exception)
            }
    }

    private fun displayInitialData() {
        db.collection("blogs")
            .get()
            .addOnSuccessListener { documents ->
                val allBlogs = documents.mapNotNull { it.toObject(Blog::class.java) }

                Log.d("SearchFragment", "All Blogs Fetched: ${allBlogs.size}")

                val popularBlogs = allBlogs.sortedByDescending { it.likes }.take(5)
                val recentBlogs = allBlogs.filter { it.uploadDate != null }
                    .sortedByDescending { it.uploadDate }
                    .take(5)
                val trendingBlogs = allBlogs.sortedByDescending { it.views }.take(5)

                Log.d("SearchFragment", "Popular Blogs: ${popularBlogs.size}")
                Log.d("SearchFragment", "Recent Blogs: ${recentBlogs.size}")
                Log.d("SearchFragment", "Trending Blogs: ${trendingBlogs.size}")

                val navigateToBlogDetail: (Blog) -> Unit = { blog ->
                    val bundle = bundleOf(
                        "blogId" to blog.blogId,
                        "title" to blog.title,
                        "content" to blog.content,
                        "image" to blog.image,
                        "username" to blog.username,
                        "uploadDate" to blog.uploadDate?.time.toString()
                    )
                    findNavController().navigate(R.id.action_search_to_blog, bundle)
                }

                recyclerViewPopular.adapter = BlogAdapter(popularBlogs, navigateToBlogDetail)
                recyclerViewRecent.adapter = BlogAdapter(recentBlogs, navigateToBlogDetail)
                recyclerViewTrending.adapter = BlogAdapter(trendingBlogs, navigateToBlogDetail)

                recyclerViewPopular.layoutManager = LinearLayoutManager(requireContext())
                recyclerViewRecent.layoutManager = LinearLayoutManager(requireContext())
                recyclerViewTrending.layoutManager = LinearLayoutManager(requireContext())

                recyclerViewPopular.adapter?.notifyDataSetChanged()
                recyclerViewRecent.adapter?.notifyDataSetChanged()
                recyclerViewTrending.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("SearchFragment", "Error getting documents: ", exception)
            }
    }

    private fun findViewsWithTag(root: View, tag: String): List<View> {
        val views = mutableListOf<View>()
        val rootView = root as? ViewGroup ?: return views

        for (i in 0 until rootView.childCount) {
            val child = rootView.getChildAt(i)
            if (child is ViewGroup) {
                views.addAll(findViewsWithTag(child, tag))
            }

            val tagObj = child.tag
            if (tagObj != null && tagObj == tag) {
                views.add(child)
            }
        }

        return views
    }
}