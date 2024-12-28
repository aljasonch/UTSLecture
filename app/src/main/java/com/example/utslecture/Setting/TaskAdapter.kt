package com.example.utslecture.Setting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.utslecture.R
import com.example.utslecture.data.TaskItem
import com.example.utslecture.databinding.TaskItemBinding

class TaskAdapter(
    private val taskList: MutableList<TaskItem>,
    private var userPoints: Int,
    private val onClaimClicked: (TaskItem) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        val isClaimable = when (task.requirementType) {
            "likes", "views" -> task.currentProgress != null && task.currentProgress >= (task.requirementValue ?: 0)
            "newUser" -> true
            else -> false
        }

        holder.bind(task, onClaimClicked,isClaimable)


    }

    override fun getItemCount(): Int = taskList.size

    fun submitList(newList: List<TaskItem>) {
        taskList.clear()
        taskList.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateUserPoints(points: Int) {
        userPoints = points
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(private val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: TaskItem, onClaimClicked: (TaskItem) -> Unit,isClaimable: Boolean) {
            binding.taskTitle.text = task.title
            binding.taskPoints.text = "+${task.points} points"

            if (task.requirementType != null && task.requirementValue != null && task.currentProgress != null) {
                binding.taskProgress.text = "${task.currentProgress}/${task.requirementValue} ${task.requirementType}"
                binding.taskProgress.visibility = View.VISIBLE
            } else {
                binding.taskProgress.visibility = View.GONE
            }

            setButtonAppearance(task.isClaimed,isClaimable)

            binding.btnTaskClaim.setOnClickListener {
                if (!task.isClaimed && isClaimable) {
                    onClaimClicked(task)
                }
            }
        }

        fun setButtonAppearance(isClaimed: Boolean, isClaimable: Boolean) {
            if (isClaimed) {
                binding.btnTaskClaim.isEnabled = false
                binding.btnTaskClaim.text = "Claimed"
                binding.btnTaskClaim.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.gray
                    )
                )
            } else if (!isClaimable) {
                binding.btnTaskClaim.isEnabled = false
                binding.btnTaskClaim.text = "Claim"
                binding.btnTaskClaim.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.gray
                    )
                )
            } else {
                binding.btnTaskClaim.isEnabled = true
                binding.btnTaskClaim.text = "Claim"
                binding.btnTaskClaim.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.primary_button
                    )
                )
            }
        }
    }
}