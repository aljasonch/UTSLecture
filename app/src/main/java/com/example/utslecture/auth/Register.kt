package com.example.utslecture.auth

import com.example.utslecture.data.ProfileUser
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.utslecture.home.HomeActivity
import com.example.utslecture.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Register : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as HomeActivity).hideBottomNavigation()
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val usernameInput = view.findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.username_input_field)
        val emailInput = view.findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.email_input_field)
        val passwordInput = view.findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.password_input_field)
        val repeatPasswordInput = view.findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.repeat_password_input_field)
        val loginText = view.findViewById<TextView>(R.id.login_text)
        val loginButton = view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.submit_button)
        loginText.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val repeatPassword = repeatPasswordInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            if (email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty() || username.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please fill in all required fields",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (password != repeatPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Log.d("email", email)
                Log.d("password", password)
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            Log.d("Register", "RegisterWithEmail:success")
                            val userId = auth.currentUser?.uid
                            createUserProfile(userId, email, username)
                            findNavController().navigate(R.id.loginFragment)
                        } else {
                            Log.w("Register", "RegisterWithEmail:failure", task.exception)
                            Toast.makeText(
                                requireContext(),
                                "Register Failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    private fun createUserProfile(userId: String?, email: String, username: String) {
        if (userId != null) {
            val userProfile = ProfileUser(
                userId = userId,
                email = email,
                name = "",
                username = username,
                phoneNumber = "",
                bio = ""
            )
            firestore.collection("users")
                .document(userId)
                .set(userProfile)
                .addOnSuccessListener {
                    Log.d(
                        "Firestore",
                        "com.example.utslecture.data.User profile created successfully"
                    )
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error adding document", e)
                }
        }
    }
}
