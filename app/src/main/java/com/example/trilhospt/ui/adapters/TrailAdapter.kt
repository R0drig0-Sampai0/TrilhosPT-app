package com.example.trilhospt.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.trilhospt.data.remote.dto.TrailDto
import com.example.trilhospt.databinding.ItemTrailBinding

class TrailAdapter(
    private var trails: List<TrailDto>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<TrailAdapter.TrailViewHolder>() {

    fun updateTrails(newTrails: List<TrailDto>) {
        trails = newTrails
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrailViewHolder {
        val binding = ItemTrailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrailViewHolder, position: Int) {
        holder.bind(trails[position])
    }

    override fun getItemCount() = trails.size

    inner class TrailViewHolder(private val binding: ItemTrailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(trail: TrailDto) {
            binding.tvTrailName.text = trail.name ?: "Trilho Desconhecido"
            binding.tvDifficulty.text = translateDifficulty(trail.difficulty)
            binding.tvDistance.text = "${trail.distance ?: 0.0} km"
            
            // Load image using Coil
            val imageUrl = getTrailImageUrl(trail)
            binding.ivTrailImage.load(imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_gallery)
            }
            
            // Show rating if available
            val rating = trail.avgRating
            if (rating != null && rating > 0) {
                binding.tvRating.text = String.format("%.1f", rating)
                binding.ratingContainer.visibility = android.view.View.VISIBLE
            } else {
                binding.ratingContainer.visibility = android.view.View.GONE
            }
            
            binding.root.setOnClickListener {
                trail.id?.let { id -> onItemClick(id) }
            }
        }

        private fun translateDifficulty(difficulty: String?): String {
            return when(difficulty?.lowercase()) {
                "easy" -> "Fácil"
                "moderate" -> "Moderado"
                "hard" -> "Difícil"
                "expert" -> "Expert"
                else -> difficulty ?: "N/A"
            }
        }
        
        private fun getTrailImageUrl(trail: TrailDto): String? {
            // Option 1: Use first_photo_url if provided by API
            trail.firstPhotoUrl?.let { return buildFullUrl(it) }
            
            // Option 2: Get first photo from photos array
            trail.photos?.firstOrNull()?.let { photo ->
                return buildFullUrl(photo.image)
            }
            
            // No photo available
            return null
        }
        
        private fun buildFullUrl(path: String?): String? {
            if (path == null) return null
            
            // If already a full URL, return as is
            if (path.startsWith("http")) return path
            
            // Otherwise, construct full URL with base URL
            val baseUrl = "http://10.129.146.48:8000"
            return "$baseUrl$path"
        }
    }
}
