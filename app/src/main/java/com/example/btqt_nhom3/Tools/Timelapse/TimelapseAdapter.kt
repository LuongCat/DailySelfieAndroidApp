package com.example.btqt_nhom3.Tools.Timelapse

import android.annotation.SuppressLint
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.btqt_nhom3.R
import java.io.File

class TimelapseAdapter(
    private var items: List<File>,
    private val onItemClick: (File) -> Unit,
    private val onDelete: (File) -> Unit
) : RecyclerView.Adapter<TimelapseAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val thumb: ImageView = v.findViewById(R.id.imgThumb)
        val name: TextView = v.findViewById(R.id.txtVideoName)
        val btnDelete: ImageButton = v.findViewById(R.id.btnDeleteVideo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timelapse, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val file = items[pos]

        holder.name.text = file.name

        // Tạo thumbnail cho video
        val thumbnail = ThumbnailUtils.createVideoThumbnail(
            file.absolutePath,
            MediaStore.Video.Thumbnails.MINI_KIND
        )
        holder.thumb.setImageBitmap(thumbnail)

        // Click vào item -> mở video
        holder.itemView.setOnClickListener { onItemClick(file) }

        // Nút xóa
        holder.btnDelete.setOnClickListener { onDelete(file) }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(newList: List<File>) {
        items = newList
        notifyDataSetChanged()
    }
}
