package com.example.btqt_nhom3.Photo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.btqt_nhom3.R
import java.io.File

import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

class PhotoPagerAdapter(private val photos: List<File>) :
    RecyclerView.Adapter<PhotoPagerAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: SubsamplingScaleImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_full_photo, parent, false) // attachToRoot = false
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val file = photos[position]
        holder.imageView.setImage(ImageSource.uri(file.absolutePath))
        holder.imageView.minScale = 1f
        holder.imageView.maxScale = 5f
    }

    override fun getItemCount() = photos.size
}
