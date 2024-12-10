package com.example.utslecture.blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.utslecture.R
import com.example.utslecture.data.Blog
import com.example.utslecture.data.ProfileUser
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class CreateBlog : Fragment() {
    private lateinit var saveButton: MaterialTextView
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var imageContainer: FrameLayout
    private lateinit var imageView: ImageView
    private lateinit var categorySpinner: Spinner
    private var imageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val TAG = "CreateBlog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveButton = view.findViewById(R.id.save_button)
        titleInput = view.findViewById(R.id.title_input)
        descriptionInput = view.findViewById(R.id.description_input)
        imageContainer = view.findViewById(R.id.image_container)
        imageView = view.findViewById(R.id.image_view)
        categorySpinner = view.findViewById(R.id.category_spinner)

        imageContainer.setOnClickListener {
            openFileChooser()
        }

        saveButton.setOnClickListener {
            uploadImageAndSaveBlog()
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri)
            imageView.visibility = View.VISIBLE
        }
    }

    private fun uploadImageAndSaveBlog() {
        if (imageUri == null) {
            saveBlog("")
            return
        }

        val storageRef = storage.reference
        val imageFileName = "blog/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(imageFileName)

        imageRef.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    saveBlog(imageUrl)
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting download URL", exception)
                    Toast.makeText(requireContext(), "Gagal mendapatkan URL gambar", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error uploading image", exception)
                Toast.makeText(requireContext(), "Gagal mengunggah gambar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveBlog(imageUrl: String) {
        val title = titleInput.text.toString().trim()
        val content = descriptionInput.text.toString().trim()
        val userId = auth.currentUser?.uid ?: "unknownUserId"
        val selectedCategory = categorySpinner.selectedItem.toString()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(requireContext(), "Judul dan konten tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val blogId = UUID.randomUUID().toString()

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val profileUser = document.toObject(ProfileUser::class.java)
                    val username = profileUser?.username ?: ""

                    val blog = Blog(
                        blogId = blogId,
                        userId = userId,
                        title = title,
                        image = imageUrl,
                        content = content,
                        views = 0,
                        likes = 0,
                        username = username,
                        uploadDate = Date(),
                        category = selectedCategory
                    )

                    db.collection("blogs")
                        .document(blogId)
                        .set(blog)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Blog berhasil disimpan", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Gagal menyimpan blog", exception)
                            Toast.makeText(requireContext(), "Gagal menyimpan blog", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e(TAG, "User tidak ditemukan")
                    Toast.makeText(requireContext(), "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Gagal mendapatkan data user", exception)
                Toast.makeText(requireContext(), "Gagal mengambil data user", Toast.LENGTH_SHORT).show()
            }
    }


}