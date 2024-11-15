package com.example.utslecture.blog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.utslecture.R

class BlogAdapter(private val onNewsClick: () -> Unit) :
    RecyclerView.Adapter<BlogAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.blog_item, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 5
    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.findViewById<View>(R.id.newsContent).setOnClickListener {
                onNewsClick()
            }
        }
    }
}
