package com.example.utslecture.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utslecture.BuildConfig
import com.example.utslecture.R
import com.example.utslecture.auth.BaseAuth
import com.example.utslecture.blog.BlogAdapter
import com.example.utslecture.data.Blog
import com.example.utslecture.data.ProfileUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class Home : BaseAuth() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""
    private var username: String = ""
    private lateinit var usernameTextView: TextView
    private lateinit var summaryTextViews: List<TextView>
    private lateinit var generateButton: Button
    val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.API_KEY
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as HomeActivity).showBottomNavigation()
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        usernameTextView = view.findViewById(R.id.username_profile)
        generateButton = view.findViewById(R.id.generateButton)

        // Initialize summary TextViews
        summaryTextViews = listOf(
            view.findViewById(R.id.summaryTextView1),
            view.findViewById(R.id.summaryTextView2),
            view.findViewById(R.id.summaryTextView3)
        )

        // Update usernameTextView with fetched username
        usernameTextView.text = "Hello, $username!"

        generateButton.setOnClickListener {
            fetchAndSummarizeBlogs()
        }

        // Fetch and summarize blogs when the fragment is created
        fetchAndSummarizeBlogs()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (userId.isNotEmpty()) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val profileUser = document.toObject(ProfileUser::class.java)
                        username = profileUser?.username ?: "Anonymous"
                        usernameTextView.text = "Hello, $username!"
                    } else {
                        Log.d("Home", "No such document")
                        usernameTextView.text = "Hello, Anonymous!"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Home", "Error getting user document", e)
                }
        }else{
            usernameTextView.text = "Hello, Anonymous!"
        }
        val newsRecyclerView = view.findViewById<RecyclerView>(R.id.newsRecyclerView)
        db.collection("blogs")
            .get()
            .addOnSuccessListener { documents ->
                val blogs = documents.mapNotNull { document ->
                    document.toObject(Blog::class.java)
                }

                val adapter = BlogAdapter(blogs) { blog ->
                    val bundle = bundleOf(
                        "blogId" to blog.blogId,
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

    private fun fetchAndSummarizeBlogs() {
        db.collection("blogs")
            .get()
            .addOnSuccessListener { documents ->
                val allBlogs = documents.mapNotNull { it.toObject(Blog::class.java) }
                val randomBlogs = allBlogs.shuffled().take(3)

                summarizeBlogs(randomBlogs)
            }
            .addOnFailureListener { exception ->
                Log.e("Home", "Error getting blogs: ", exception)
            }
    }

    private fun summarizeBlogs(blogs: List<Blog>) {
        val summaries = mutableListOf<String>()
        var processed = 0

        for ((index, blog) in blogs.withIndex()) {
            summarizeWithGemini(blog.content) { summary ->
                summaries.add(summary ?: "Could not generate summary.")
                processed++
                if (processed == blogs.size) {
                    activity?.runOnUiThread {
                        updateSummaries(summaries)
                    }
                }
            }
        }
    }

    private fun summarizeWithGemini(text: String, callback: (String?) -> Unit) {
        lifecycleScope.launch {
            try {
                val prompt = "Summarize the following text in 1 sentences:\n\n$text"
                val response = generativeModel.generateContent(content { text(prompt) })
                val summary = response.text
                callback(summary)
            } catch (e: Exception) {
                Log.e("GeminiAPI", "Exception during API call", e)
                callback(null)
            }
        }
    }

    private fun updateSummaries(summaries: List<String>) {
        for (i in summaries.indices) {
            if (i < summaryTextViews.size) {
                summaryTextViews[i].text = summaries[i]
            }
        }
    }
}