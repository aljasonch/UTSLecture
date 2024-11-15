package com.example.utslecture.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.example.utslecture.R

class Profile : Fragment() {
    private lateinit var editProfileButton: Button
    private lateinit var settingButton: ImageView
    private lateinit var createBlogButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editProfileButton = view.findViewById<Button>(R.id.edit_profile_button)
        editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }
        settingButton = view.findViewById<ImageView>(R.id.SettingButton)
        settingButton.setOnClickListener {
            findNavController().navigate(R.id.Settings)
        }
        createBlogButton = view.findViewById<Button>(R.id.CreateBlogButton)
        createBlogButton.setOnClickListener {
            findNavController().navigate(R.id.CreateBlog)
        }
    }
}