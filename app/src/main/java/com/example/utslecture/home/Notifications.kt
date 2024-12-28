package com.example.utslecture.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utslecture.R
import com.example.utslecture.data.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Notifications : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var notificationsRecyclerView: RecyclerView
    private val notificationList = mutableListOf<Notification>()
    private lateinit var adapter: NotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView)
        adapter = NotificationsAdapter(notificationList)
        notificationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        notificationsRecyclerView.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (!userId.isNullOrEmpty()) {
            fetchNotifications(userId)
        }
    }
    private fun fetchNotifications(userId: String) {
        db.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("NotificationsFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    notificationList.clear()
                    for (doc in snapshots.documents) {
                        val notif = doc.toObject(Notification::class.java)
                        if (notif != null) {
                            notificationList.add(notif)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}