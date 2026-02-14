package com.example.trilhospt.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.trilhospt.R
import com.example.trilhospt.data.remote.dto.PhotoDto
import com.example.trilhospt.databinding.ItemPhotoCarouselBinding

class ProfilePhotoAdapter(
    private var photos: List<PhotoDto>,
    private val onPhotoClick: (PhotoDto) -> Unit
) : RecyclerView.Adapter<ProfilePhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(private val binding: ItemPhotoCarouselBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(photo: PhotoDto) {
            val imageUrl = photo.image ?: ""
            val fullUrl = if (imageUrl.startsWith("http")) imageUrl else "http://10.129.146.48:8000$imageUrl"
            
            binding.ivPhoto.load(fullUrl) {
                crossfade(true)
                placeholder(R.drawable.bg_gradient_overlay)
                error(R.drawable.bg_gradient_overlay)
            }
            
            binding.root.setOnClickListener {
                onPhotoClick(photo)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoCarouselBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount() = photos.size

    fun updatePhotos(newPhotos: List<PhotoDto>) {
        photos = newPhotos
        notifyDataSetChanged()
    }
}
