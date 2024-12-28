package com.example.utslecture.blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.utslecture.R
import com.example.utslecture.data.Blog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UpdateBlog : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null
    private lateinit var blogId: String
    private lateinit var storageRef: StorageReference
    private var imageUri: String = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var updateButton: TextView
    private lateinit var titleInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var descriptionInput: EditText
    private lateinit var progressOverlay: FrameLayout
    private lateinit var progressBar: ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_update_blog, container, false)
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference
        auth = FirebaseAuth.getInstance()

        updateButton = view.findViewById<TextView>(R.id.update_button)
        titleInput = view.findViewById<EditText>(R.id.update_title_input)
        categorySpinner = view.findViewById<Spinner>(R.id.update_category_spinner)
        descriptionInput = view.findViewById<EditText>(R.id.update_description_input)
        progressOverlay = view.findViewById(R.id.progress_overlay)
        progressBar = view.findViewById(R.id.progress_bar)
        val imageView = view.findViewById<ImageView>(R.id.update_image_view)
        val imagePlaceholder = view.findViewById<LinearLayout>(R.id.update_image_placeholder)

        blogId = arguments?.getString("blogId") ?: ""

        firestore.collection("blogs").document(blogId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val blog = document.toObject(Blog::class.java)

                    titleInput.setText(blog?.title)
                    descriptionInput.setText(blog?.content)
                    imageUri = blog?.image ?: ""
                    Glide.with(this).load(imageUri).into(imageView)

                    if (imageUri.isNotEmpty()) {
                        imageView.visibility = View.VISIBLE
                        imagePlaceholder.visibility = View.GONE
                    }

                    val currentUserId = auth.currentUser?.uid
                    if (blog?.userId == currentUserId) {
                        updateButton.setOnClickListener {
                            updateBlog(blog)
                        }

                        imagePlaceholder.setOnClickListener {
                            openGalleryForImage()
                        }

                        imageView.setOnClickListener {
                            openGalleryForImage()
                        }

                    } else {
                        showToast("Anda tidak diizinkan untuk mengupdate blog ini.")
                        updateButton.isEnabled = false

                    }

                } else {
                    showToast("Blog tidak ditemukan.")
                    findNavController().popBackStack()
                }
            }
            .addOnFailureListener { exception ->
                showToast("Gagal mengambil data blog: ${exception.message}")
                findNavController().popBackStack()

            }

    }

    private fun updateBlog(blog: Blog?) {

        val updatedTitle = titleInput.text.toString()
        val updatedCategory = categorySpinner.selectedItem.toString()
        val updatedDescription = descriptionInput.text.toString()

        val updates = hashMapOf<String, Any>(
            "title" to updatedTitle,
            "category" to updatedCategory,
            "content" to updatedDescription,
            "image" to imageUri
        )

        firestore.collection("blogs").document(blogId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Blog berhasil diupdate!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_updateBlogFragment_to_ProfileFragment)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Gagal mengupdate blog: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            selectedImageUri = data?.data
            uploadImageToStorage()

            val imageView = view?.findViewById<ImageView>(R.id.update_image_view)
            val imagePlaceholder = view?.findViewById<LinearLayout>(R.id.update_image_placeholder)
            imageView?.setImageURI(selectedImageUri)
            imageView?.visibility = View.VISIBLE
            imagePlaceholder?.visibility = View.GONE

        }
    }

    private fun uploadImageToStorage() {

        if (selectedImageUri != null) {

            val imageRef = storageRef.child("blog_images/${blogId}_${System.currentTimeMillis()}")
            imageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener { taskSnapshot ->

                    imageRef.downloadUrl
                        .addOnSuccessListener { uri ->
                            imageUri = uri.toString()

                        }
                }.addOnFailureListener { exception ->

                    Toast.makeText(requireContext(), exception.message, Toast.LENGTH_LONG).show()

                }
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_CODE = 100
    }
    private fun showLoading(show: Boolean) {
        if (show) {
            progressOverlay.visibility = View.VISIBLE
            updateButton.isEnabled = false
        } else {
            progressOverlay.visibility = View.GONE
            updateButton.isEnabled = true
        }
    }

}