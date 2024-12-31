package com.example.utslecture.profile

import com.example.utslecture.data.ProfileUser
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.utslecture.R
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import com.google.firebase.firestore.SetOptions

class EditProfile : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var nameInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var bioInput: EditText
    private lateinit var saveButton: MaterialTextView
    private lateinit var editProfileButton: TextView
    private lateinit var profileImageView: ImageView

    private var profilePicture: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameInput = view.findViewById(R.id.name_input_field)
        usernameInput = view.findViewById(R.id.username_input_field)
        emailInput = view.findViewById(R.id.email_input_field)
        phoneInput = view.findViewById(R.id.phone_input_field)
        bioInput = view.findViewById(R.id.bio_input_field)
        saveButton = view.findViewById(R.id.save_button)
        editProfileButton = view.findViewById(R.id.profile_picture)
        profileImageView = view.findViewById(R.id.profilePicture)

        loadUserData()

        saveButton.setOnClickListener {
            updateUserProfile()
        }

        editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_editProfilePictureFragment)
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userProfile = document.toObject(ProfileUser::class.java)
                        userProfile?.let {
                            nameInput.setText(it.name)
                            usernameInput.setText(it.username)
                            emailInput.setText(it.email)
                            phoneInput.setText(it.phoneNumber)
                            bioInput.setText(it.bio)

                            profilePicture = it.profilePicture

                            if (!it.profilePicture.isNullOrEmpty()) {
                                Glide.with(this)
                                    .load(it.profilePicture)
                                    .placeholder(R.drawable.profile_photo)
                                    .error(R.drawable.profile_photo)
                                    .circleCrop()
                                    .into(profileImageView)
                            }
                        }
                    } else {
                        Log.d("EditProfile", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("EditProfile", "Get failed with ", exception)
                }
        }
    }

    private fun updateUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User is not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedProfile = ProfileUser(
            name = nameInput.text.toString().trim(),
            username = usernameInput.text.toString().trim(),
            email = emailInput.text.toString().trim(),
            phoneNumber = phoneInput.text.toString().trim(),
            bio = bioInput.text.toString().trim(),
            userId = userId,
            profilePicture = profilePicture ?: ""
        )

        firestore.collection("users").document(userId).set(updatedProfile, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_editProfileFragment_to_ProfileFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    fun updateProfilePicture(url: String) {
        profilePicture = url
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.profile_photo)
            .error(R.drawable.profile_photo)
            .circleCrop()
            .into(profileImageView)
    }
}