package com.example.utslecture.auth

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

class Login : Fragment() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_login, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as HomeActivity).hideBottomNavigation()
        val registerButton = view.findViewById<TextView>(R.id.register_text)
        val emailInput = view.findViewById<EditText>(R.id.email_input_field)
        val passwordInput =
            view.findViewById<EditText>(R.id.password_input_field)
        auth = FirebaseAuth.getInstance()
        registerButton.setOnClickListener{
            findNavController().navigate(R.id.registerFragment)
        }
        val loginButton = view.findViewById<Button>(R.id.submit_button)
        loginButton.setOnClickListener{
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Email and Password are required",
                    Toast.LENGTH_SHORT).show()
            }else{
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            Log.d("Login", "signInWithEmail:success")
                            findNavController().navigate(R.id.Home)
                        } else {
                            Log.w("Login", "signInWithEmail:failure", task.exception)
                            Toast.makeText(requireContext(), "Email or Password Wrong",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}
