package com.example.trilhospt.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.trilhospt.R
import com.example.trilhospt.data.remote.dto.ReviewDto
import com.example.trilhospt.databinding.ItemReviewBinding
import com.example.trilhospt.data.remote.api.RetrofitClient

class ReviewAdapter(
    private var reviews: List<ReviewDto>,
    private val onUserClick: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        val context = holder.itemView.context

        holder.binding.tvUsername.text = review.user?.username ?: "Anonymous"
        holder.binding.tvRating.text = "⭐ ${review.rating}"
        holder.binding.tvComment.text = review.comment ?: ""
        holder.binding.tvDate.text = review.createdAt?.take(10) ?: "" // Show only date part

        // Load profile photo if available
        // Load profile photo if available
        val userPhoto = review.user?.profilePhoto
        if (!userPhoto.isNullOrEmpty()) {
            holder.binding.ivUserAvatar.clearColorFilter()
            
            val baseUrl = RetrofitClient.BASE_URL.trimEnd('/')
            val fullUrl = if (userPhoto.startsWith("http")) userPhoto else "$baseUrl/${userPhoto.trimStart('/')}"
            
            holder.binding.ivUserAvatar.load(fullUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.binding.ivUserAvatar.setImageResource(android.R.drawable.ic_menu_gallery)
            holder.binding.ivUserAvatar.setColorFilter(android.graphics.Color.GRAY) // Re-apply tint for placeholder
        }

        // Handle profile mapping click
        val userId = review.user?.id
        if (userId != null && onUserClick != null) {
            holder.binding.tvUsername.setOnClickListener { onUserClick.invoke(userId) }
            holder.binding.ivUserAvatar.setOnClickListener { onUserClick.invoke(userId) }
        }
    }

    override fun getItemCount() = reviews.size

    fun updateReviews(newReviews: List<ReviewDto>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}
