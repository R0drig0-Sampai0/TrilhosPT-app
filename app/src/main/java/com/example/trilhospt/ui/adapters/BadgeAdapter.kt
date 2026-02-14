package com.example.trilhospt.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trilhospt.R
import com.example.trilhospt.data.remote.dto.BadgeDto
import com.example.trilhospt.databinding.ItemBadgeBinding

class BadgeAdapter(private var badges: List<BadgeDto>) :
    RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    class BadgeViewHolder(val binding: ItemBadgeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val binding = ItemBadgeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BadgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]
        val context = holder.itemView.context

        val (name, iconRes) = when (badge.badgeType.lowercase()) {
            "first_trail" -> "Primeiro Trilho" to android.R.drawable.btn_star_big_on
            "10km" -> "10km" to android.R.drawable.ic_menu_compass
            "50km" -> "50km" to android.R.drawable.ic_menu_compass
            "100km" -> "100km" to android.R.drawable.ic_menu_compass
            "5_trails" -> "5 Trilhos" to android.R.drawable.ic_menu_agenda
            "20_trails" -> "20 Trilhos" to android.R.drawable.ic_menu_agenda
            "photographer" -> "Fotógrafo" to android.R.drawable.ic_menu_camera
            "explorer" -> "Explorador" to android.R.drawable.ic_menu_mapmode
            else -> badge.badgeType to android.R.drawable.btn_star
        }

        holder.binding.tvBadgeName.text = name
        holder.binding.ivBadgeIcon.setImageResource(iconRes)
    }

    override fun getItemCount() = badges.size

    fun updateBadges(newBadges: List<BadgeDto>) {
        badges = newBadges
        notifyDataSetChanged()
    }
}
