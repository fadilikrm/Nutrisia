package com.example.nutrisia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView

class FoodHistoryAdapter : ListAdapter<FoodItem, FoodHistoryAdapter.FoodHistoryViewHolder>(FoodHistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodHistoryViewHolder, position: Int) {
        val foodHistory = getItem(position)
        holder.bind(foodHistory)
    }

    class FoodHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //        private val foodNameTextView: TextView = itemView.findViewById(R.id.food_name)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.total_calories)
        private val dateTextView: TextView = itemView.findViewById(R.id.scan_date)

        fun bind(foodHistory: FoodItem) {
//            foodNameTextView.text = foodHistory.food_name
            caloriesTextView.text = "Kalori: ${foodHistory.total_calories} kkal"
            dateTextView.text = "Tanggal: ${foodHistory.scan_date}"
        }
    }

    class FoodHistoryDiffCallback : DiffUtil.ItemCallback<FoodItem>() {
        override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem.scan_date == newItem.scan_date
        }

        override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem == newItem
        }
    }
}
