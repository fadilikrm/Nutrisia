package com.example.nutrisia

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrisia.databinding.ActivityDailyStreakBinding
import com.example.nutrisia.databinding.DialogWarningBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class DiaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDailyStreakBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyStreakBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menampilkan tanggal sekarang
        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        binding.tvDate.text = currentDate

        // Ambil ID pengguna dari SharedPreferences
        val userId = getLoggedInUserId()
        if (userId == null) {
            Toast.makeText(this, "ID pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Navigasi tombol lainnya
        binding.IconAbout.setOnClickListener {
            startActivity(Intent(this, AboutUsActivity::class.java))
        }

        binding.IconProfile.setOnClickListener {
            val intent = Intent(this, ActivityUserInformation::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        binding.Olahraga.setOnClickListener {
            val intent = Intent(this, ActivityOlahraga::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        binding.btnIsiProfile.setOnClickListener {
            if (userId != null) {
                val intent = Intent(this, ActivityInsertProfile::class.java)
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigasi ke OCRActivity saat ll_food_target diklik
        val llFoodTarget = findViewById<LinearLayout>(R.id.ll_food_target)
        llFoodTarget.setOnClickListener {
            val intent = Intent(this, OcrActivity::class.java)
            startActivity(intent)
        }
    }

    // Pastikan data selalu diperbarui saat activity kembali muncul
    override fun onResume() {
        super.onResume()
        val userId = getLoggedInUserId()
        if (userId != null) {
            fetchUserData(userId)
        }
    }

    private fun fetchUserData(userId: Int) {
        val params = mapOf("id" to userId.toString())

        RetrofitClient.instance.getProgram(params).enqueue(object : Callback<ViewProgramResponse> {
            override fun onResponse(call: Call<ViewProgramResponse>, response: Response<ViewProgramResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    val programData = response.body()?.data
                    if (programData != null) {
                        populateDiaryFields(programData)
                    } else {
                        Toast.makeText(this@DiaryActivity, "Program tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@DiaryActivity, "Gagal memuat program", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ViewProgramResponse>, t: Throwable) {
                Toast.makeText(this@DiaryActivity, "Kesalahan jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateDiaryFields(data: ViewProgram) {
        val totalCalories = data.jml_kal
        val remainingCalories = data.remaining_calories

        binding.tvNameUser2.text = "${data.fullname}👋"
        binding.tvProgram2.text = data.program
        binding.textView3.text = data.status_bmi
        binding.textViewbmivalue.text = data.bmi.toString()
        binding.tvCalorieTitle3.text = data.jml_kal.toString()
        binding.tvcalorieremainingvalue.text = data.remaining_calories.toString()

        if (remainingCalories < totalCalories * 0.2) {
            // Ubah warna menjadi merah
            binding.tvcalorieremainingvalue.setTextColor(Color.RED)
        } else {
            // Ubah warna menjadi default (misalnya hitam)
            binding.tvcalorieremainingvalue.setTextColor(Color.BLACK)
        }
    }

    private fun getLoggedInUserId(): Int? {
        val sharedPreferences = getSharedPreferences("NutrisiaPrefs", MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1).takeIf { it != -1 }
    }
}
