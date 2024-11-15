package com.example.utslecture.profile

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.utslecture.R
import com.google.firebase.auth.FirebaseAuth

class Setting : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        val logOutTextView: TextView = view.findViewById(R.id.logOut)
        logOutTextView.setOnClickListener {
            logout()
        }

        return view
    }

    private fun logout() {
        auth.signOut()
        showLogoutSuccessMessage()
        findNavController().navigate(R.id.loginFragment)
    }
    private fun showLogoutSuccessMessage() {

        val toast = Toast.makeText(context, "Logout Successful", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }
}
