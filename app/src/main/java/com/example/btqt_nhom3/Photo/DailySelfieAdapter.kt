package com.example.btqt_nhom3.Photo

import android.content.Intent
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
import java.util.Date
import java.util.Locale

class DailySelfieAdapter (private val dailySelfies: Map<String, List<File>>)
    : RecyclerView.Adapter<DailySelfieAdapter.DayViewHolder>() {

    private val dateList: List<String>
        get() {
            val sorted = dailySelfies.keys.sortedDescending().toMutableList()

            val timestamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val todayMonthDay = SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date())

            if (!sorted.contains(timestamp)) {
                sorted.add(0, timestamp)
            }

            val onThisDayExists = dailySelfies.keys.any { it.substring(5, 10) == todayMonthDay && it != timestamp }
            if (onThisDayExists) {
                sorted.add(0, "on_this_day")
            }

            return sorted
        }
    class DayViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val tvDay : TextView = itemView.findViewById(R.id.tvDay)
        val rvDay : RecyclerView = itemView.findViewById(R.id.rvDay)
        val cvEmpty : CardView = itemView.findViewById(R.id.cvEmpty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        var date = dateList[position]

        val timestamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val displayDate = when (date) {
            "on_this_day" -> "Ngày này năm xưa"
            timestamp -> "Hôm nay"
            else -> {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat =
                    SimpleDateFormat("'Ngày' dd 'tháng' MM 'năm' yyyy", Locale.getDefault())
                inputFormat.parse(date)?.let { outputFormat.format(it) } ?: date
            }
        }

        val selfies = if (date == "on_this_day") {
            val todayMonthDay = SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date())

            dailySelfies
                .filter { (d, _) -> d.substring(5, 10) == todayMonthDay && d != timestamp }
                .toSortedMap(compareByDescending { it })   // sort key yyyy-MM-dd descending
                .flatMap { it.value }
        } else {
            dailySelfies[date] ?: emptyList()
        }

        holder.tvDay.text = displayDate

        holder.rvDay.visibility = if (selfies.isEmpty()) View.GONE else View.VISIBLE
        holder.cvEmpty.visibility = if (selfies.isEmpty()) View.VISIBLE else View.GONE

        holder.rvDay.layoutManager = GridLayoutManager(holder.itemView.context, 3)
        holder.rvDay.adapter = SelfieAdapter(selfies) { file ->
            val context = holder.itemView.context
            val intent = Intent(context, PhotoViewerActivity::class.java)
            intent.putExtra("file_path", file.absolutePath)
            context.startActivity(intent)
        }

        holder.rvDay.setHasFixedSize(true)
        holder.rvDay.isNestedScrollingEnabled = false
    }

    override fun getItemCount(): Int = dateList.size
}