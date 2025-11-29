package com.example.btqt_nhom3.Photo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btqt_nhom3.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DailySelfieAdapter(
    private var dailySelfies: Map<String, List<File>>,
    private val onPhotoClick: (File) -> Unit,
    private val onSelectionChanged: ((count: Int, files: List<File>) -> Unit)? = null
) : RecyclerView.Adapter<DailySelfieAdapter.DayVH>() {

    private val selfieAdapters = mutableListOf<SelfieAdapter>()

    private val dateList: List<String>
        get() = dailySelfies.keys.sortedDescending()

    class DayVH(item: View) : RecyclerView.ViewHolder(item) {
        val tvDay: TextView = item.findViewById(R.id.tvDay)
        val rvDay: RecyclerView = item.findViewById(R.id.rvDay)
        val cvEmpty: CardView = item.findViewById(R.id.cvEmpty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        return DayVH(v)
    }

    override fun getItemCount(): Int = dateList.size

    override fun onBindViewHolder(holder: DayVH, position: Int) {

        val date = dateList[position]
        val todayFull = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // -------- TEXT NGÀY ----------
        holder.tvDay.text = when (date) {
            "on_this_day" -> "Ngày này năm xưa"
            todayFull -> "Hôm nay"
            else -> {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat =
                    SimpleDateFormat("'Ngày' dd 'tháng' MM 'năm' yyyy", Locale.getDefault())
                runCatching {
                    inputFormat.parse(date)?.let { outputFormat.format(it) }
                }.getOrNull() ?: date
            }
        }

        // -------- LẤY DANH SÁCH ẢNH ——
        val selfies: List<File> = if (date == "on_this_day") {
            val todayMonthDay = SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date())

            dailySelfies
                .filter { (d, _) ->
                    d.length >= 10 &&
                            d.substring(5, 10) == todayMonthDay &&
                            d != todayFull
                }
                .toSortedMap(compareByDescending { it })
                .flatMap { it.value }
        } else {
            dailySelfies[date] ?: emptyList()
        }

        // -------- UI EMPTY / RECYCLER ----------
        val hasPhoto = selfies.isNotEmpty()
        holder.rvDay.visibility = if (hasPhoto) View.VISIBLE else View.GONE
        holder.cvEmpty.visibility = if (!hasPhoto) View.VISIBLE else View.GONE

        val manager = GridLayoutManager(holder.itemView.context, 3)
        manager.isItemPrefetchEnabled = false
        holder.rvDay.layoutManager = manager

        // -------- CHILD ADAPTER -------------
        val childAdapter = SelfieAdapter(selfies, onPhotoClick) { _ ->
            val allSelected = selfieAdapters.flatMap { it.getSelectedPhotos() }
            onSelectionChanged?.invoke(allSelected.size, allSelected)
        }

        if (!selfieAdapters.contains(childAdapter)) {
            selfieAdapters.add(childAdapter)
        }

        holder.rvDay.adapter = childAdapter
        holder.rvDay.setHasFixedSize(false)
        holder.rvDay.isNestedScrollingEnabled = false
    }

    fun updateData(newData: Map<String, List<File>>) {
        selfieAdapters.clear()
        dailySelfies = newData
        notifyDataSetChanged()
    }

    fun clearAllSelections() {
        selfieAdapters.forEach { it.clearSelection(false) }
        onSelectionChanged?.invoke(0, emptyList())
    }
}
