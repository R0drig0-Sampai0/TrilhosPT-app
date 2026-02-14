package com.example.trilhospt.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.trilhospt.R
import com.example.trilhospt.data.remote.dto.TrailDto
import com.example.trilhospt.databinding.ItemTrailHorizontalBinding

class HorizontalTrailAdapter(
    private var trails: List<TrailDto>,
    private val onTrailClick: (TrailDto) -> Unit
) : RecyclerView.Adapter<HorizontalTrailAdapter.TrailViewHolder>() {

    class TrailViewHolder(val binding: ItemTrailHorizontalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrailViewHolder {
        val binding = ItemTrailHorizontalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TrailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrailViewHolder, position: Int) {
        val trail = trails[position]
        
        holder.binding.tvTrailName.text = trail.name
        holder.binding.tvDistance.text = "${trail.distance ?: 0.0} km"
        
        // Difficulty badge with color
        val (difficultyText, difficultyColor) = when (trail.difficulty?.lowercase()) {
            "easy" -> "Fácil" to android.graphics.Color.parseColor("#4CAF50")
            "moderate" -> "Moderado" to android.graphics.Color.parseColor("#FF9800")
            "hard" -> "Difícil" to android.graphics.Color.parseColor("#F44336")
            "expert" -> "Expert" to android.graphics.Color.parseColor("#9C27B0")
            else -> "N/A" to android.graphics.Color.parseColor("#757575")
        }
        holder.binding.tvDifficultyBadge.text = difficultyText
        holder.binding.tvDifficultyBadge.setBackgroundColor(difficultyColor)
        
        // Show completed icon if trail is completed
        if (trail.completedTrailId != null) {
            holder.binding.ivCompletedIcon.visibility = android.view.View.VISIBLE
        } else {
            holder.binding.ivCompletedIcon.visibility = android.view.View.GONE
        }
        
        // Load image (using the same logic as existing adapters)
        val photoUrl = trail.firstPhotoUrl ?: trail.photos?.firstOrNull()?.image
        if (photoUrl != null) {
            val fullUrl = if (photoUrl.startsWith("http")) photoUrl else "http://10.129.146.48:8000$photoUrl"
            holder.binding.ivTrailImage.load(fullUrl) {
                crossfade(true)
                placeholder(R.drawable.bg_gradient_overlay)
            }
        } else {
            holder.binding.ivTrailImage.setImageResource(R.drawable.bg_gradient_overlay)
        }

        holder.itemView.setOnClickListener { onTrailClick(trail) }
    }

    override fun getItemCount() = trails.size

    fun updateTrails(newTrails: List<TrailDto>) {
        trails = newTrails
        notifyDataSetChanged()
    }
}
