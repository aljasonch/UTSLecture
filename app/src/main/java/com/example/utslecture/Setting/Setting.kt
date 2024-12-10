package com.example.utslecture.Setting

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.utslecture.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class Setting : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var notificationSwitch: Switch
    private lateinit var sharedPreferences: SharedPreferences
    private val prefsName = "settings_prefs"
    private val notificationKey = "notifications_enabled"

    // Permission Launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, enable notifications
            enableNotifications(true)
        } else {
            // Permission denied
            notificationSwitch.isChecked = false // Reset switch to off
            // Show explanation, possibly with a button to open app settings
            showNotificationPermissionRationale()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        sharedPreferences =
            requireActivity().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        createNotificationChannel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        // Wrap the content in a CoordinatorLayout (if not already)
        val coordinatorLayout = CoordinatorLayout(requireContext())
        coordinatorLayout.addView(view)

        // Initialize UI components
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { findNavController().popBackStack() }

        val logOutTextView: TextView = view.findViewById(R.id.logOut)
        logOutTextView.setOnClickListener { logout() }

        // Initialize Notification Switch
        notificationSwitch = view.findViewById(R.id.notificationSwitch)

        // Load switch status from SharedPreferences
        notificationSwitch.isChecked = sharedPreferences.getBoolean(notificationKey, false)

        // Set up switch listener
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Check for permission before enabling
                checkForNotificationPermission()
            } else {
                // Directly disable notifications
                enableNotifications(false)
            }
        }

        return coordinatorLayout // Return the CoordinatorLayout as the root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up navigation for other buttons
        view.findViewById<LinearLayout>(R.id.helpButton).setOnClickListener {
            findNavController().navigate(R.id.helpFragment)
        }
        view.findViewById<LinearLayout>(R.id.privacyButton).setOnClickListener {
            findNavController().navigate(R.id.privacyFragment)
        }
        view.findViewById<LinearLayout>(R.id.aboutButton).setOnClickListener {
            findNavController().navigate(R.id.aboutFragment)
        }
        view.findViewById<LinearLayout>(R.id.redeemButton).setOnClickListener {
            findNavController().navigate(R.id.redeemFragment)
        }
    }

    private fun checkForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted
                enableNotifications(true)
            } else {
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    // Show rationale before requesting
                    showNotificationPermissionRationale()
                } else {
                    // Request the permission. The registered ActivityResultCallback gets the result of this request.
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // No runtime permission needed for older versions, just enable
            enableNotifications(true)
        }
    }

    private fun enableNotifications(enable: Boolean) {
        // Save the setting to SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putBoolean(notificationKey, enable)
        editor.apply()

        // Show a message about the setting
        val message = if (enable) "Notifikasi Diaktifkan" else "Notifikasi Dinonaktifkan"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showNotificationPermissionRationale() {
        val snackbar = Snackbar.make(
            requireView(), // Use requireView() to get the root view
            "Notification permission is required to show notifications.",
            Snackbar.LENGTH_INDEFINITE
        )

        // Add a button to the Snackbar to open app settings
        snackbar.setAction("Settings") {
            val intent = Intent()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            } else {
                // For older versions, provide a general way to open settings
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = android.net.Uri.fromParts("package", requireContext().packageName, null)
            }
            startActivity(intent)
        }

        snackbar.show()
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "my_channel_id"
    }
}