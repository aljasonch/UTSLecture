package com.example.utslecture.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.utslecture.R
import com.example.utslecture.data.Blog
import java.text.SimpleDateFormat
import java.util.*

class ProfileBlogAdapter(
    private var blogList: List<Blog>,
    private val onUpdateClickListener: (Blog) -> Unit,
    private val onDeleteClickListener: (Blog) -> Unit
) : RecyclerView.Adapter<ProfileBlogAdapter.BlogViewHolder>() {

    fun setData(newBlogList: List<Blog>) {
        blogList = newBlogList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blog_profile, parent, false)
        return BlogViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blog = blogList[position]
        holder.bind(blog)

        holder.ivBlogMenu.setOnClickListener {
            showPopupMenu(holder.ivBlogMenu, blog)
        }
    }

    override fun getItemCount(): Int = blogList.size

    inner class BlogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivBlogImage: ImageView = itemView.findViewById(R.id.iv_blog_image)
        val tvBlogTitle: TextView = itemView.findViewById(R.id.tv_blog_title)
        val tvBlogDate: TextView = itemView.findViewById(R.id.tv_blog_date)
        val ivBlogMenu: ImageView = itemView.findViewById(R.id.iv_blog_menu)

        fun bind(blog: Blog) {
            tvBlogTitle.text = blog.title

            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            tvBlogDate.text = blog.uploadDate?.let { dateFormat.format(it) } ?: ""

            Glide.with(itemView.context)
                .load(blog.image)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(ivBlogImage)

        }
    }

    private fun showPopupMenu(view: View, blog: Blog) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.blog_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_update -> {
                    onUpdateClickListener(blog)
                    true
                }
                R.id.action_delete -> {
                    onDeleteClickListener(blog)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}