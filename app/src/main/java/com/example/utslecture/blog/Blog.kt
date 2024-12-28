package com.example.utslecture.blog

import com.example.utslecture.data.Blog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.utslecture.BuildConfig
import com.example.utslecture.R
import com.example.utslecture.data.Komentar
import com.example.utslecture.data.ProfileUser
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog

class Blog : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var commentAdapter: CommentAdapter
    private val commentsList: MutableList<Komentar> = mutableListOf()
    private var blogId: String = ""
    private var userId: String = ""
    private var username: String = ""
    private val firestore = Firebase.firestore
    private lateinit var likeButton: ImageView
    private lateinit var bookmarkButton: ImageView
    private lateinit var likesCountTextView: TextView
    private var isLiked: Boolean = false
    private lateinit var summaryTextView: TextView
    private lateinit var fabSummary: FloatingActionButton
    private var isSummaryVisible = false
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.API_KEY
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Firebase.firestore
        auth = Firebase.auth
        userId = auth.currentUser?.uid ?: ""
        if (userId.isNotEmpty()) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val profileUser = document.toObject(ProfileUser::class.java)
                        username = profileUser?.username ?: "Anonymous"
                    } else {
                        Log.d("BlogFragment", "No such document")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BlogFragment", "Error getting user document", e)
                }
        }
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

        val postCommentButton = view.findViewById<Button>(R.id.post_comment_button)
        postCommentButton.setOnClickListener {
            postComment()
        }
        likesCountTextView = view.findViewById(R.id.likesCountTextView)
        likeButton = view.findViewById(R.id.likeButton)
        bookmarkButton = view.findViewById(R.id.bookmarkButton)
        summaryTextView = view.findViewById(R.id.detail_news_summary)
        fabSummary = view.findViewById(R.id.fab_summary)

        fabSummary.setOnClickListener {
            showSummaryDialog()
        }

        likeButton.setOnClickListener {
            toggleLike()
        }

        bookmarkButton.setOnClickListener {
            toggleBookmark()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            blogId = it.getString("blogId") ?: ""
            val title = it.getString("title")
            val content = it.getString("content")
            val image = it.getString("image")
            val blogUsername = it.getString("username")
            val uploadDate = it.getString("uploadDate")?.toLongOrNull()?.let { timestamp ->
                Date(
                    timestamp
                )
            }
            view.findViewById<TextView>(R.id.detail_news_title).text = title
            view.findViewById<TextView>(R.id.detail_news_content).text = content

            val metadataTextView = view.findViewById<TextView>(R.id.detail_news_metadata)
            val relativeTime = uploadDate?.let {
                android.text.format.DateUtils.getRelativeTimeSpanString(
                    it.time,
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.MINUTE_IN_MILLIS
                ).toString()
            } ?: "Unknown time"

            metadataTextView.text = "$blogUsername • $relativeTime"

            Glide.with(this)
                .load(image)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(view.findViewById<ImageView>(R.id.detail_news_image))
            setupRecyclerView(view)
            content?.let { it1 -> summarizeContent(it1) }
        }
        loadComments()
        updateLikesCountUI()
        updateLikeButtonUI()
        updateBookmarkButtonUI()
        checkIfLiked()
        checkIfBookmarked()

    }

    private fun setupRecyclerView(view: View) {
        commentAdapter = CommentAdapter(commentsList)
        val commentsRecyclerView = view.findViewById<RecyclerView>(R.id.comments_recycler_view)
        commentsRecyclerView.layoutManager = LinearLayoutManager(context)
        commentsRecyclerView.adapter = commentAdapter
    }

    private fun postComment() {
        val commentInput = view?.findViewById<EditText>(R.id.comment_input)
        val commentText = commentInput?.text.toString().trim()

        if (commentText.isNotEmpty() && blogId.isNotEmpty() && userId.isNotEmpty() && username.isNotEmpty()) {
            val newComment = hashMapOf(
                "userId" to userId,
                "blogId" to blogId,
                "content" to commentText,
                "username" to username,
                "uploadDate" to FieldValue.serverTimestamp()
            )

            db.collection("comments")
                .add(newComment)
                .addOnSuccessListener { documentReference ->
                    Log.d("BlogFragment", "Comment added with ID: ${documentReference.id}")
                    commentInput?.text?.clear()
                    Toast.makeText(context, "Comment added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w("BlogFragment", "Error adding comment", e)
                    Toast.makeText(context, "Failed to add comment", Toast.LENGTH_SHORT).show()
                }
        } else {
            if (commentText.isBlank()) {
                Toast.makeText(context, "Comment is blank", Toast.LENGTH_SHORT).show()
            }

            if (blogId.isEmpty()) {
                Toast.makeText(context, "blogId is empty", Toast.LENGTH_SHORT).show()
            }

            if (username.isEmpty()) {
                Toast.makeText(context, "username is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadComments() {
        if (blogId.isNotEmpty()) {
            db.collection("comments")
                .whereEqualTo("blogId", blogId)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("BlogFragment", "listen:error", e)
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        commentsList.clear()
                        for (document in snapshots.documents) {
                            val commentData = document.data
                            Log.d("BlogFragment", "Comment Data: $commentData")

                            val uploadDateTimestamp = commentData?.get("uploadDate") as? Timestamp
                            val uploadDate = uploadDateTimestamp?.toDate() ?: Date()

                            val dateString = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(uploadDate)

                            val comment = Komentar(
                                userId = commentData?.get("userId") as String? ?: "",
                                blogId = commentData?.get("blogId") as String? ?: "",
                                content = commentData?.get("content") as String? ?: "",
                                username = commentData?.get("username") as String? ?: "",
                                uploadDate = uploadDate,
                                uploadDateString = dateString
                            )

                            commentsList.add(comment)
                        }
                        commentAdapter.notifyDataSetChanged()
                    }
                }
        } else {
            Log.w("BlogFragment", "Blog ID is empty, cannot load comments.")
        }
    }

    private fun toggleLike() {
        val currentUserId = auth.currentUser?.uid ?: return
        val blogRef = firestore.collection("blogs").document(blogId)
        val likeRef = blogRef.collection("likes").document(currentUserId)

        likeRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                blogRef.update("likes", FieldValue.increment(-1))
                likeRef.delete()
                    .addOnSuccessListener {
                        updateLikeStatus(false)
                    }
                    .addOnFailureListener { exception ->
                        showToast("Error unliking: ${exception.message}")
                        blogRef.update("likes", FieldValue.increment(1))
                    }

            } else {

                blogRef.update("likes", FieldValue.increment(1))
                likeRef.set(hashMapOf("userId" to currentUserId)).addOnSuccessListener {
                    updateLikeStatus(true)
                }
                    .addOnFailureListener { exception ->
                        showToast("Error liking: ${exception.message}")
                        blogRef.update("likes", FieldValue.increment(-1))
                    }
            }

            updateLikeButtonUI()
        }.addOnFailureListener { exception ->
            showToast("Error: ${exception.message}")
        }
    }

    private fun updateLikeStatus(liked: Boolean) {
        isLiked = liked
        updateLikeButtonUI()
        updateLikesCountUI()
    }

    private fun updateLikesCountUI() {
        val blogRef = firestore.collection("blogs").document(blogId)
        blogRef.addSnapshotListener { documentSnapshot, exception ->
            if (exception != null) {
                showToast("Error: ${exception.message}")
                return@addSnapshotListener
            }
            val blogData = documentSnapshot?.toObject(Blog::class.java)
            val likeCount = blogData?.likes ?: 0
            likesCountTextView.text = "$likeCount Likes"
        }
    }

    private fun updateLikeButtonUI() {
        if (isLiked) {
            likeButton.setImageResource(R.drawable.like_added)
        } else {
            likeButton.setImageResource(R.drawable.like_button)
        }
    }

    private fun updateBookmarkButtonUI() {
        val currentUserId = auth.currentUser?.uid ?: return
        val bookmarkRef =
            firestore.collection("blogs").document(blogId).collection("bookmarks")
                .document(currentUserId)
        bookmarkRef.get().addOnSuccessListener { document ->
            val isBookmarked = document.exists()

            if (isBookmarked) {
                bookmarkButton.setImageResource(R.drawable.bookmark_added)
            } else {
                bookmarkButton.setImageResource(R.drawable.bookmark_button)
            }
        }
    }

    private fun toggleBookmark() {
        val currentUserId = auth.currentUser?.uid ?: return
        val bookmarkRef =
            firestore.collection("blogs").document(blogId).collection("bookmarks")
                .document(currentUserId)
        bookmarkRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                bookmarkRef.delete()
            } else {
                bookmarkRef.set(hashMapOf("userId" to currentUserId))
            }
            updateBookmarkButtonUI()
        }.addOnFailureListener { exception ->
            showToast("Error: ${exception.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun checkIfLiked() {
        val currentUserId = auth.currentUser?.uid ?: return
        val likeRef = firestore.collection("blogs").document(blogId).collection("likes").document(currentUserId)

        likeRef.get().addOnSuccessListener { document ->
            isLiked = document.exists()
            updateLikeButtonUI()
        }.addOnFailureListener { exception ->
            showToast("Error: ${exception.message}")
        }
    }

    private fun checkIfBookmarked() {
        val currentUserId = auth.currentUser?.uid ?: return
        val bookmarkRef = firestore.collection("blogs").document(blogId).collection("bookmarks").document(currentUserId)
        bookmarkRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                bookmarkButton.setImageResource(R.drawable.bookmark_added)
            } else {
                bookmarkButton.setImageResource(R.drawable.bookmark_button)
            }
        }.addOnFailureListener { exception ->
            showToast("Error: ${exception.message}")
        }
    }

    private fun showSummaryDialog() {
        val summary = summaryTextView.text.toString()

        val dialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Summary")
            .setMessage(summary)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun summarizeContent(content: String) {
        lifecycleScope.launch {
            try {
                val prompt = "Summarize the following text in 1 sentences:\n\n$content"
                val response = generativeModel.generateContent(content { text(prompt) })
                val summary = response.text
                summaryTextView.text = summary ?: "Could not generate summary."
            } catch (e: Exception) {
                Log.e("GeminiAPI", "Exception during API call", e)
                summaryTextView.text = "Error generating summary."
            }
        }
    }
}