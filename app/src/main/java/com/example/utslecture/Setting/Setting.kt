package com.example.utslecture.Setting

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.utslecture.R
import com.google.api.Distribution.BucketOptions.Linear
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
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        val logOutTextView: TextView = view.findViewById(R.id.logOut)
        logOutTextView.setOnClickListener {
            logout()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val helpButton =
            view.findViewById<LinearLayout>(R.id.helpButton)
        val privacyButton =
            view.findViewById<LinearLayout>(R.id.privacyButton)
        val aboutButton =
            view.findViewById<LinearLayout>(R.id.aboutButton)
        val redeemButton =
            view.findViewById<LinearLayout>(R.id.redeemButton)
        super.onViewCreated(view, savedInstanceState)
        helpButton.setOnClickListener {
            findNavController().navigate(R.id.helpFragment)
        }
        privacyButton.setOnClickListener {
            findNavController().navigate(R.id.privacyFragment)
        }
        aboutButton.setOnClickListener {
            findNavController().navigate(R.id.aboutFragment)
        }
        redeemButton.setOnClickListener{
            findNavController().navigate(R.id.redeemFragment)
        }
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
