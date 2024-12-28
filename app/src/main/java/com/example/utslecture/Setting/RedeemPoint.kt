package com.example.utslecture.Setting

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.utslecture.R
import com.example.utslecture.data.RedeemItem
import com.example.utslecture.data.TaskItem
import com.example.utslecture.databinding.FragmentRedeemPointBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class RedeemPoint : Fragment() {

    private var _binding: FragmentRedeemPointBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var redeemAdapter: RedeemAdapter
    private var userPoints: Int = 0
    private var userPointsListenerRegistration: ListenerRegistration? = null
    private var redeemedItemIds = mutableMapOf<String, String>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRedeemPointBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        setupBackButton()
        setupDailyCheckIn()
        setupTaskRecyclerView()
        setupRedeemRecyclerView()
        loadUserPoints()
        loadTasks()
        loadRedeemItems()
    }

    override fun onStop() {
        super.onStop()
        userPointsListenerRegistration?.remove()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupDailyCheckIn() {
        binding.dailyCheckIn.setOnClickListener {
            performDailyCheckIn()
        }
        updateDailyCheckInUI()
    }

    private fun performDailyCheckIn() {
        val today = Calendar.getInstance()
        val lastCheckIn = Calendar.getInstance()

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val lastCheckInTimestamp = document.getTimestamp("lastCheckInDate")
                    val currentStreak = document.getLong("checkInStreak")?.toInt() ?: 0

                    if (lastCheckInTimestamp != null) {
                        lastCheckIn.time = lastCheckInTimestamp.toDate()
                        val daysDifference = daysBetween(lastCheckIn.time, today.time)

                        if (daysDifference == 0) {
                            return@addOnSuccessListener
                        } else if (daysDifference == 1) {
                            var newStreak = currentStreak + 1
                            if (newStreak > 7) newStreak = 7
                            val pointsToAdd = newStreak
                            updateUserPoints(pointsToAdd)
                            updateCheckInStreak(newStreak)
                        } else {
                            val pointsToAdd = 1
                            updateUserPoints(pointsToAdd)
                            updateCheckInStreak(1)
                        }
                    } else {
                        val pointsToAdd = 1
                        updateUserPoints(pointsToAdd)
                        updateCheckInStreak(1)
                    }
                } else {
                }
            }
            .addOnFailureListener { e ->
                Log.e("RedeemPoint", "Error getting user data: ", e)
            }
    }

    private fun daysBetween(startDate: Date, endDate: Date): Int {
        val diff = endDate.time - startDate.time
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }

    private fun updateCheckInStreak(streak: Int) {
        firestore.collection("users").document(userId)
            .update(
                mapOf(
                    "checkInStreak" to streak,
                    "lastCheckInDate" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener {
                updateDailyCheckInUI()
            }
            .addOnFailureListener { e ->
                Log.e("RedeemPoint", "Error updating check-in streak: ", e)
            }
    }

    private fun updateDailyCheckInUI() {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val streak = document.getLong("checkInStreak")?.toInt() ?: 0
                    updateDotSelection(streak)
                }
            }
            .addOnFailureListener { e ->
                Log.e("RedeemPoint", "Error fetching check-in streak for UI: ", e)
            }
    }

    private fun updateDotSelection(streak: Int) {
        val dots = listOf(
            binding.dot1, binding.dot2, binding.dot3, binding.dot4,
            binding.dot5, binding.dot6, binding.dot7
        )

        for (i in 0 until dots.size) {
            if (i < streak) {
                dots[i].setBackgroundResource(R.drawable.circle_selected)
                dots[i].setTextColor(resources.getColor(android.R.color.white, null))
            } else {
                dots[i].setBackgroundResource(R.drawable.circle_unselected)
                dots[i].setTextColor(resources.getColor(android.R.color.black, null))
            }
        }
    }

    private fun setupTaskRecyclerView() {
        taskAdapter = TaskAdapter(mutableListOf(), userPoints) { task ->
            claimTask(task)
        }
        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun setupRedeemRecyclerView() {
        redeemAdapter = RedeemAdapter(mutableListOf(), userPoints) { redeemItem, redeemCode ->
            if (redeemCode != null){
                showRedeemCodeDialog(redeemItem.name,redeemCode)
            }else{
                redeemItem(redeemItem)
            }
        }
        binding.rvRedeemItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = redeemAdapter
        }
    }
    private fun loadUserPoints() {
        userPointsListenerRegistration = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("RedeemPoint", "Listen failed: ", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    userPoints = snapshot.getLong("points")?.toInt() ?: 0
                    if (_binding != null) {
                        binding.tvPoints.text = userPoints.toString()
                        taskAdapter.updateUserPoints(userPoints)
                        redeemAdapter.updateUserPoints(userPoints)
                    }
                }
            }
    }

    private fun updateUserPoints(points: Int) {
        firestore.collection("users").document(userId)
            .update("points", FieldValue.increment(points.toLong()))
            .addOnFailureListener { e ->
                Log.e("RedeemPoint", "Error updating points: ", e)
            }
    }

    private fun loadTasks() {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    val claimedTasks =
                        userDocument.get("claimedTasks") as? List<String> ?: emptyList()

                    firestore.collection("tasks")
                        .get()
                        .addOnSuccessListener { taskSnapshot ->
                            val tasks = mutableListOf<TaskItem>()
                            for (document in taskSnapshot.documents) {
                                val taskId = document.id
                                val title = document.getString("title") ?: ""
                                val points = document.getLong("points")?.toInt() ?: 0
                                val requirementType = document.getString("requirementType")
                                val requirementValue = document.getLong("requirementValue")?.toInt()

                                var currentProgress: Int? = null

                                when (requirementType) {
                                    "likes" -> {
                                        checkBlogLikes(requirementValue ?: 0) { progress ->
                                            tasks.add(
                                                TaskItem(
                                                    taskId,
                                                    title,
                                                    points,
                                                    claimedTasks.contains(taskId),
                                                    requirementType,
                                                    requirementValue,
                                                    progress
                                                )
                                            )
                                            taskAdapter.submitList(tasks)
                                        }
                                    }

                                    "views" -> {
                                        checkBlogViews(requirementValue ?: 0) { progress ->
                                            tasks.add(
                                                TaskItem(
                                                    taskId,
                                                    title,
                                                    points,
                                                    claimedTasks.contains(taskId),
                                                    requirementType,
                                                    requirementValue,
                                                    progress
                                                )
                                            )
                                            taskAdapter.submitList(tasks)
                                        }
                                    }

                                    "newUser" -> {
                                        tasks.add(
                                            TaskItem(
                                                taskId,
                                                title,
                                                points,
                                                claimedTasks.contains(taskId),
                                                requirementType,
                                                requirementValue
                                            )
                                        )
                                        taskAdapter.submitList(tasks)
                                    }

                                    else -> {
                                        tasks.add(
                                            TaskItem(
                                                taskId,
                                                title,
                                                points,
                                                claimedTasks.contains(taskId),
                                                requirementType,
                                                requirementValue
                                            )
                                        )
                                        taskAdapter.submitList(tasks)
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("RedeemPoint", "Error loading tasks: ", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("RedeemPoint", "Error loading user data for tasks: ", e)
            }
    }

    private fun checkBlogLikes(requiredLikes: Int, onProgress: (Int) -> Unit) {
        firestore.collection("blogs")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { blogSnapshot ->
                var totalLikes = 0
                for (document in blogSnapshot.documents) {
                    totalLikes += document.getLong("likes")?.toInt() ?: 0
                }
                onProgress(totalLikes)
            }
            .addOnFailureListener { e ->
                Log.e("RedeemPoint", "Error checking blog likes: ", e)
                onProgress(0)
            }
    }

    private fun checkBlogViews(requiredViews: Int, onProgress: (Int) -> Unit) {
        firestore.collection("blogs")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { blogSnapshot ->
                var totalViews = 0
                for (document in blogSnapshot.documents) {
                    totalViews += document.getLong("views")?.toInt() ?: 0
                }
                onProgress(totalViews)
            }
            .addOnFailureListener { e ->
                Log.e("RedeemPoint", "Error checking blog views: ", e)
                onProgress(0)
            }
    }

    private fun claimTask(task: TaskItem) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val claimedTasks = document.get("claimedTasks") as? MutableList<String> ?: mutableListOf()
                    if (!claimedTasks.contains(task.id)) {
                        when (task.requirementType) {
                            "likes" -> {
                                checkBlogLikes(task.requirementValue ?: 0) { totalLikes ->
                                    if (totalLikes >= (task.requirementValue ?: 0)) {
                                        addClaimedTaskAndUpdatePoints(claimedTasks, task)
                                    } else {
                                        Log.d("RedeemPoint", "Not enough likes to claim this task.")
                                    }
                                }
                            }
                            "views" -> {
                                checkBlogViews(task.requirementValue ?: 0) { totalViews ->
                                    if (totalViews >= (task.requirementValue ?: 0)) {
                                        addClaimedTaskAndUpdatePoints(claimedTasks, task)
                                    } else {
                                        Log.d("RedeemPoint", "Not enough views to claim this task.")
                                    }
                                }
                            }
                            "newUser" -> {
                                addClaimedTaskAndUpdatePoints(claimedTasks, task)
                            }
                            else -> {
                                Log.d("RedeemPoint", "Unknown requirement type or no requirement specified.")
                            }
                        }
                    } else {
                        Log.d("RedeemPoint", "Task already claimed.")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("RedeemPoint", "Error accessing user data for claiming task: ", e)
            }
    }

    private fun addClaimedTaskAndUpdatePoints(claimedTasks: MutableList<String>, task: TaskItem) {
        claimedTasks.add(task.id)
        firestore.collection("users").document(userId)
            .update("claimedTasks", claimedTasks)
            .addOnSuccessListener {
                updateUserPoints(task.points)
                loadTasks()
            }
            .addOnFailureListener { e ->
                Log.e("RedeemPoint", "Error updating claimed tasks: ", e)
            }
    }

    private fun loadRedeemItems() {
        firestore.collection("redeemItems")
            .get()
            .addOnSuccessListener { snapshot ->
                val redeemItemsList = snapshot.documents.map { document ->
                    val redeemedItem  =  document.id?.let { redeemedItemIds[it] }
                    RedeemItem(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        points = document.getLong("points")?.toInt() ?: 0,
                        imageUrl = document.getString("imageUrl") ?: "",
                        redeemCode = redeemedItem
                    )
                }
                redeemAdapter.submitList(redeemItemsList)
                loadRedeemedItems()
            }
            .addOnFailureListener { e ->
                Log.e("RedeemPoint", "Error loading redeem items: ", e)
            }
    }

    private fun loadRedeemedItems() {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val redeemedItems = document.get("redeemedItems") as? List<Map<String,String>> ?: emptyList()
                    redeemedItemIds.clear()
                    redeemedItems.forEach {
                        if (it.containsKey("itemId") && it.containsKey("redeemCode")) {
                            redeemedItemIds[it["itemId"].toString()] = it["redeemCode"].toString()
                        }
                    }

                }

            }
            .addOnFailureListener { e ->
                Log.e("RedeemPoint", "Error loading redeemed items: ", e)
            }


    }

    private fun redeemItem(item: RedeemItem) {
        if (userPoints >= item.points) {
            val redeemCode = generateRedeemCode()
            firestore.collection("users").document(userId)
                .update(
                    "points",
                    FieldValue.increment(-item.points.toLong()),
                    "redeemedItems",
                    FieldValue.arrayUnion(mapOf("itemId" to item.id, "redeemCode" to redeemCode))
                )
                .addOnSuccessListener {
                    loadRedeemedItems()
                    redeemAdapter.updateUserPoints(userPoints - item.points)
                    Log.d("RedeemPoint", "Redeemed ${item.name} with code: $redeemCode")
                }
                .addOnFailureListener { e ->
                    Log.e("RedeemPoint", "Error redeeming item: ", e)
                }
        } else {
            AlertDialog.Builder(context)
                .setTitle("Insufficient Points")
                .setMessage("You don't have enough points to redeem ${item.name}.")
                .setPositiveButton("OK", null)
                .show()
            Log.d("RedeemPoint", "Not enough points to redeem ${item.name}")
        }
    }

    private fun showRedeemCodeDialog(itemName: String, redeemCode: String) {
        AlertDialog.Builder(context)
            .setTitle("Redeem Code")
            .setMessage("You have redeemed $itemName!\nYour code is: $redeemCode")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun generateRedeemCode(): String {
        return UUID.randomUUID().toString().substring(0, 8).uppercase(Locale.ROOT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}