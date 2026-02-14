package com.example.trilhospt.ui.adapters

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trilhospt.data.remote.dto.BadgeDto
import com.example.trilhospt.databinding.ItemBadgeBinding

class BadgeAdapter(private var earnedBadges: List<BadgeDto>) :
    RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    data class BadgeItem(
        val type: String,
        val name: String,
        val iconRes: Int,
        var isEarned: Boolean = false
    )

    private val allBadges = listOf(
        BadgeItem("first_trail", "Primeiro Trilho", android.R.drawable.btn_star_big_on),
        BadgeItem("5_trails", "5 Trilhos", android.R.drawable.ic_menu_agenda),
        BadgeItem("20_trails", "20 Trilhos", android.R.drawable.ic_menu_agenda),
        BadgeItem("10km", "10km", android.R.drawable.ic_menu_compass),
        BadgeItem("50km", "50km", android.R.drawable.ic_menu_compass),
        BadgeItem("100km", "100km", android.R.drawable.ic_menu_compass),
        BadgeItem("photographer", "Fotógrafo", android.R.drawable.ic_menu_camera),
        BadgeItem("explorer", "Explorador", android.R.drawable.ic_menu_mapmode)
    )
    
    // List to display (initially all set to unearned, updated in updateBadges)
    private var displayList: List<BadgeItem> = allBadges.map { it.copy() }

    init {
        updateDisplayList()
    }

    class BadgeViewHolder(val binding: ItemBadgeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val binding = ItemBadgeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BadgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = displayList[position]
        
        holder.binding.tvBadgeName.text = badge.name
        holder.binding.ivBadgeIcon.setImageResource(badge.iconRes)
        
        if (badge.isEarned) {
            // Full color, normal opacity
            holder.binding.ivBadgeIcon.colorFilter = null
            holder.binding.ivBadgeIcon.alpha = 1.0f
            
            // If using standard Android icons that are black/white, we might want to tint them Yellow/Gold
            // But if they are standard colored icons (like btn_star_big_on is yellow), we leave them.
            // ic_menu_* are usually white or black. Let's tint them Gold if earned, or keep original if it's a star.
            if (badge.iconRes == android.R.drawable.btn_star_big_on) {
                // Keep original (Yellow star)
                holder.binding.ivBadgeIcon.clearColorFilter()
            } else {
                 // Tint Gold for other icons when earned
                 holder.binding.ivBadgeIcon.setColorFilter(android.graphics.Color.parseColor("#FFD700"))
            }

        } else {
            // Grayscale and lower opacity
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            val filter = ColorMatrixColorFilter(matrix)
            holder.binding.ivBadgeIcon.colorFilter = filter
            holder.binding.ivBadgeIcon.alpha = 0.5f
        }
    }

    override fun getItemCount() = displayList.size

    fun updateBadges(newEarnedBadges: List<BadgeDto>) {
        earnedBadges = newEarnedBadges
        updateDisplayList()
        notifyDataSetChanged()
    }
    
    private fun updateDisplayList() {
        // Reset display list
        displayList = allBadges.map { it.copy() }
        
        // Mark earned ones
        earnedBadges.forEach { earned ->
            displayList.find { it.type.equals(earned.badgeType, ignoreCase = true) }?.isEarned = true
        }
    }
}
