package com.projekt.prohealth.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.projekt.prohealth.R
import com.projekt.prohealth.entity.Run
import com.projekt.prohealth.utility.Utilities
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RunAdapter: RecyclerView.Adapter<RunAdapter.ViewHolder>() {

    inner class ViewHolder(v:View):RecyclerView.ViewHolder(v){
        var mapImage:ImageView
        var dateTextView: TextView
        var distanceTextView: TextView
        var durationTextView: TextView

        init {
            mapImage = v.findViewById(R.id.map_iv)
            dateTextView = v.findViewById(R.id.date_tv)
            distanceTextView = v.findViewById(R.id.distance_tv)
            durationTextView = v.findViewById(R.id.duration_tv)
        }

    }

    private val diffUtil = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem == newItem
        }

    }
    private var differ = AsyncListDiffer(this, diffUtil)
    fun setData(data: List<Run>){
        differ.submitList(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.run_item_recycler_view,parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        differ.currentList[position].img?.let {
            holder.mapImage.setImageBitmap(Utilities.byteArrayToBitmap(it))
        }
        val date = Date(differ.currentList[position].timestamp)
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val formattedDate = format.format(date)
        holder.dateTextView.text = "Date: $formattedDate"
        holder.distanceTextView.text = "Distance: ${String.format("%.2f", differ.currentList[position].distanceInMeter.div(1000))} KM"
        holder.durationTextView.text = "Duration: ${Utilities.formatTime(differ.currentList[position].time.toInt())}"
    }
}