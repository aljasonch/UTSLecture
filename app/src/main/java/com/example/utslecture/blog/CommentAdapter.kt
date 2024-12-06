package com.example.utslecture.blog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.utslecture.R
import com.example.utslecture.data.Komentar

class CommentAdapter(private val comments: List<Komentar>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int = comments.size

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.comment_username)
        private val commentTextView: TextView = itemView.findViewById(R.id.comment_text)

        fun bind(comment: Komentar) {
            usernameTextView.text = comment.username + ":"
            commentTextView.text = comment.content

        }
    }
}