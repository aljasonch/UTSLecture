package com.example.utslecture.blog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.utslecture.R
import java.util.Date

class Blog : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val title = arguments?.getString("title")
            val content = arguments?.getString("content")
            val image = arguments?.getString("image")
            val username = arguments?.getString("username")
            val uploadDate = arguments?.getString("uploadDate")?.toLongOrNull()?.let { Date(it) }

            view.findViewById<TextView>(R.id.detail_news_title).text = title
            view.findViewById<TextView>(R.id.detail_news_content).text = content
            view.findViewById<TextView>(R.id.detail_news_metadata).text = "$username • ${uploadDate?.toString() ?: ""}"

            Glide.with(this)
                .load(image)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(view.findViewById<ImageView>(R.id.detail_news_image))
        }
    }
}