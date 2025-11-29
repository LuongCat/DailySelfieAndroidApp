package com.example.btqt_nhom3.Photo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.btqt_nhom3.R
import java.io.File

class SelfieAdapter(
    private val photos: List<File>,
    private val onPhotoClick: (File) -> Unit,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<SelfieAdapter.VH>() {

    private val selected = mutableSetOf<File>()
    var selectionMode = false

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgSelfie)
        val overlay: View = v.findViewById(R.id.selectionOverlay)
        val shadow: View = v.findViewById(R.id.selectionShadow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selfie, parent, false)
        return VH(v)
    }

    override fun getItemCount() = photos.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val f = photos[position]
        holder.img.load(f)

        val isSel = selected.contains(f)
        holder.overlay.visibility = if (isSel) View.VISIBLE else View.GONE
        holder.shadow.alpha = if (isSel) 0.3f else 0f

        holder.itemView.setOnClickListener {
            if (selectionMode) toggle(f)
            else onPhotoClick(f)
        }

        holder.itemView.setOnLongClickListener {
            if (!selectionMode) {
                selectionMode = true
                toggle(f)
            }
            true
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun toggle(f: File) {
        if (selected.contains(f)) selected.remove(f) else selected.add(f)

        if (selected.isEmpty()) selectionMode = false

        notifyDataSetChanged()
        onSelectionChanged(selected.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelection(notify: Boolean = true) {
        selected.clear()
        selectionMode = false
        notifyDataSetChanged()
        if (notify) onSelectionChanged(0)
    }

    fun getSelectedPhotos(): List<File> = selected.toList()
}
