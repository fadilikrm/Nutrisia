package com.example.nutrisia

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class FoodHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var foodHistoryAdapter: FoodHistoryAdapter
    private val apiService: ApiService by lazy { RetrofitClient.instance }
    private lateinit var dateEditText: EditText
    private lateinit var totalCaloriesTextView: TextView
    private var foodHistoryList: List<FoodItem> = emptyList() // Full list of food history data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_history)

        // Initialize UI components
        recyclerView = findViewById(R.id.recycler_view)
        dateEditText = findViewById(R.id.date_edit_text)
        totalCaloriesTextView = findViewById(R.id.total_calories_text_view)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        foodHistoryAdapter = FoodHistoryAdapter()
        recyclerView.adapter = foodHistoryAdapter

        // Set today's date in the EditText
        val currentDate = getCurrentDate()
        dateEditText.setText(currentDate)

        // Fetch food history data
        getFoodHistory(currentDate)

        // Date picker for the date field
        dateEditText.setOnClickListener { showDatePicker() }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return String.format("%04d-%02d-%02d", year, month + 1, day)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            dateEditText.setText(selectedDate)
            filterHistoryByDate(selectedDate) // Automatically filter data based on the selected date
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun getFoodHistory(selectedDate: String) {
        val userId = intent.getIntExtra("USER_ID", -1)

        if (userId != -1) {
            apiService.getFoodHistory(userId).enqueue(object : Callback<FoodHistoryResponse> {
                override fun onResponse(call: Call<FoodHistoryResponse>, response: Response<FoodHistoryResponse>) {
                    response.body()?.let { foodHistoryResponse ->
                        if (foodHistoryResponse.status) {
                            foodHistoryList = foodHistoryResponse.data ?: emptyList()
                            filterHistoryByDate(selectedDate) // Filter data for today when it's fetched
                        } else {
                            Toast.makeText(this@FoodHistoryActivity, foodHistoryResponse.message, Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Toast.makeText(this@FoodHistoryActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<FoodHistoryResponse>, t: Throwable) {
                    Toast.makeText(this@FoodHistoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterHistoryByDate(date: String) {
        if (date.isNotEmpty()) {
            val filteredList = foodHistoryList.filter { it.scan_date == date }
            if (filteredList.isEmpty()) {
                Toast.makeText(this, "No records found for the selected date", Toast.LENGTH_SHORT).show()
                totalCaloriesTextView.text = "Total Calories: 0 kkal"
            } else {
                foodHistoryAdapter.submitList(filteredList) // Update RecyclerView with filtered data
                calculateTotalCalories(filteredList) // Calculate total calories
            }
        } else {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateTotalCalories(filteredList: List<FoodItem>) {
        val totalCalories = filteredList.sumOf { it.total_calories }
        totalCaloriesTextView.text = "Total Calories: $totalCalories kkal"
    }
}