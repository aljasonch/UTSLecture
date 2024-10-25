package com.example.utslecture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Search : Fragment() {

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
            putString("category", category) // Pass the category string as an argument
        }
        findNavController().navigate(R.id.Category, bundle) // Use the ID of the destination fragment
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerViewPopular = view.findViewById<RecyclerView>(R.id.recyclerViewPopular)
        recyclerViewPopular.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewPopular.adapter = NewsAdapter {
            findNavController().navigate(R.id.action_search_to_news) // Navigasi dari "Popular"
        }

        // Inisialisasi RecyclerView untuk "Recent"
        val recyclerViewRecent = view.findViewById<RecyclerView>(R.id.recyclerViewRecent)
        recyclerViewRecent.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewRecent.adapter = NewsAdapter {
            findNavController().navigate(R.id.action_search_to_news) // Navigasi dari "Recent"
        }

        // Inisialisasi RecyclerView untuk "Trending"
        val recyclerViewTrending = view.findViewById<RecyclerView>(R.id.recyclerViewTrending)
        recyclerViewTrending.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewTrending.adapter = NewsAdapter {
            findNavController().navigate(R.id.action_search_to_news) // Navigasi dari "Trending"
        }
    }
}
