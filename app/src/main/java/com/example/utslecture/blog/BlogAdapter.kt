package com.example.utslecture.blog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.utslecture.R
import com.example.utslecture.data.Blog
import java.text.SimpleDateFormat
import java.util.*

class BlogAdapter(private val blogs: List<Blog>, private val onNewsClick: (Blog) -> Unit) :
    RecyclerView.Adapter<BlogAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.blog_item, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val blog = blogs[position]
        holder.bind(blog)
    }

    override fun getItemCount(): Int = blogs.size

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val authorTextView: TextView = itemView.findViewById(R.id.tv_author)
        private val dateTextView: TextView = itemView.findViewById(R.id.tv_date)
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_title)
        private val newsImageView: ImageView = itemView.findViewById(R.id.iv_news_image)

        fun bind(blog: Blog) {
            authorTextView.text = blog.username
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            dateTextView.text = blog.uploadDate?.let { dateFormat.format(it) } ?: ""
            titleTextView.text = blog.title

            Glide.with(itemView.context)
                .load(blog.image)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(newsImageView)

            itemView.setOnClickListener {
                onNewsClick(blog)
            }
        }
    }
}