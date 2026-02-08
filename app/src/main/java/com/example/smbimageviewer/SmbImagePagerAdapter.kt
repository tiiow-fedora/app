package com.example.smbimageviewer

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smbimageviewer.databinding.ItemImageBinding

class SmbImagePagerAdapter(
    private val images: List<ByteArray>
) : RecyclerView.Adapter<SmbImagePagerAdapter.ImageViewHolder>() {

    class ImageViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemImageBinding.inflate(inflater, parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageBytes = images[position]
        val imageView: ImageView = holder.binding.imageView
        Glide.with(imageView)
            .asBitmap()
            .load(imageBytes)
            .into(imageView)
    }

    override fun getItemCount(): Int = images.size
}
