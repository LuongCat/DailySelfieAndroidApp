package com.example.btqt_nhom3.Photo

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.btqt_nhom3.R
import java.io.File

class SelfieAdapter(
    private val selfies: List<File>,
    private val onClick: (File) -> Unit
) : RecyclerView.Adapter<SelfieAdapter.SelfieViewHolder>() {

    class SelfieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgSelfie)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelfieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selfie, parent, false)
        return SelfieViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelfieViewHolder, position: Int) {
        val file = selfies[position]

        holder.imageView.load(file) {
            crossfade(true)
            placeholder(android.R.drawable.ic_menu_gallery)
            error(android.R.drawable.ic_delete)
        }

        holder.imageView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PhotoViewerActivity::class.java)

            intent.putExtra("photos", selfies.map { it.absolutePath } as ArrayList<String>)
            intent.putExtra("start_index", position)

            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = selfies.size
}