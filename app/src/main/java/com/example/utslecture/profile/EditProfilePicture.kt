package com.example.utslecture.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.utslecture.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID

class EditProfilePicture : Fragment() {
    private lateinit var previewImage: ImageView
    private lateinit var saveButton: TextView
    private var imageBitmap: Bitmap? = null

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile_picture, container, false)
        previewImage = view.findViewById(R.id.previewImage)
        saveButton = view.findViewById(R.id.saveButtonProfilePicture)
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mengaktifkan kamera saat fragment dibuat
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultLauncher.launch(takePictureIntent)

        saveButton.setOnClickListener {
            uploadImageAndSaveProfile()
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            imageBitmap = data?.extras?.get("data") as Bitmap
            previewImage.setImageBitmap(imageBitmap)
        }
    }

    private fun uploadImageAndSaveProfile() {
        if (imageBitmap == null) {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User is not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Upload image to Firebase Storage
        val storageRef = storage.reference
        val imageFileName = "profile_pictures/${userId}/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(imageFileName)

        val baos = ByteArrayOutputStream()
        imageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        val uploadTask = imageRef.putBytes(imageData)
        uploadTask.addOnSuccessListener {
            // Get the download URL
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()

                // Update profile in Firestore with the image URL
                updateUserProfile(userId, imageUrl)
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserProfile(userId: String, imageUrl: String) {
        val userRef = firestore.collection("users").document(userId)

        userRef.update("profilePicture", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.Profile) // Navigate back to profile
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update profile picture: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}