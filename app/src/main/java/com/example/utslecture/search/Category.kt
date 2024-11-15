package com.example.utslecture.search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utslecture.blog.BlogAdapter
import com.example.utslecture.R

class Category : Fragment() {

    private var category: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            category = it.getString("category")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)

        val textView = view.findViewById<TextView>(R.id.textView)
        textView.text = category ?: "No category found"

        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        Log.d("CategoryFragment", "Category: $category")

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newsRecyclerView = view.findViewById<RecyclerView>(R.id.newsRecyclerView)
        val adapter = BlogAdapter {
            findNavController().navigate(R.id.action_category_to_news)
        }

        newsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        newsRecyclerView.adapter = adapter
    }
}
