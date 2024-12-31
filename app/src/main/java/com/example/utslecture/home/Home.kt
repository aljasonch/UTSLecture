package com.example.utslecture.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels // Changed to activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utslecture.BuildConfig
import com.example.utslecture.R
import com.example.utslecture.auth.BaseAuth
import com.example.utslecture.blog.BlogAdapter
import com.example.utslecture.data.Blog
import com.example.utslecture.data.Notification
import com.example.utslecture.data.ProfileUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.DocumentChange
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import java.util.Date

class Home : BaseAuth() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""
    private var username: String = ""
    private lateinit var usernameTextView: TextView
    private lateinit var summaryTextViews: List<TextView>
    private lateinit var generateButton: Button
    private var blogListener: ListenerRegistration? = null
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.API_KEY
    )

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var progressBar1: ProgressBar
    private lateinit var progressBar2: ProgressBar
    private lateinit var progressBar3: ProgressBar

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

        summaryTextViews = listOf(
            view.findViewById(R.id.summaryTextView1),
            view.findViewById(R.id.summaryTextView2),
            view.findViewById(R.id.summaryTextView3)
        )

        progressBar1 = view.findViewById(R.id.progressBar1)
        progressBar2 = view.findViewById(R.id.progressBar2)
        progressBar3 = view.findViewById(R.id.progressBar3)

        viewModel.summaries.observe(viewLifecycleOwner, Observer { summaries ->
            if (!summaries.isNullOrEmpty()) {
                updateSummaries(summaries)
                hideLoadingIndicators()
            }
        })

        viewModel.summariesGenerated.observe(viewLifecycleOwner, Observer { generated ->
            if (!generated && viewModel.summaries.value.isNullOrEmpty()) {
                fetchAndSummarizeBlogs()
            }
        })

        generateButton.setOnClickListener {
            viewModel.resetSummaries()
            showLoadingIndicators()
            fetchAndSummarizeBlogs()
        }

        if (viewModel.summaries.value.isNullOrEmpty()) {
            showLoadingIndicators()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.notificationButton).setOnClickListener {
            findNavController().navigate(R.id.action_home_to_notification)
        }

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
                        Log.d("Home", "User document not found")
                        usernameTextView.text = "Hello, Anonymous!"
                    }
                    startListeningForBlogMilestones()
                }
                .addOnFailureListener { e ->
                    Log.e("Home", "Error fetching user document", e)
                }
        } else {
            usernameTextView.text = "Hello, Anonymous!"
        }

        val newsRecyclerView = view.findViewById<RecyclerView>(R.id.newsRecyclerView)
        db.collection("blogs")
            .get()
            .addOnSuccessListener { documents ->
                val blogs = documents.mapNotNull { it.toObject(Blog::class.java) }
                val recommendBlogs = blogs.shuffled().take(3)
                val navigateToBlogDetail: (Blog) -> Unit = { blog ->
                    incrementBlogViews(blog.blogId)

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
                newsRecyclerView.isNestedScrollingEnabled = false
                newsRecyclerView.adapter = BlogAdapter(recommendBlogs, navigateToBlogDetail)

                newsRecyclerView.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("Home", "Error fetching blogs: ", exception)
            }
    }

    private fun incrementBlogViews(blogId: String) {
        val blogRef = db.collection("blogs").document(blogId)
        blogRef.update("views", FieldValue.increment(1))
            .addOnSuccessListener { Log.d("Home", "View count incremented for blogId: $blogId") }
            .addOnFailureListener { e -> Log.e("Home", "Error incrementing view count for blogId: $blogId", e) }
    }

    private fun fetchAndSummarizeBlogs() {
        if (viewModel.summariesGenerated.value == true) return

        showLoadingIndicators()
        db.collection("blogs")
            .get()
            .addOnSuccessListener { documents ->
                val allBlogs = documents.mapNotNull { it.toObject(Blog::class.java) }
                val randomBlogs = allBlogs.shuffled().take(3)

                summarizeBlogs(randomBlogs)
            }
            .addOnFailureListener { exception ->
                Log.e("Home", "Error fetching blogs: ", exception)
                hideLoadingIndicators()
            }
    }

    private fun summarizeBlogs(blogs: List<Blog>) {
        val summaries = mutableListOf<String>()
        var processed = 0

        for ((index, blog) in blogs.withIndex()) {
            summarizeWithGemini(blog.content) { summary ->
                summaries.add(summary ?: "Unable to generate summary.")
                processed++
                if (processed == blogs.size) {
                    viewModel.setSummaries(summaries)
                }
            }
        }
    }

    private fun summarizeWithGemini(text: String, callback: (String?) -> Unit) {
        lifecycleScope.launch {
            try {
                val prompt = "Summarize the following text in one sentence:\n\n$text"
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
                summaryTextViews[i].visibility = View.VISIBLE
            }
        }
        hideLoadingIndicators()
    }

    private fun showLoadingIndicators() {
        progressBar1.visibility = View.VISIBLE
        progressBar2.visibility = View.VISIBLE
        progressBar3.visibility = View.VISIBLE

        summaryTextViews.forEach { it.visibility = View.GONE }
    }

    private fun hideLoadingIndicators() {
        progressBar1.visibility = View.GONE
        progressBar2.visibility = View.GONE
        progressBar3.visibility = View.GONE

        summaryTextViews.forEach { it.visibility = View.VISIBLE }
    }

    private fun startListeningForBlogMilestones() {
        if (blogListener != null) {
            return
        }

        blogListener = db.collection("blogs")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("Home", "Listen error", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    Log.d("Home", "Received snapshots: ${snapshots.documentChanges.size} changes")
                    for (doc in snapshots.documentChanges) {
                        if (doc.type == DocumentChange.Type.MODIFIED) {
                            val blogId = doc.document.id
                            Log.d("Home", "Blog with ID $blogId modified.")

                            db.collection("blogs").document(blogId).get()
                                .addOnSuccessListener { latestBlogSnapshot ->
                                    val latestBlog = latestBlogSnapshot.toObject(Blog::class.java)
                                    if (latestBlog != null) {
                                        Log.d(
                                            "Home",
                                            "Latest blog data (ID: $blogId): Title: ${latestBlog.title}, Likes: ${latestBlog.likes}, Like Milestone Notified: ${latestBlog.likeMilestoneNotified}"
                                        )
                                        if (latestBlog.likes >= 1 && !latestBlog.likeMilestoneNotified) {
                                            Log.d("Home", "Like milestone met for blog ID: $blogId. Sending notification...")
                                            val homeActivity = activity as? HomeActivity
                                            homeActivity?.showNotification(
                                                "Congratulations!",
                                                "Your blog '${latestBlog.title}' has received 1 like!"
                                            )

                                            addNotificationToFirebase(
                                                latestBlog.userId,
                                                "Blog Milestone",
                                                "Your blog '${latestBlog.title}' has received 1 like!"
                                            )
                                            Log.d("Home", "Updating likeMilestoneNotified to true for blog ID: $blogId...")
                                            doc.document.reference.update("likeMilestoneNotified", true)
                                                .addOnSuccessListener {
                                                    Log.d(
                                                        "Home",
                                                        "Successfully updated likeMilestoneNotified for blogId: $blogId"
                                                    )
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(
                                                        "Home",
                                                        "Failed to update likeMilestoneNotified for blogId: $blogId",
                                                        e
                                                    )
                                                }
                                        } else {
                                            Log.d("Home", "Like milestone not met or already notified for blog ID: $blogId.")
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Home", "Error fetching latest blog data for blog ID: $blogId", e)
                                }

                            val currentBlog = doc.document.toObject(Blog::class.java)
                            if (currentBlog.views >= 10 && !currentBlog.viewMilestoneNotified) {
                                val homeActivity = activity as? HomeActivity
                                homeActivity?.showNotification(
                                    "Congratulations!",
                                    "Your blog '${currentBlog.title}' has reached 10 views!"
                                )
                                addNotificationToFirebase(
                                    currentBlog.userId,
                                    "Blog Milestone",
                                    "Your blog '${currentBlog.title}' has reached 10 views!"
                                )
                                doc.document.reference.update("viewMilestoneNotified", true)
                                    .addOnSuccessListener {
                                        Log.d(
                                            "Home",
                                            "View milestone sent for blogId: ${currentBlog.blogId}"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Home", "Error updating viewMilestoneNotified", e)
                                    }
                            }
                        }
                    }
                }
            }
    }

    override fun onPause() {
        super.onPause()
        blogListener?.remove()
        blogListener = null
        Log.d("Home", "Listener removed in onPause()")
    }

    override fun onResume() {
        super.onResume()
        if (userId.isNotEmpty()) {
            startListeningForBlogMilestones()
            Log.d("Home", "Listener reactivated in onResume()")
        }
    }

    private fun addNotificationToFirebase(userId: String, title: String, message: String) {
        val notificationRef = db.collection("notifications").document()

        val newNotification = Notification(
            notificationId = notificationRef.id,
            userId = userId,
            title = title,
            message = message,
            timestamp = Date()
        )

        notificationRef.set(newNotification)
            .addOnSuccessListener {
                Log.d(
                    "Notifications",
                    "Notification added to Firebase with ID: ${notificationRef.id}"
                )
            }
            .addOnFailureListener { e ->
                Log.e("Notifications", "Failed to add notification", e)
            }
    }
}