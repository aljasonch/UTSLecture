package com.example.utslecture.profile

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utslecture.R
import com.example.utslecture.data.Blog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class Profile : Fragment() {
    private lateinit var editProfileButton: Button
    private lateinit var settingButton: ImageView
    private lateinit var createBlogButton: Button
    private lateinit var rvBlogs: RecyclerView
    private lateinit var blogAdapter: ProfileBlogAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        editProfileButton = view.findViewById(R.id.edit_profile_button)
        settingButton = view.findViewById(R.id.SettingButton)
        createBlogButton = view.findViewById(R.id.CreateBlogButton)
        rvBlogs = view.findViewById(R.id.rv_blogs)

        setupRecyclerView()
        loadUserBlogs()

        editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }

        settingButton.setOnClickListener {
            findNavController().navigate(R.id.Settings)
        }

        createBlogButton.setOnClickListener {
            findNavController().navigate(R.id.CreateBlog)
        }
    }

    private fun setupRecyclerView() {
        blogAdapter = ProfileBlogAdapter(listOf(),
            onUpdateClickListener = { blog ->
                navigateToUpdateBlog(blog)
            },
            onDeleteClickListener = { blog ->
                deleteBlog(blog)
            }
        )
        rvBlogs.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = blogAdapter
        }
    }

    private fun loadUserBlogs() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("blogs")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val blogList = documents.toObjects(Blog::class.java)
                    blogAdapter.setData(blogList)
                }
                .addOnFailureListener { exception ->
                    showToast("Error saat mengambil data blog: ${exception.message}")
                }
        }
    }

    private fun navigateToUpdateBlog(blog: Blog) {
        val blogQuery = firestore.collection("blogs").whereEqualTo("title", blog.title).get()
        blogQuery.addOnSuccessListener { querySnapshot ->
            if (querySnapshot.documents.isNotEmpty()) {
                val blogId = querySnapshot.documents[0].id
                val bundle = bundleOf(
                    "blogId" to blogId,
                    "title" to blog.title,
                    "content" to blog.content,
                    "image" to blog.image
                )
                findNavController().navigate(R.id.action_ProfileFragment_to_updateBlogFragment, bundle)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun deleteBlog(blog: Blog) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Blog")
            .setMessage("Apakah Anda yakin ingin menghapus blog ini?")
            .setPositiveButton("Hapus") { _, _ ->
                val blogQuery = firestore.collection("blogs").whereEqualTo("title", blog.title) // Query menggunakan field unik
                blogQuery.get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.documents.isNotEmpty()) {
                            val blogId = querySnapshot.documents[0].id
                            firestore.collection("blogs").document(blogId)
                                .delete()
                                .addOnSuccessListener {
                                    if (!blog.image.isNullOrEmpty()) {
                                        deleteImageFromStorage(blog.image)
                                    }
                                    showToast("Blog berhasil dihapus")
                                    loadUserBlogs()
                                }
                                .addOnFailureListener { exception ->
                                    showToast("Error saat menghapus blog: ${exception.message}")
                                }
                        } else {
                            showToast("Blog tidak ditemukan")
                        }
                    }
                    .addOnFailureListener { exception ->
                        showToast("Error saat mencari blog: ${exception.message}")
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteImageFromStorage(imageUrl: String) {
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        storageReference.delete()
            .addOnSuccessListener {
                // Hapus gambar berhasil
            }
            .addOnFailureListener { exception ->
                showToast("Error saat menghapus gambar: ${exception.message}")
            }
    }
}