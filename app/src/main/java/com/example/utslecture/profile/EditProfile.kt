package com.example.utslecture.profile

import com.example.utslecture.data.ProfileUser
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.utslecture.R
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfile : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var nameInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var bioInput: EditText
    private lateinit var saveButton: MaterialTextView // Gunakan MaterialTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameInput = view.findViewById(R.id.name_input_field)
        usernameInput = view.findViewById(R.id.username_input_field)
        emailInput = view.findViewById(R.id.email_input_field)
        phoneInput = view.findViewById(R.id.phone_input_field)
        bioInput = view.findViewById(R.id.bio_input_field)
        saveButton = view.findViewById(R.id.save_button)

        loadUserData()

        saveButton.setOnClickListener {
            updateUserProfile()
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
            Toast.makeText(requireContext(), "com.example.utslecture.data.User is not logged in", Toast.LENGTH_SHORT).show()
            return // Hentikan eksekusi jika userId null
        }

        val updatedProfile = ProfileUser(
            name = nameInput.text.toString().trim(),
            username = usernameInput.text.toString().trim(),
            email = emailInput.text.toString().trim(),
            phoneNumber = phoneInput.text.toString().trim(),
            bio = bioInput.text.toString().trim(),
            userId = userId // Sertakan userId di sini
        )

        firestore.collection("users").document(userId).set(updatedProfile)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.Profile) // Navigasi setelah sukses
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
