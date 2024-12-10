package com.example.utslecture.bookmark

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

class BookmarkedCategory : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth
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
        textView.text = "${category}"

        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newsRecyclerView = view.findViewById<RecyclerView>(R.id.newsRecyclerView)
        newsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val currentUserId = auth.currentUser?.uid

        if (category != null && currentUserId != null) {
            db.collection("blogs")
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener { documents ->
                    val bookmarkedBlogsInCategory = mutableListOf<Blog>()
                    for (document in documents) {
                        val blog = document.toObject(Blog::class.java)
                        db.collection("blogs").document(blog.blogId)
                            .collection("bookmarks").document(currentUserId)
                            .get()
                            .addOnSuccessListener { bookmarkDoc ->
                                if (bookmarkDoc.exists()) {
                                    bookmarkedBlogsInCategory.add(blog)
                                    if(bookmarkedBlogsInCategory.size == documents.size()){
                                        val adapter = BlogAdapter(bookmarkedBlogsInCategory) { blog ->
                                            val bundle = Bundle().apply {
                                                putString("blogId", blog.blogId)
                                                putString("title", blog.title)
                                                putString("content", blog.content)
                                                putString("image", blog.image)
                                                putString("username", blog.username)
                                                putString("uploadDate", blog.uploadDate?.time.toString())
                                            }
                                            findNavController().navigate(R.id.action_bookmarkedCategory_to_blog, bundle)
                                        }
                                        newsRecyclerView.adapter = adapter
                                    }
                                }
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("BookmarkedCategory", "Error getting documents: ", exception)
                }
        } else {
            Log.e("BookmarkedCategory", "Category or userId is null")
        }
    }
}