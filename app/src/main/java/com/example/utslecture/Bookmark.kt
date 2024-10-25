package com.example.utslecture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
class Bookmark : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bookmark, container, false)

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.queryHint = "Search"


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

}
