package com.example.utslecture.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utslecture.blog.BlogAdapter
import com.example.utslecture.R
import com.example.utslecture.auth.BaseAuth

class Home : BaseAuth() {

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
        val adapter = BlogAdapter {
            findNavController().navigate(R.id.action_home_to_news)
        }

        newsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        newsRecyclerView.adapter = adapter
    }

}