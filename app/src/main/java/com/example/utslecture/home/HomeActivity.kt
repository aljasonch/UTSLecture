package com.example.utslecture.home

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.utslecture.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var bottomNavigation: BottomNavigationView
    private val channelId = "blogNotifications"
    private var notificationId = 1

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("Permission", "Granted")
            } else {
                Log.d("Permission", "Denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize and set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.hide()

        // Set up Navigation Component
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupActionBarWithNavController(navController)
        bottomNavigation = findViewById(R.id.bottom_nav)
        bottomNavigation.setupWithNavController(navController)

        hideBottomNavigation()
        setupBottomNavigationWithAnimation()
        createNotificationChannel()
        askNotificationPermission()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private var lastSelectedTabId: Int = R.id.Home

    /**
     * Sets up the bottom navigation with animations based on the selected tab index.
     * Utilizes launchSingleTop to prevent multiple instances of the same fragment.
     */
    private fun setupBottomNavigationWithAnimation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            val currentTabIndex = getTabIndex(item.itemId)
            val lastTabIndex = getTabIndex(lastSelectedTabId)

            val navOptionsBuilder = NavOptions.Builder()
                .setLaunchSingleTop(true) // Prevent multiple instances

            // Determine animation based on navigation direction
            if (currentTabIndex > lastTabIndex) {
                navOptionsBuilder.setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_left)
                    .setPopEnterAnim(R.anim.fade_in)
                    .setPopExitAnim(R.anim.fade_out)
            } else if (currentTabIndex < lastTabIndex) {
                navOptionsBuilder.setEnterAnim(R.anim.slide_in_left)
                    .setExitAnim(R.anim.slide_out_right)
                    .setPopEnterAnim(R.anim.fade_in)
                    .setPopExitAnim(R.anim.fade_out)
            } else {
                navOptionsBuilder.setEnterAnim(R.anim.fade_in)
                    .setExitAnim(R.anim.fade_out)
            }

            val navOptions = navOptionsBuilder.build()

            // Navigate to the selected fragment
            navController.navigate(item.itemId, null, navOptions)

            lastSelectedTabId = item.itemId
            true
        }
    }

    /**
     * Maps the menu item IDs to their respective indices for animation purposes.
     */
    private fun getTabIndex(itemId: Int): Int {
        return when (itemId) {
            R.id.Home -> 0
            R.id.Search -> 1
            R.id.Bookmark -> 2
            R.id.Profile -> 3
            else -> -1 // Default if ID not found
        }
    }

    /**
     * Hides the bottom navigation view.
     */
    fun hideBottomNavigation() {
        bottomNavigation.visibility = View.GONE
    }

    /**
     * Shows the bottom navigation view.
     */
    fun showBottomNavigation() {
        bottomNavigation.visibility = View.VISIBLE
    }

    /**
     * Creates a notification channel for devices running Android O and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Blog Notifications"
            val descriptionText = "Notifications for blog milestones"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Displays a notification with the given title and message.
     */
    fun showNotification(title: String, message: String) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askNotificationPermission()
            return
        }

        createNotificationChannel()
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo_ailog) // Replace with your notification icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            Log.d("HomeActivity", "Trying to send notification: $title, $message")
            notify(notificationId++, builder.build())
        }
    }

    /**
     * Requests notification permission if not already granted (for Android Tiramisu and above).
     */
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show an explanation to the user asynchronously
                }

                else -> {
                    // Directly ask for the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}